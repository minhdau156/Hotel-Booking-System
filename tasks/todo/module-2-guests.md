# Module 2 — Guests

Owner: teammate B. No dependencies on other modules (only Task 0). See `tasks/plan.md` for full context and DDL. Sequence top-to-bottom.

- [ ] 2.1 `Guest` model
  - Acceptance: POJO with `id, fullName, phone, email, idNumber`.
  - Verify: compiles.
  - Dependencies: 0.1 (`tasks/todo/task-0-scaffolding.md`)
  - Files: `src/main/java/com/hotel/model/Guest.java`
- [ ] 2.2 `GuestDao` interface
  - Acceptance: CRUD + `List<Guest> search(String query)` (matches name or phone).
  - Verify: compiles.
  - Dependencies: 2.1
  - Files: `src/main/java/com/hotel/dao/GuestDao.java`
- [ ] 2.3 `JdbcGuestDao` impl
  - Acceptance: implements 2.2 with `JdbcTemplate`, `search` uses `LIKE` with bound params.
  - Verify: insert 2-3 test guests, confirm `search("smi")` matches a "Smith" by partial name and a phone substring.
  - Dependencies: 2.2, 0.3
  - Files: `src/main/java/com/hotel/dao/JdbcGuestDao.java`
- [ ] 2.4 `GuestService`
  - Acceptance: basic validation (non-blank name; at least one contact field) before delegating to `GuestDao`.
  - Verify: attempting to save a guest with a blank name is rejected.
  - Dependencies: 2.3
  - Files: `src/main/java/com/hotel/service/GuestService.java`
**Swing note:** read `tasks/todo/swing-guide.md` first — `GuestPanel` is the guide's worked "list + search" example (section 1) almost verbatim, and `GuestFormDialog` is its worked "form dialog" example (section 2) verbatim. Start from those code blocks and adapt field names rather than writing from scratch.

- [ ] 2.5 `GuestPanel` — list + search UI
  - Acceptance: `BorderLayout` panel — `NORTH`: a `FlowLayout` bar with a `JLabel("Search:")`, a `JTextField` (~20 cols), a "Search" `JButton`, and an "Add Guest" `JButton`; `CENTER`: `JScrollPane`-wrapped `JTable` backed by `BaseTableModel<Guest>` with columns Name / Phone / Email. "Search" button's listener calls `refresh()`, which reads the search field and calls `guestService.search(text)` if non-blank else `findAll()`, then `tableModel.setRows(...)`. Double-click a row opens `GuestFormDialog` pre-filled for editing.
  - Verify: type a partial name/phone and click Search, confirm the table filters to matching guests; clear the field and search again, confirm it shows all guests.
  - Dependencies: 2.4, 0.7, 0.8
  - Files: `src/main/java/com/hotel/ui/guests/GuestPanel.java`
- [ ] 2.6 `GuestFormDialog`
  - Acceptance: static helper class, not a full `JDialog` subclass — `showCreate(Component parent, GuestService service)` and `showEdit(Component parent, GuestService service, Guest existing)` both delegate to a private `show(...)` that builds 3 `JTextField`s (name/phone/email, pre-filled from `existing` when editing), passes them to `Dialogs.showForm`, and on OK builds/mutates the `Guest` and calls `service.save(...)` inside a `try/catch (IllegalArgumentException)` that reports via `Dialogs.showError`. Returns `true` only when a save actually happened, so the caller knows whether to `refresh()`.
  - Verify: add a guest via the dialog, confirm it shows in 2.5's table and survives app restart; edit it and confirm the change persists; submit with a blank name and confirm `GuestService`'s validation error shows via `Dialogs.showError` instead of the dialog silently closing or a stack trace appearing.
  - Dependencies: 2.5, 0.8
  - Files: `src/main/java/com/hotel/ui/guests/GuestFormDialog.java`

**Exposes to other modules:** `GuestService` (used by Module 3 to attach a guest to a reservation).
