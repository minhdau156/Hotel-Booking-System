# Implementation Plan: Hotel Booking System

## Context

This is a 5-person school project combining an OOP subject and a SQL subject. The approved spec (`D:\Hotel-Booking-System\SPEC.md`) defines a desktop Swing app backed by MySQL, using Spring Boot only for its DI container + `JdbcTemplate` (hand-written SQL, no ORM) so the SQL subject's requirements stay visible in code. The project is greenfield — only `SPEC.md` exists on disk today, nothing to explore or reuse.

This plan turns that spec into buildable, module-owned tasks so all 5 teammates can work with minimal file overlap, plus the concrete DDL and package skeleton needed to start.

**Decisions resolved during planning** (to be folded into SPEC.md's Assumptions/Success Criteria, replacing its "Open Questions" section, as the first task below):

- Pricing: flat `base_price` per room type (no seasonal rates).
- SQL feature coverage: JOINs, subqueries, `GROUP BY`/aggregates, and `@Transactional` — no stored procedures/triggers/views needed.
- Staff accounts: Admin gets an in-app staff management screen (create/edit/deactivate), not just DB-seeded accounts.

## Task 0 — Shared scaffolding (do first, blocks everyone)

One person builds this before the 5 modules can start in parallel.

- **0.1 Maven skeleton**: `pom.xml` (Java 21, Spring Boot 3.3.x parent, `spring-boot-starter`, `spring-boot-starter-jdbc`, `spring-security-crypto`, `mysql-connector-j`), `HotelBookingApplication.java` (boots `ApplicationContext`, then launches Swing on the EDT via `SwingUtilities.invokeLater`), `config/DataSourceConfig.java` (`DataSource` + `JdbcTemplate` beans from `application.properties`), `application.properties` + `application-local.properties.example` (gitignore the real local file).
- **0.2 Schema & seed data** — `src/main/resources/schema.sql`:

  ```sql
  CREATE TABLE room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    base_price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    description VARCHAR(255)
  );

  CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type_id BIGINT NOT NULL,
    status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
  );

  CREATE TABLE guests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    id_number VARCHAR(50)
  );

  CREATE TABLE staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN','RECEPTIONIST') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
  );

  CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    status ENUM('BOOKED','CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (staff_id) REFERENCES staff(id),
    CHECK (check_out > check_in)
  );
  CREATE INDEX idx_reservations_room_dates ON reservations(room_id, check_in, check_out);

  CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
  );

  CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method ENUM('CASH','CARD','TRANSFER') NOT NULL,
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id)
  );
  ```

  `data.sql`: 3-4 room types, ~10 rooms, one seeded `ADMIN` staff row (password hashed with `BCryptPasswordEncoder` at seed-generation time, not plaintext).

- **0.3 UI shell**: `ui/MainFrame.java` (JFrame with a `CardLayout` content area + a role-aware side/menu bar that shows/hides items based on `CurrentUserContext`), `ui/common/` helpers (a reusable `JTable` + `AbstractTableModel` base, a simple form-dialog helper, an error/alert dialog helper).

## Modules 1-5 (parallel after Task 0, ~1 person each)

Each module = model → DAO (interface + `Jdbc*Dao` impl) → service → Swing panel. Package by feature: `model/`, `dao/`, `service/`, `ui/<module>/`.

**Module 1 — Rooms & Room Types** (no dependencies)

- `RoomType`, `Room` models; `RoomTypeDao`/`RoomDao` (CRUD + `findAvailable(checkIn, checkOut)` using the NOT IN / date-overlap SQL from the spec's Code Style example)
- `RoomService` (validation; exposes availability search reused by Module 3)
- `ui/rooms`: room type list/add/edit, room list/add/edit/status-change

**Module 2 — Guests** (no dependencies)

- `Guest` model, `GuestDao` (CRUD + search by name/phone), `GuestService`
- `ui/guests`: guest list, search, add/edit

**Module 4 — Staff & Auth** (no dependencies — build in parallel with 1 & 2)

- `Staff` model + `Role` enum, `StaffDao` (CRUD, `findByUsername`)
- `AuthService` (verify username/password via `BCryptPasswordEncoder.matches`, populate `CurrentUserContext` — a simple Spring-managed singleton holding the logged-in `Staff`), `StaffService` (admin CRUD over staff accounts, hashes new passwords on create)
- `ui/login`: login form (first screen shown); `ui/staff`: admin-only staff management panel

**Module 3 — Reservations/Booking** (depends on Modules 1 & 2 for `RoomService`/`GuestService`)

- `Reservation` model + `ReservationStatus` enum, `ReservationDao` (CRUD, conflict-check query, joins for reservation-with-room/guest listing)
- `ReservationService` (`@Transactional` create — re-check availability then insert; check-in/check-out/cancel transitions that also flip `rooms.status`)
- `ui/booking`: availability search, create-reservation form, active-reservations table with check-in/check-out/cancel actions

**Module 5 — Invoicing, Payments & Reports** (depends on Module 3 for `Reservation`/`ReservationService`)

- `Invoice`, `Payment` models; `InvoiceDao`, `PaymentDao`
- `BillingService` (`@Transactional` generate invoice on checkout = nights × room rate, record payment)
- `ReportService` (raw aggregate SQL: occupancy by date range, revenue by period via `GROUP BY`/`SUM`/`JOIN`)
- `ui/billing`: invoice view + record-payment dialog; `ui/reports`: admin-only occupancy/revenue screens

## Task 6 — Integration (after all modules land)

- Wire `MainFrame` menu visibility to `CurrentUserContext.role` (Admin sees Staff + Reports; Receptionist doesn't)
- Wire login success → `MainFrame` launch, logout → clear `CurrentUserContext` → back to login
- Update `SPEC.md`: replace "Open Questions" section with the resolved decisions above; confirm the "Assumptions" section still matches what was built

## Verification (no automated tests, per project scope)

Manual run-through against the spec's Success Criteria checklist:

1. `mysql < schema.sql`, `mysql < data.sql` against a local MySQL 8 instance; set real credentials in `application-local.properties`.
2. `mvn spring-boot:run` — app opens to the login screen (not any other screen).
3. Log in as the seeded admin → confirm Staff management + Reports are visible; log in as a receptionist account → confirm they're hidden.
4. As receptionist: search available rooms for a date range, create a reservation, confirm a second overlapping booking for the same room is rejected.
5. Check the guest in, then check out → confirm an invoice is generated with the correct total (nights × rate) and room status flips back to AVAILABLE.
6. Record a payment against the invoice.
7. As admin: view occupancy report and revenue-by-period report and confirm numbers match the bookings created in steps 4-6.
8. Confirm all 5 modules' screens are reachable from one running app against the one schema (no separate mini-apps).
