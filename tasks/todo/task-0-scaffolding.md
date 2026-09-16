# Task 0 — Shared Scaffolding

Owner: whoever starts first; blocks everyone else. See `tasks/plan.md` for full context and DDL. Sequence top-to-bottom — later tasks in this file need the ones above them.

- [ ] 0.1 `pom.xml`
  - Acceptance: Java 21, Spring Boot 3.3.x parent, `spring-boot-starter`, `spring-boot-starter-jdbc`, `spring-security-crypto`, `mysql-connector-j` declared.
  - Verify: `mvn -q compile` resolves dependencies with no errors (no source files needed yet — an empty `src/main/java` is fine at this point).
  - Dependencies: None
  - Files: `pom.xml`
- [ ] 0.2 `HotelBookingApplication.java` entrypoint
  - Acceptance: boots the Spring `ApplicationContext`, then launches Swing via `SwingUtilities.invokeLater` on the EDT; no web server starts.
  - Verify: `mvn spring-boot:run` starts and doesn't crash (UI can be a blank/no-op frame until 0.3 lands).
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/HotelBookingApplication.java`
- [ ] 0.3 `DataSourceConfig`
  - Acceptance: `@Configuration` class exposing `DataSource` and `JdbcTemplate` beans, reading connection info from properties.
  - Verify: app context starts with a real local MySQL running and no bean-creation errors.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/config/DataSourceConfig.java`
- [ ] 0.4 `application.properties` + local override + gitignore
  - Acceptance: `application.properties` has placeholder/blank DB creds; `application-local.properties.example` documents the real keys to fill in; `.gitignore` excludes `application-local.properties`.
  - Verify: copying the example to `application-local.properties` with real creds lets 0.3's beans connect.
  - Dependencies: 0.3
  - Files: `src/main/resources/application.properties`, `application-local.properties.example`, `.gitignore`
- [ ] 0.5 `schema.sql`
  - Acceptance: creates all 7 tables (`room_types`, `rooms`, `guests`, `staff`, `reservations`, `invoices`, `payments`) with FKs, the `CHECK (check_out > check_in)` constraint, and the `idx_reservations_room_dates` index, exactly per `tasks/plan.md` DDL.
  - Verify: `mysql < schema.sql` against a clean local MySQL 8 database runs with no errors; `SHOW TABLES` lists all 7.
  - Dependencies: None (can be done alongside 0.1-0.4)
  - Files: `src/main/resources/schema.sql`
- [ ] 0.6 `data.sql`
  - Acceptance: seeds 3-4 room types, ~10 rooms (mapped to those types), and one `ADMIN` staff row whose `password_hash` is a real BCrypt hash (generate it with `BCryptPasswordEncoder` once and paste the hash — don't seed plaintext).
  - Verify: `mysql < data.sql` after 0.5 runs with no FK errors; `SELECT * FROM staff` shows the admin row with a `$2a$...` hash.
  - Dependencies: 0.5
  - Files: `src/main/resources/data.sql`
**Swing note:** none of us have used Swing before — read `tasks/todo/swing-guide.md` in full before starting 0.7-0.9. It has copy-paste-able code for every pattern below (generic table model, form dialog helper, `CardLayout` navigation). Every other module's UI tasks build directly on top of what you create here, so match the shapes in the guide exactly — deviating here means every downstream panel has to work around it.

- [ ] 0.7 `ui/common/BaseTableModel<T>`
  - Acceptance: generic `AbstractTableModel` per guide section 5 — constructor takes `List<String> columnNames` + `List<Function<T,Object>> columnExtractors`; has `setRows(List<T>)` (calls `fireTableDataChanged()` — easy to forget, and without it the table silently never updates), `getRowAt(int)` (so a panel can map a selected table row back to the domain object for edit/delete), and overrides `getRowCount`, `getColumnCount`, `getColumnName`, `getValueAt`.
  - Verify: compiles; a throwaway `main` builds a `JFrame` containing a `JTable` (wrapped in `JScrollPane` — a bare `JTable` won't show its header) backed by this model with 2-3 dummy rows, and the rows render.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/ui/common/BaseTableModel.java`
- [ ] 0.8 `ui/common/Dialogs`
  - Acceptance: static helpers per guide section 2 — `showForm(Component parent, String title, LinkedHashMap<String,JComponent> fields)` lays the map out as label/field rows (`GridLayout(fields.size(), 2, ...)`) inside `JOptionPane.showConfirmDialog(..., OK_CANCEL_OPTION)` and returns `true` iff OK was clicked; `showError(Component parent, String message)` wraps `JOptionPane.showMessageDialog(..., ERROR_MESSAGE)`; `showConfirm(Component parent, String message)` wraps a YES_NO confirm dialog and returns a boolean. Every feature-specific form dialog (guest, room, staff, reservation, payment) is built on top of `showForm` — don't let any module hand-roll its own `JOptionPane` call.
  - Verify: compiles; manually trigger all three helpers from a scratch `main` (a form with 2 `JTextField`s, an error dialog, a confirm dialog) and confirm they display and return the right boolean/value for OK vs Cancel.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/ui/common/Dialogs.java`
- [ ] 0.9 `ui/MainFrame`
  - Acceptance: `JFrame` per guide section 6 — `BorderLayout` at the top level; `WEST` holds a sidebar (`JPanel` with `BoxLayout(Y_AXIS)`) with one `JButton` per module (Rooms, Guests, Booking, Staff, Billing, Reports — even though some are placeholders until their module lands); `CENTER` holds a `JPanel` using `CardLayout`. Expose `addCard(String name, JPanel panel)` (registers a module's top-level panel) and `showCard(String name)` (called by sidebar button listeners). No visibility/role logic yet — every button is shown regardless of role until Task 6.1 wires that in.
  - Verify: manually register two placeholder `JPanel`s (each just a `JLabel` with different text) via `addCard`, wire two sidebar buttons to `showCard` each, run it, and confirm clicking each button swaps the visible panel.
  - Dependencies: 0.2
  - Files: `src/main/java/com/hotel/ui/MainFrame.java`
