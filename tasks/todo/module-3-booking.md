# Module 3 — Reservations/Booking

Owner: teammate D. Depends on Module 1 (`tasks/todo/module-1-rooms.md`, specifically `RoomService`) and Module 2 (`tasks/todo/module-2-guests.md`, specifically `GuestService`). See `tasks/plan.md` for full context and DDL. Sequence top-to-bottom.

- [ ] 3.1 `Reservation` model + `ReservationStatus` enum
  - Acceptance: POJO with `id, roomId, guestId, staffId, checkIn, checkOut, status, createdAt`; `ReservationStatus` = `BOOKED, CHECKED_IN, CHECKED_OUT, CANCELLED`.
  - Verify: compiles.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/model/Reservation.java`, `src/main/java/com/hotel/model/ReservationStatus.java`
- [ ] 3.2 `ReservationDao` interface
  - Acceptance: CRUD + a conflict-check query method (e.g. `boolean hasConflict(roomId, checkIn, checkOut)`) + a joined listing method returning reservation rows enriched with room number + guest name.
  - Verify: compiles.
  - Dependencies: 3.1
  - Files: `src/main/java/com/hotel/dao/ReservationDao.java`
- [ ] 3.3 `JdbcReservationDao` impl
  - Acceptance: implements 3.2; conflict-check and listing queries use real `JOIN`s per SPEC's SQL coverage goals.
  - Verify: insert a reservation via raw SQL/data.sql, confirm `hasConflict` returns true for an overlapping range and false for a non-overlapping one; confirm the joined listing returns room number + guest name correctly.
  - Dependencies: 3.2, 0.3, 1.6 (Module 1), 2.3 (Module 2)
  - Files: `src/main/java/com/hotel/dao/JdbcReservationDao.java`
- [ ] 3.4 `ReservationService` — create reservation
  - Acceptance: `@Transactional` method that re-checks availability (via `RoomService`/`ReservationDao`) then inserts, to close the race between search and booking.
  - Verify: create two overlapping reservations for the same room back-to-back through the service; the second is rejected.
  - Dependencies: 3.3, 1.7 (Module 1's `RoomService`)
  - Files: `src/main/java/com/hotel/service/ReservationService.java`
- [ ] 3.5 `ReservationService` — check-in/check-out/cancel
  - Acceptance: state transition methods that update `reservations.status` and the corresponding `rooms.status` (e.g. check-in → room OCCUPIED, check-out/cancel → room AVAILABLE), each `@Transactional`.
  - Verify: check a booked reservation in, confirm room flips to OCCUPIED; check it out, confirm room flips back to AVAILABLE and reservation status is CHECKED_OUT.
  - Dependencies: 3.4
  - Files: `src/main/java/com/hotel/service/ReservationService.java` (same file as 3.4, additional methods)
**Swing note:** read `tasks/todo/swing-guide.md` first, especially section 3 (date pickers via `JSpinner` + `SpinnerDateModel` — core Swing has no calendar widget and we're not adding a dependency for one) and section 4 (`JComboBox` for picking an existing room/guest). This module's UI is the most involved in the app since it combines a search form, a create form, and a live-updating table in one screen — build it as two cooperating panels (both registered as one "Booking" card in `MainFrame`, e.g. stacked with `BoxLayout(Y_AXIS)` or split with `JSplitPane`) rather than cramming everything into one class.

- [ ] 3.6 `BookingPanel` — availability search + create form
  - Acceptance: `BorderLayout` panel. `NORTH`: search bar with two `JSpinner`s (check-in / check-out, per guide section 3, defaulting to today and tomorrow) and a "Search" button; `CENTER`: a second, smaller `JScrollPane`-wrapped `JTable` (its own `BaseTableModel<Room>`, columns Room Number / Type / Capacity) showing `RoomService.findAvailable(checkIn, checkOut)` results — reuse the spinners' current values, don't duplicate date fields. `SOUTH`: a "Book Selected Room" button, disabled until a row is selected (`table.getSelectionModel().addListSelectionListener(...)` to enable/disable it), which opens a `ReservationFormDialog`: pre-fills the chosen room + dates (read-only, since they came from the search), and adds a `JComboBox<Guest>` (populated from `guestService.findAll()`, relying on `Guest.toString()` returning the guest's name per guide section 4) — plus a small "New Guest" button next to the combo that opens Module 2's `GuestFormDialog.showCreate` and, on success, re-populates the combo and selects the new guest. On OK, calls `reservationService.create(...)`; on the availability-conflict error, shows it via `Dialogs.showError` (this can legitimately happen if someone else booked the room between search and click — that's exactly what 3.4's re-check guards against) rather than crashing.
  - Verify: search a date range, confirm only available rooms show (compare against a room you've manually booked for overlapping dates via 0.6/manual SQL); create a reservation from a search result and confirm it's saved and the room drops out of a repeated search for the same dates.
  - Dependencies: 3.4, 1.7 (Module 1), 2.4 (Module 2's `GuestService`), 0.7, 0.8
  - Files: `src/main/java/com/hotel/ui/booking/BookingPanel.java`, `src/main/java/com/hotel/ui/booking/ReservationFormDialog.java`
- [ ] 3.7 `ActiveReservationsPanel` — active reservations table + actions
  - Acceptance: `BorderLayout` panel; `CENTER`: `JScrollPane`-wrapped `JTable` via `BaseTableModel<ReservationView>` (whatever row type 3.3's joined listing returns — needs room number + guest name + dates + status columns, not just raw `Reservation` ids) populated from `reservationService`'s active-reservations listing; `SOUTH`: "Check In", "Check Out", and "Cancel" buttons, each disabled/enabled based on the selected row's current status (e.g. Check In only enabled when status is BOOKED, Check Out only when CHECKED_IN — grey out the rest rather than letting the user trigger an invalid transition and catch an exception). Each button calls the matching `ReservationService` method, wraps it in `try/catch` reporting via `Dialogs.showError`, then calls this panel's `refresh()` — and must also trigger `BookingPanel`'s room-search `refresh()` if it's currently showing stale results (simplest approach: have `BookingPanel` re-run its search whenever its card becomes visible again, rather than pushing a cross-panel refresh call).
  - Verify: full manual flow — create a reservation in 3.6, confirm it appears here as BOOKED with Check In enabled; check it in, confirm status flips to CHECKED_IN, room's status updates (spot-check via Module 1's `RoomPanel`), and Check Out becomes enabled while Check In disables; check it out and confirm the same for CHECKED_OUT.
  - Dependencies: 3.6, 3.5
  - Files: `src/main/java/com/hotel/ui/booking/ActiveReservationsPanel.java`

**Exposes to other modules:** `ReservationService` (used by Module 5's `BillingService` to generate an invoice on checkout).
