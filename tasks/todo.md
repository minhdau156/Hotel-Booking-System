# Task List: Hotel Booking System

See `tasks/plan.md` for full context, DDL, and rationale. Tasks are grouped by module owner; Task 0 must land before Modules 1-5 start.

## Task 0 — Shared scaffolding (owner: whoever starts first; blocks everyone else)
- [ ] 0.1 Maven project skeleton
  - Acceptance: `mvn spring-boot:run` launches an empty Swing frame with no errors; `pom.xml` has Java 21, Spring Boot 3.3.x parent, `spring-boot-starter`, `spring-boot-starter-jdbc`, `spring-security-crypto`, `mysql-connector-j`.
  - Verify: run `mvn spring-boot:run`, confirm a blank JFrame opens and app doesn't crash on missing DB (or DB is up).
  - Files: `pom.xml`, `src/main/java/com/hotel/HotelBookingApplication.java`, `src/main/java/com/hotel/config/DataSourceConfig.java`, `src/main/resources/application.properties`, `application-local.properties.example`, `.gitignore`
- [ ] 0.2 Schema & seed data
  - Acceptance: `schema.sql` creates all 7 tables with FKs/constraints from `tasks/plan.md`; `data.sql` seeds 3-4 room types, ~10 rooms, 1 admin staff row with a real BCrypt hash.
  - Verify: `mysql < schema.sql && mysql < data.sql` runs clean against a local MySQL 8 instance with no errors.
  - Files: `src/main/resources/schema.sql`, `src/main/resources/data.sql`
- [ ] 0.3 UI shell
  - Acceptance: `MainFrame` with `CardLayout` + role-aware menu exists (menu items wired later in Task 6); shared table/dialog helpers exist for other modules to reuse.
  - Verify: manually swap two placeholder panels via the CardLayout to confirm it works.
  - Files: `ui/MainFrame.java`, `ui/common/BaseTableModel.java`, `ui/common/Dialogs.java`

## Module 1 — Rooms & Room Types (owner: teammate A)
- [ ] 1.1 Room/RoomType models + DAOs
  - Acceptance: `RoomTypeDao`/`RoomDao` interfaces + Jdbc impls support CRUD and `findAvailable(checkIn, checkOut)`.
  - Verify: manually insert test rows via `data.sql`, run `findAvailable` from a scratch `main` method or the UI once built, confirm overlapping-booked rooms are excluded.
  - Files: `model/RoomType.java`, `model/Room.java`, `dao/RoomTypeDao.java`, `dao/JdbcRoomTypeDao.java`, `dao/RoomDao.java`, `dao/JdbcRoomDao.java`
- [ ] 1.2 RoomService
  - Acceptance: validation (unique room number, valid room_type_id) lives here, not in DAO/UI.
  - Verify: attempt to add a duplicate room number via the UI once 1.3 lands, confirm rejection.
  - Files: `service/RoomService.java`
- [ ] 1.3 Rooms UI panel
  - Acceptance: list/add/edit room types and rooms; room list shows current status.
  - Verify: manually add a room type and room, confirm it appears in the list and persists after restart.
  - Files: `ui/rooms/RoomTypePanel.java`, `ui/rooms/RoomPanel.java`

## Module 2 — Guests (owner: teammate B)
- [ ] 2.1 Guest model + DAO + Service
  - Acceptance: CRUD + search by name/phone.
  - Files: `model/Guest.java`, `dao/GuestDao.java`, `dao/JdbcGuestDao.java`, `service/GuestService.java`
  - Verify: manually add/search/edit a guest through the DAO/service.
- [ ] 2.2 Guests UI panel
  - Acceptance: list, search, add/edit guest forms.
  - Verify: add a guest, search by partial name/phone, confirm match.
  - Files: `ui/guests/GuestPanel.java`

## Module 4 — Staff & Auth (owner: teammate C; no dependencies, can start immediately)
- [ ] 4.1 Staff model + DAO
  - Acceptance: `Staff` model + `Role` enum (ADMIN/RECEPTIONIST); `StaffDao` CRUD + `findByUsername`.
  - Files: `model/Staff.java`, `model/Role.java`, `dao/StaffDao.java`, `dao/JdbcStaffDao.java`
  - Verify: fetch the seeded admin by username.
- [ ] 4.2 AuthService + StaffService + CurrentUserContext
  - Acceptance: `AuthService.login(username, password)` verifies via `BCryptPasswordEncoder.matches` and populates `CurrentUserContext`; `StaffService` hashes new passwords on create, supports edit/deactivate.
  - Files: `service/AuthService.java`, `service/StaffService.java`, `service/CurrentUserContext.java`
  - Verify: login with seeded admin credentials succeeds; wrong password fails; new staff created via service can then log in.
- [ ] 4.3 Login screen + Staff management UI
  - Acceptance: login form is the app's first screen; on success launches `MainFrame`; admin-only staff management panel (create/edit/deactivate).
  - Files: `ui/login/LoginFrame.java`, `ui/staff/StaffPanel.java`
  - Verify: run the app, confirm login screen appears first; log in as admin, reach staff management; log in as receptionist, confirm staff management is not visible (ties into Task 6).

## Module 3 — Reservations/Booking (owner: teammate D; depends on Modules 1 & 2)
- [ ] 3.1 Reservation model + DAO
  - Acceptance: `Reservation` + `ReservationStatus` enum; DAO has CRUD, conflict-check query, and a joined listing query (reservation + room + guest).
  - Files: `model/Reservation.java`, `model/ReservationStatus.java`, `dao/ReservationDao.java`, `dao/JdbcReservationDao.java`
  - Verify: insert a reservation, confirm the conflict query correctly flags an overlapping date range as unavailable.
- [ ] 3.2 ReservationService
  - Acceptance: `@Transactional` create (re-checks availability then inserts), check-in/check-out/cancel transitions that also update `rooms.status`.
  - Files: `service/ReservationService.java`
  - Verify: create two overlapping reservations for the same room back-to-back, confirm the second is rejected; check a guest in/out, confirm `rooms.status` flips accordingly.
- [ ] 3.3 Booking UI panel
  - Acceptance: search availability by date range, create reservation form, active-reservations table with check-in/check-out/cancel buttons.
  - Files: `ui/booking/BookingPanel.java`
  - Verify: full manual flow — search, book, check in, check out — from the UI.

## Module 5 — Invoicing, Payments & Reports (owner: teammate E; depends on Module 3)
- [ ] 5.1 Invoice/Payment models + DAOs
  - Files: `model/Invoice.java`, `model/Payment.java`, `dao/InvoiceDao.java`, `dao/JdbcInvoiceDao.java`, `dao/PaymentDao.java`, `dao/JdbcPaymentDao.java`
  - Verify: manually insert/fetch an invoice and payment row.
- [ ] 5.2 BillingService
  - Acceptance: `@Transactional` — generates invoice on checkout as `nights × room_type.base_price`; records payments against an invoice.
  - Files: `service/BillingService.java`
  - Verify: check a reservation out, confirm invoice total matches nights × rate.
- [ ] 5.3 ReportService
  - Acceptance: raw SQL aggregate queries — occupancy by date range, revenue by period (`GROUP BY`/`SUM`/`JOIN`).
  - Files: `service/ReportService.java`
  - Verify: run both reports against seeded + test data, confirm numbers match manual calculation.
- [ ] 5.4 Billing & Reports UI panels
  - Acceptance: invoice view + record-payment dialog; admin-only occupancy/revenue report screens.
  - Files: `ui/billing/BillingPanel.java`, `ui/reports/ReportsPanel.java`
  - Verify: full manual flow — checkout generates invoice, record payment, admin views reports reflecting it.

## Task 6 — Integration (owner: whoever finishes first; after all modules land)
- [ ] 6.1 Wire role-based menu visibility in `MainFrame` (Admin: Staff + Reports visible; Receptionist: hidden)
  - Verify: log in as each role, confirm correct menu items shown/hidden.
  - Files: `ui/MainFrame.java`
- [ ] 6.2 Wire login → MainFrame → logout flow end-to-end
  - Verify: login → use app → logout → back to login screen, `CurrentUserContext` cleared.
  - Files: `ui/login/LoginFrame.java`, `ui/MainFrame.java`
- [ ] 6.3 Full manual verification pass against `SPEC.md` Success Criteria (see `tasks/plan.md` Verification section, steps 1-8)
  - Files: none (manual QA pass)
- [ ] 6.4 Update `SPEC.md` if any decision changed during implementation
