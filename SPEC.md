# Spec: Hotel Booking System

## Assumptions (please confirm or correct)
1. Build tool: **Maven** (most common pairing with Spring Boot in coursework).
2. Java version: **21** (LTS).
3. Spring Boot version: latest stable **3.3.x**, using only `spring-boot-starter` + `spring-boot-starter-jdbc` (no web starter — this is a desktop app, not a web app).
4. DB driver: `mysql-connector-j`.
5. The app is a **single desktop process**: Spring Boot's `ApplicationContext` boots headless (`SpringApplication.run` with `setHeadless(false)`), then hands control to a Swing `JFrame` — no embedded servlet container.
6. Team of 5 → 5 module owners (see **Module Ownership** below), each responsible for that module's table(s), DAO, service, and Swing panel.
7. Single hotel branch (not multi-property) for v1, per "Standard set" scope.
8. Authentication is a **real login screen** (username + password), not a role-picker: the app's first window is a login form; passwords are stored hashed (BCrypt) in `staff.password_hash`, never in plaintext. No session tokens needed — it's a single-user desktop process, so the logged-in `Staff` is just held in memory (e.g. a `CurrentUserContext` bean) for the rest of the app's lifetime, cleared on logout.
9. Room pricing is a **flat `base_price` per room type** — no seasonal/dynamic rates in v1.
10. SQL feature coverage for the SQL subject: JOINs, subqueries, `GROUP BY`/aggregate functions, and `@Transactional` transactions — no stored procedures, triggers, or views required.
11. Admin gets an **in-app staff management screen** (create/edit/deactivate staff accounts) rather than DB-seeded-only accounts.

## Objective
A desktop Hotel Booking System for a single hotel, used by front-desk staff to manage rooms, guests, reservations, billing, and view basic reports. Built as a team project to demonstrate:
- **OOP**: layered architecture (Model / DAO / Service / UI), interfaces, inheritance (e.g. staff roles), encapsulation.
- **SQL**: hand-written SQL via Spring `JdbcTemplate` (joins, transactions, aggregate queries for reports) — no ORM.

**Users**: Hotel receptionists (create/manage bookings, check-in/out) and Admins (manage rooms, staff, view reports).

**Success looks like**: a runnable Swing app backed by MySQL where a receptionist can search room availability by date, create a reservation for a guest, check them in/out, generate an invoice, and an admin can view occupancy/revenue reports — with no double-bookings possible.

## Tech Stack
- Java 21
- Spring Boot 3.3.x (`spring-boot-starter`, `spring-boot-starter-jdbc`) — used purely for IoC/DI container + `JdbcTemplate` + `@Transactional`
- `spring-security-crypto` (just the `BCryptPasswordEncoder`, not full Spring Security — no web/filter chain needed for a desktop app)
- Swing (`javax.swing`) for UI, using `FlatLaf` or default look-and-feel (TBD, cosmetic)
- MySQL 8.x
- Maven

## Commands
```
Build:  mvn clean package
Run:    mvn spring-boot:run
Package: mvn clean package -> java -jar target/hotel-booking-system.jar
```

## Project Structure
```
src/main/java/com/hotel/
  HotelBookingApplication.java     → main entrypoint (SpringApplication + launches Swing UI)
  model/                           → POJOs: Room, RoomType, Guest, Reservation, Staff, Invoice, Payment
  dao/                             → interfaces + JdbcTemplate implementations (RoomDao, ReservationDao, ...)
  service/                         → business logic, transactions (RoomService, ReservationService, ...)
  ui/                              → Swing JPanels/JFrames, one subpackage per module (ui/rooms, ui/booking, ...)
  ui/common/                       → shared Swing components (tables, dialogs, validators)
  config/                          → Spring @Configuration (DataSource, JdbcTemplate bean)
src/main/resources/
  application.properties           → DB connection config
  schema.sql                       → DDL for all tables (run manually or via Spring init)
  data.sql                         → seed/sample data (room types, demo rooms, admin user)
docs/
  SPEC.md                           → this file
  er-diagram.png / .md              → entity relationship diagram (Plan phase)
tasks/
  plan.md, todo.md                  → produced in Plan/Tasks phases
```

## Module Ownership (5-person team)
| # | Module | Owns tables | Owns classes |
|---|---|---|---|
| 1 | **Rooms & Room Types** | `room_types`, `rooms` | RoomType, Room, RoomDao, RoomService, ui/rooms |
| 2 | **Guests** | `guests` | Guest, GuestDao, GuestService, ui/guests |
| 3 | **Reservations / Booking** | `reservations` | Reservation, ReservationDao, ReservationService (availability/conflict checks), ui/booking |
| 4 | **Staff & Auth** | `staff`, roles | Staff (base) → Receptionist/Admin (inheritance or role enum), StaffDao, AuthService (login, BCrypt verify, CurrentUserContext), ui/login |
| 5 | **Invoicing, Payments & Reports** | `invoices`, `payments` | Invoice, Payment, BillingService, ReportService (SQL aggregate queries), ui/billing, ui/reports |

Modules 1–2 have no dependencies and can start immediately. Module 3 depends on 1 & 2. Module 4 is independent (can run in parallel). Module 5 depends on 3.
Build order: **(Rooms, Guests, Staff/Auth in parallel) → Reservations → Invoicing/Reports**.

## Core Entities (high-level — full DDL drafted in Plan phase)
- `room_types (id, name, base_price, capacity, description)`
- `rooms (id, room_number, room_type_id FK, status)` — status: AVAILABLE/OCCUPIED/MAINTENANCE
- `guests (id, full_name, phone, email, id_number)`
- `staff (id, username, password_hash, full_name, role, active)` — role: ADMIN/RECEPTIONIST
- `reservations (id, room_id FK, guest_id FK, staff_id FK, check_in, check_out, status)` — status: BOOKED/CHECKED_IN/CHECKED_OUT/CANCELLED
- `invoices (id, reservation_id FK, total_amount, issued_at)`
- `payments (id, invoice_id FK, amount, method, paid_at)`

## Code Style
Layered, interface-first DAO pattern:
```java
public interface RoomDao {
    Optional<Room> findById(long id);
    List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut);
    void save(Room room);
}

@Repository
public class JdbcRoomDao implements RoomDao {
    private final JdbcTemplate jdbc;

    public JdbcRoomDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        String sql = """
            SELECT r.* FROM rooms r
            WHERE r.status = 'AVAILABLE'
              AND r.id NOT IN (
                SELECT res.room_id FROM reservations res
                WHERE res.status IN ('BOOKED','CHECKED_IN')
                  AND res.check_in < ? AND res.check_out > ?
              )
            """;
        return jdbc.query(sql, this::mapRow, checkOut, checkIn);
    }
    // ...
}
```
- Package by feature under `model/dao/service/ui`, not by layer-then-feature.
- DAOs return `Optional<T>` for single lookups, `List<T>` for collections; never return `null`.
- Services own transaction boundaries (`@Transactional`) — DAOs never manage transactions themselves.
- Swing panels never call DAOs directly — always go through a Service.
- Constructor injection only (no field `@Autowired`).

## Boundaries
- **Always**: SQL lives only in DAO classes (never inline in Service or UI).
- **Ask first**: changing a table schema once another module depends on it; adding a new Maven dependency; changing the module ownership split.
- **Never**: commit `application.properties` with real DB passwords (use `application-local.properties`, gitignored); write SQL string-concatenated with user input (always use `PreparedStatement`/`JdbcTemplate` parameters — no SQL injection); delete another module owner's files without discussion.

## Success Criteria
- [ ] App opens to a login screen; a staff member must authenticate with username + password (verified against the BCrypt hash) before reaching any other screen; wrong credentials show an error and don't proceed.
- [ ] Logged-in role (ADMIN vs RECEPTIONIST) determines which screens/menu items are visible (e.g. only Admin sees Staff management and Reports).
- [ ] Receptionist can search available rooms by date range and room type.
- [ ] Receptionist can create a reservation for an existing or new guest; overlapping bookings for the same room are rejected at the DB/service layer.
- [ ] Receptionist can check a guest in and out, updating room status accordingly.
- [ ] An invoice is generated on checkout, total computed from nights × room rate.
- [ ] Admin can log in separately from receptionist and view an occupancy report and a revenue-by-period report (SQL aggregate queries, e.g. `GROUP BY`, `SUM`).
- [ ] All 5 modules build and run together as one Swing app against one MySQL schema.

## Open Questions
None — all resolved (see Assumptions #9-11). See `tasks/plan.md` for the implementation plan and `tasks/todo.md` for the task breakdown.
