# Module 4 — Staff & Auth

Owner: teammate C. No dependencies on other modules (only Task 0) — can start immediately alongside Modules 1 & 2. See `tasks/plan.md` for full context and DDL. Sequence top-to-bottom.

- [ ] 4.1 `Staff` model + `Role` enum
  - Acceptance: POJO with `id, username, passwordHash, fullName, role, active`; `Role` enum = `ADMIN, RECEPTIONIST`.
  - Verify: compiles.
  - Dependencies: 0.1 (`tasks/todo/task-0-scaffolding.md`)
  - Files: `src/main/java/com/hotel/model/Staff.java`, `src/main/java/com/hotel/model/Role.java`
- [ ] 4.2 `StaffDao` interface
  - Acceptance: CRUD + `Optional<Staff> findByUsername(String)`.
  - Verify: compiles.
  - Dependencies: 4.1
  - Files: `src/main/java/com/hotel/dao/StaffDao.java`
- [ ] 4.3 `JdbcStaffDao` impl
  - Acceptance: implements 4.2 with `JdbcTemplate`.
  - Verify: with 0.6 seed data loaded, `findByUsername("admin")` (or whatever the seeded username is) returns the seeded row.
  - Dependencies: 4.2, 0.3, 0.6
  - Files: `src/main/java/com/hotel/dao/JdbcStaffDao.java`
- [ ] 4.4 `CurrentUserContext`
  - Acceptance: Spring-managed singleton bean holding the currently logged-in `Staff` (nullable/empty when logged out); simple get/set/clear API.
  - Verify: compiles; a scratch test sets then clears the context and confirms state.
  - Dependencies: 4.1, 0.1
  - Files: `src/main/java/com/hotel/service/CurrentUserContext.java`
- [ ] 4.5 `AuthService`
  - Acceptance: `login(username, password)` looks up via `StaffDao.findByUsername`, verifies with `BCryptPasswordEncoder.matches`, populates `CurrentUserContext` on success; `logout()` clears it.
  - Verify: login with the seeded admin's real password succeeds; a wrong password fails and `CurrentUserContext` stays empty.
  - Dependencies: 4.3, 4.4
  - Files: `src/main/java/com/hotel/service/AuthService.java`
- [ ] 4.6 `StaffService`
  - Acceptance: admin-facing CRUD over staff accounts; hashes new/changed passwords with `BCryptPasswordEncoder` before saving; supports deactivate (flip `active`) instead of hard delete.
  - Verify: create a new staff account via the service, then log in as that account through `AuthService`.
  - Dependencies: 4.3
  - Files: `src/main/java/com/hotel/service/StaffService.java`
**Swing note:** read `tasks/todo/swing-guide.md` first. `StaffPanel` is another "list + search" + "form dialog" pair (sections 1-2) — same shape as `GuestPanel`/`GuestFormDialog` in Module 2, just with different fields (and a "Deactivate" action instead of delete). `LoginFrame` is the one screen in the whole app that's a plain `JFrame`, not a panel registered into `MainFrame`'s `CardLayout` — it's shown before `MainFrame` even exists.

- [ ] 4.7 `LoginFrame` UI
  - Acceptance: standalone `JFrame` (not a card) with a centered form: `JLabel("Username")` + `JTextField`, `JLabel("Password")` + `JPasswordField` (use `JPasswordField`, not `JTextField`, so input is masked), a "Login" `JButton`, laid out with `GridLayout(3,2,8,8)` or `GridBagLayout` inside a padded outer panel. Pressing Enter in the password field should also trigger login (`passwordField.addActionListener(loginAction)` — `JPasswordField` fires an action event on Enter, same listener as the button). On click: call `authService.login(username, new String(passwordField.getPassword()))`; on success, `dispose()` this frame and construct/show `MainFrame` with all module panels registered; on failure, `Dialogs.showError(this, "Invalid username or password")` and stay open with the password field cleared. This is the very first window `HotelBookingApplication` shows.
  - Verify: run the app — login screen appears first, no other window; wrong credentials show an error dialog and the login screen stays open with password cleared; correct credentials close the login window and open `MainFrame`.
  - Dependencies: 4.5, 0.8, 0.9
  - Files: `src/main/java/com/hotel/ui/login/LoginFrame.java`
- [ ] 4.8 `StaffPanel` UI (admin-only)
  - Acceptance: same "list + search" shape as `GuestPanel` (guide section 1) — `BorderLayout`; `NORTH`: "Add Staff" button (search is optional, staff lists are small); `CENTER`: `JScrollPane`-wrapped `JTable` via `BaseTableModel<Staff>` with columns Username / Full Name / Role / Active. Add/edit via a `StaffFormDialog` (guide section 2 shape) with `JTextField` username/full name, a `JComboBox<Role>` for role, and — only when creating, not editing — a `JPasswordField` for the initial password (edit mode changes name/role/active only; add a separate "Reset Password" button/dialog if a password change is needed later, don't overload the edit form). "Deactivate" is a button on the selected row (not a delete) calling `staffService` to flip `active` to false, guarded by `Dialogs.showConfirm`. This panel itself doesn't need to check `CurrentUserContext.role` — Task 6.1 controls whether it's reachable at all via the sidebar.
  - Verify: as admin, create a staff account, edit it, deactivate it, confirm each persists in the table and after app restart; log in as the newly created account via `LoginFrame` before it's deactivated, then confirm login fails via `AuthService` after deactivation.
  - Dependencies: 4.6, 0.7, 0.8
  - Files: `src/main/java/com/hotel/ui/staff/StaffPanel.java`, `src/main/java/com/hotel/ui/staff/StaffFormDialog.java`

**Exposes to other modules:** `CurrentUserContext` (used by Task 6 for role-based menu visibility; used by Module 3 to stamp `staff_id` on a reservation).
