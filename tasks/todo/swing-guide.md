# Swing Cookbook (read this before starting any UI task)

None of us have built a Swing app before, so this collects the handful of patterns every panel in this project reuses. Every UI task below links back here instead of re-explaining these. Copy these shapes, don't reinvent them.

## 0. Ground rules
- **Never touch a Swing component from outside the Event Dispatch Thread (EDT).** In practice: everything happens inside button/menu callbacks, which already run on the EDT, so as long as you don't spawn your own `Thread`/`ExecutorService`, you're fine. The only place we explicitly jump onto the EDT is app startup (`SwingUtilities.invokeLater(...)` in `HotelBookingApplication`).
- **DB calls happen synchronously, directly inside the button handler.** For a local MySQL instance this is fast enough that the UI won't visibly freeze. Don't build async/background-thread plumbing — it's out of scope and adds Swing threading bugs we don't want to debug.
- **Every `JTable` must be wrapped in a `JScrollPane`.** A bare `JTable` added straight to a panel won't even show its header row. This is the #1 "why is my table broken" bug.
- **A panel is just a `JPanel` subclass.** Build its children in the constructor, lay them out with a `LayoutManager`, and expose one or two public methods (e.g. `refresh()`) that other code calls after a save/delete.
- **Prefer `BorderLayout` for the outer skeleton of every screen**, then nest simpler layouts inside each region. Don't reach for `GridBagLayout` unless a form genuinely needs a label/field grid — `GridLayout`/`BoxLayout`/`FlowLayout` cover most of what we need.

## 1. The "list + search" panel shape
Used by: Rooms, Guests, Staff. Skeleton:

```java
public class GuestPanel extends JPanel {
    private final GuestService guestService;
    private final BaseTableModel<Guest> tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(20);

    public GuestPanel(GuestService guestService) {
        this.guestService = guestService;
        setLayout(new BorderLayout());

        // NORTH: search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Search");
        JButton addBtn = new JButton("Add Guest");
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(addBtn);
        add(searchBar, BorderLayout.NORTH);

        // CENTER: table, ALWAYS inside a JScrollPane
        tableModel = new BaseTableModel<>(
            List.of("Name", "Phone", "Email"),
            List.of(Guest::getFullName, Guest::getPhone, Guest::getEmail)
        );
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> refresh());
        addBtn.addActionListener(e -> {
            if (GuestFormDialog.showCreate(this, guestService)) refresh();
        });
        // double-click a row -> edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Guest selected = tableModel.getRowAt(table.getSelectedRow());
                    if (GuestFormDialog.showEdit(GuestPanel.this, guestService, selected)) refresh();
                }
            }
        });

        refresh();
    }

    public void refresh() {
        String query = searchField.getText();
        tableModel.setRows(query.isBlank() ? guestService.findAll() : guestService.search(query));
    }
}
```

The pattern for every list panel: constructor wires layout + listeners once, `refresh()` re-pulls data and calls `tableModel.setRows(...)`, and every mutation (add/edit/delete/status-change) ends by calling `refresh()` so the table never goes stale.

## 2. The "form dialog" shape
Used by: every add/edit screen (guests, rooms, staff, reservations, payments). Build these on top of `Dialogs` (task 0.8), which should expose something like:

```java
public class Dialogs {
    // returns true if the user clicked OK, false if Cancel/closed
    public static boolean showForm(Component parent, String title, LinkedHashMap<String, JComponent> fields) {
        JPanel panel = new JPanel(new GridLayout(fields.size(), 2, 8, 8));
        for (var entry : fields.entrySet()) {
            panel.add(new JLabel(entry.getKey()));
            panel.add(entry.getValue());
        }
        int result = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
```

A feature-specific dialog (e.g. `GuestFormDialog`) is a thin static helper that builds the `LinkedHashMap<String, JComponent>` of fields, calls `Dialogs.showForm`, reads the field values back out, calls the service, and catches/reports validation errors with `Dialogs.showError`:

```java
public class GuestFormDialog {
    public static boolean showCreate(Component parent, GuestService service) {
        return show(parent, service, null);
    }
    public static boolean showEdit(Component parent, GuestService service, Guest existing) {
        return show(parent, service, existing);
    }

    private static boolean show(Component parent, GuestService service, Guest existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.getFullName());
        JTextField phone = new JTextField(existing == null ? "" : existing.getPhone());
        JTextField email = new JTextField(existing == null ? "" : existing.getEmail());

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Full name", name);
        fields.put("Phone", phone);
        fields.put("Email", email);

        boolean ok = Dialogs.showForm(parent, existing == null ? "Add Guest" : "Edit Guest", fields);
        if (!ok) return false;

        try {
            Guest guest = existing == null ? new Guest() : existing;
            guest.setFullName(name.getText());
            guest.setPhone(phone.getText());
            guest.setEmail(email.getText());
            service.save(guest);
            return true;
        } catch (IllegalArgumentException ex) {
            Dialogs.showError(parent, ex.getMessage());
            return false;
        }
    }
}
```

Every add/edit dialog in the project follows this exact shape: build fields pre-filled from `existing` (or blank if `null`), show the form, on OK build/mutate the model object, call the service inside a `try/catch` that surfaces validation errors via `Dialogs.showError`, return whether it saved so the caller knows to `refresh()`.

## 3. Picking a date (check-in / check-out, report ranges)
Core Swing has no calendar widget, and we're not adding a new dependency for one. Use `JSpinner` with a `SpinnerDateModel` — it's built in, gives arrow-key/spinner increment for free, and is trivial to convert to `LocalDate`:

```java
JSpinner checkInSpinner = new JSpinner(new SpinnerDateModel());
checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
checkInSpinner.setValue(new Date()); // default to today

// reading it back as a LocalDate:
Date raw = (Date) checkInSpinner.getValue();
LocalDate checkIn = raw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
```

Use this same spinner-based date field everywhere a `LocalDate` is entered: booking search, create-reservation form, report date-range pickers.

## 4. `JComboBox` for picking an existing entity (room type, guest, room)
Give the combo box a small wrapper object (or override `toString()` on the model class) so the dropdown shows something readable instead of `Guest@6bc7c054`:

```java
JComboBox<RoomType> roomTypeCombo = new JComboBox<>(roomTypes.toArray(new RoomType[0]));
// RoomType.toString() should return roomType.getName() for this to read well
RoomType selected = (RoomType) roomTypeCombo.getSelectedItem();
```

If you don't want to override `toString()` on the model class itself, use `combo.setRenderer(new DefaultListCellRenderer() { ... })` — but overriding `toString()` is simpler and fine for this project.

## 5. `BaseTableModel<T>` contract (implement this once in task 0.7, reuse everywhere)
```java
public class BaseTableModel<T> extends AbstractTableModel {
    private final List<String> columnNames;
    private final List<Function<T, Object>> columnExtractors;
    private List<T> rows = new ArrayList<>();

    public BaseTableModel(List<String> columnNames, List<Function<T, Object>> columnExtractors) {
        this.columnNames = columnNames;
        this.columnExtractors = columnExtractors;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
        fireTableDataChanged(); // without this the JTable won't repaint
    }

    public T getRowAt(int rowIndex) { return rows.get(rowIndex); }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columnNames.size(); }
    @Override public String getColumnName(int col) { return columnNames.get(col); }
    @Override public Object getValueAt(int row, int col) { return columnExtractors.get(col).apply(rows.get(row)); }
}
```
Every list panel constructs one of these with its own column names + extractor lambdas — no per-module table model subclassing needed.

## 6. `MainFrame` / `CardLayout` navigation
```java
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    public MainFrame() {
        setTitle("Hotel Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JButton roomsBtn = new JButton("Rooms");
        roomsBtn.addActionListener(e -> cardLayout.show(content, "rooms"));
        sidebar.add(roomsBtn);
        // ... one button per module, add/remove visibility in Task 6

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    public void addCard(String name, JPanel panel) {
        content.add(panel, name);
    }

    public void showCard(String name) {
        cardLayout.show(content, name);
    }
}
```
Each module's top-level panel gets registered once via `mainFrame.addCard("rooms", roomTypePanel)`; sidebar buttons just call `showCard(...)`.

## 7. Checklist before calling a UI task "done"
- [ ] Every `JTable` is wrapped in `JScrollPane`.
- [ ] Every button has an `addActionListener` — a button that does nothing is a silent bug, not a TODO.
- [ ] Every save/delete/status-change ends by calling the panel's `refresh()`.
- [ ] Every service call that can throw a validation error is wrapped in `try/catch` showing `Dialogs.showError` — the app should never show a raw stack trace to the user.
- [ ] Dates go through `JSpinner` + `SpinnerDateModel` (section 3), not free-text fields the user could mistype.
