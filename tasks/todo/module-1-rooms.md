# Module 1 — Rooms & Room Types

Owner: teammate A. No dependencies on other modules (only Task 0). See `tasks/plan.md` for full context and DDL. Sequence top-to-bottom.

- [ ] 1.1 `RoomType` model
  - Acceptance: POJO with `id, name, basePrice, capacity, description` fields, constructor, getters (and setters if the DAO row-mapper needs them).
  - Verify: compiles.
  - Dependencies: 0.1 (`tasks/todo/task-0-scaffolding.md`)
  - Files: `src/main/java/com/hotel/model/RoomType.java`
- [ ] 1.2 `Room` model
  - Acceptance: POJO with `id, roomNumber, roomTypeId, status` (status as a `RoomStatus` enum: AVAILABLE/OCCUPIED/MAINTENANCE).
  - Verify: compiles.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/model/Room.java`, `src/main/java/com/hotel/model/RoomStatus.java`
- [ ] 1.3 `RoomTypeDao` interface
  - Acceptance: `Optional<RoomType> findById(long)`, `List<RoomType> findAll()`, `void save(RoomType)`, per SPEC's DAO contract (`Optional`/`List`, never `null`).
  - Verify: compiles.
  - Dependencies: 1.1
  - Files: `src/main/java/com/hotel/dao/RoomTypeDao.java`
- [ ] 1.4 `JdbcRoomTypeDao` impl
  - Acceptance: implements 1.3 with `JdbcTemplate`, using `PreparedStatement` params (no string concatenation).
  - Verify: with 0.5/0.6 seed data loaded, a scratch call to `findAll()` returns the seeded room types.
  - Dependencies: 1.3, 0.3, 0.6
  - Files: `src/main/java/com/hotel/dao/JdbcRoomTypeDao.java`
- [ ] 1.5 `RoomDao` interface
  - Acceptance: CRUD methods plus `List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut)`.
  - Verify: compiles.
  - Dependencies: 1.2
  - Files: `src/main/java/com/hotel/dao/RoomDao.java`
- [ ] 1.6 `JdbcRoomDao` impl
  - Acceptance: implements 1.5; `findAvailable` uses the NOT IN / date-overlap query from SPEC.md's Code Style example.
  - Verify: with seed data + a manually inserted test reservation (via raw SQL), confirm `findAvailable` excludes the booked room for overlapping dates and includes it for non-overlapping dates.
  - Dependencies: 1.5, 0.3, 0.6
  - Files: `src/main/java/com/hotel/dao/JdbcRoomDao.java`
- [ ] 1.7 `RoomService`
  - Acceptance: validates unique room number and valid `room_type_id` before delegating to `RoomDao`/`RoomTypeDao`; exposes `findAvailable` for Module 3 to reuse.
  - Verify: unit-style scratch call — adding a duplicate room number throws/returns a validation error instead of hitting the DB.
  - Dependencies: 1.4, 1.6
  - Files: `src/main/java/com/hotel/service/RoomService.java`
**Swing note:** read `tasks/todo/swing-guide.md` before 1.8/1.9 — both follow the "list + search" shape (section 1) and "form dialog" shape (section 2) exactly; don't design a new layout from scratch.

- [ ] 1.8 `RoomTypePanel` UI
  - Acceptance: `BorderLayout` panel — `NORTH`: just an "Add Room Type" `JButton` (no search needed, there are only 3-4 room types); `CENTER`: a `JScrollPane`-wrapped `JTable` backed by a `BaseTableModel<RoomType>` with columns Name / Base Price / Capacity / Description. Add button opens a `RoomTypeFormDialog` (built on `Dialogs.showForm`, guide section 2) with `JTextField`s for name/capacity/description and a `JTextField` or `JSpinner` (`SpinnerNumberModel`) for base price; double-click a row opens the same dialog pre-filled for editing. On OK, calls `RoomService`/`RoomTypeDao` (whichever the service exposes) then calls the panel's `refresh()`.
  - Verify: add a room type through the UI, confirm it appears in the list and persists after app restart; edit one and confirm the change shows; try adding a duplicate name and confirm `RoomService`'s validation error surfaces via `Dialogs.showError` instead of a raw exception/stack trace.
  - Dependencies: 1.7, 0.7, 0.8
  - Files: `src/main/java/com/hotel/ui/rooms/RoomTypePanel.java`, `src/main/java/com/hotel/ui/rooms/RoomTypeFormDialog.java`
- [ ] 1.9 `RoomPanel` UI
  - Acceptance: `BorderLayout` panel — `NORTH`: "Add Room" `JButton`; `CENTER`: `JScrollPane`-wrapped `JTable` backed by `BaseTableModel<Room>` with columns Room Number / Room Type / Status. Add/edit via a `RoomFormDialog` with a `JTextField` for room number and a `JComboBox<RoomType>` (populated from `RoomService`/`RoomTypeDao`, relies on `RoomType.toString()` returning its name per guide section 4) to pick the type. Status change is a separate, simpler action: right-click or a "Change Status" button on the selected row opens a `JComboBox<RoomStatus>` picker (a small `Dialogs.showForm` with one combo field) and calls the service to persist it.
  - Verify: add a room, confirm it appears with correct type/status; change status to MAINTENANCE via the UI and confirm it persists (re-open the panel or restart the app).
  - Dependencies: 1.7, 0.7, 0.8
  - Files: `src/main/java/com/hotel/ui/rooms/RoomPanel.java`, `src/main/java/com/hotel/ui/rooms/RoomFormDialog.java`

**Exposes to other modules:** `RoomService` (used by Module 3 for availability search).
