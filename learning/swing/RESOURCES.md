# Swing Resources

## Knowledge

- [Trail: Creating a GUI With Swing (The Java Tutorials)](https://docs.oracle.com/javase/tutorial/uiswing/index.html)
  Oracle's official Swing trail, the primary source for this whole mission. Use for: orientation, and as the top-level table of contents to link every other page below.
- [Getting Started with Swing](https://docs.oracle.com/javase/tutorial/uiswing/start/index.html)
  How a Swing program compiles/runs, the minimal `JFrame` shape. Use for: Lesson 1 (first window).
- [The Event Dispatch Thread (Concurrency in Swing)](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html)
  Explains why Swing calls must happen on the EDT and what `invokeLater`/`invokeAndWait` do. Use for: Lesson 1, and for justifying `swing-guide.md`'s "ground rules" section 0.
- [Initial Threads](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html)
  Why app startup wraps GUI creation in `SwingUtilities.invokeLater`. Use for: explaining `HotelBookingApplication`'s startup shape (task 0.2).
- [Lesson: Laying Out Components Within a Container](https://docs.oracle.com/javase/tutorial/uiswing/layout/index.html)
  Index for all layout-manager pages. Use for: any layout question.
- [A Visual Guide to Layout Managers](https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html)
  Side-by-side visual comparison of every layout manager. Use for: picking the right layout for a given panel region.
- [How to Use Various Layout Managers](https://docs.oracle.com/javase/tutorial/uiswing/layout/layoutlist.html)
  One how-to page per manager (`BorderLayout`, `FlowLayout`, `GridLayout`, `BoxLayout`, `GridBagLayout`, ...). Use for: the specific manager a panel needs.
- [How to Use CardLayout](https://docs.oracle.com/javase/tutorial/uiswing/layout/card.html)
  Official reference for the exact navigation pattern `MainFrame` uses (task 0.9 / `swing-guide.md` section 6). Use for: the "screen switching" lesson.
- [How to Use Tables](https://docs.oracle.com/javase/tutorial/uiswing/components/table.html)
  `JTable` usage, including custom table models. Use for: the "list + search panel" lesson (every CRUD screen in the project).
- [AbstractTableModel (Java SE 21 API)](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/table/AbstractTableModel.html)
  Exact contract (`getRowCount`, `getColumnCount`, `getValueAt`, `fireTableDataChanged`) that `BaseTableModel<T>` (task 0.7) implements. Use for: writing/debugging `BaseTableModel`.
- [JTable (Java SE 21 API)](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/JTable.html)
  Full `JTable` API reference. Use for: selection handling, renderers, column sizing questions.
- [How to Make Dialogs](https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html)
  `JOptionPane`/`JDialog` usage, modal vs non-modal. Use for: the "form dialog" lesson underlying `Dialogs.showForm`/`showError`/`showConfirm` (task 0.8).
- [How to Use Combo Boxes](https://docs.oracle.com/javase/tutorial/uiswing/components/combobox.html)
  `JComboBox` basics, action events, custom rendering. Use for: picking an existing entity (room type, guest, room) in forms — `swing-guide.md` section 4.
- [How to Use Spinners](https://docs.oracle.com/javase/tutorial/uiswing/components/spinner.html)
  `JSpinner`, including `SpinnerDateModel` + `JSpinner.DateEditor`. Use for: the date-picker lesson (`swing-guide.md` section 3) — check-in/check-out fields, report date ranges.
- [How to Write an Action Listener](https://docs.oracle.com/javase/tutorial/uiswing/events/actionlistener.html)
  Canonical `ActionListener` pattern (buttons, text fields, combo boxes). Use for: every "wire this button" step across all module tasks.
- [General Information about Writing Event Listeners](https://docs.oracle.com/javase/tutorial/uiswing/events/generalrules.html)
  General rules for listener interfaces (the same shape applies to `MouseListener`, `ListSelectionListener`, etc.). Use for: `ActiveReservationsPanel`'s selection-based button enabling (task 3.7), double-click-to-edit rows.
- [How to Use Text Fields](https://docs.oracle.com/javase/tutorial/uiswing/components/textfield.html)
  `JTextField` basics; the same page family covers `JPasswordField`. Use for: every form field, and `LoginFrame`'s password field (task 4.7).

## Wisdom (Communities)

- [r/learnjava](https://reddit.com/r/learnjava)
  Active, beginner-friendly, reasonably well-moderated. Use for: "is this the idiomatic way to do X in Swing" sanity checks once a teammate has working code and wants a second opinion.
- Stack Overflow, `[java]` + `[swing]` tags
  Not a "community" to join, but the highest-density source of real Swing bugs (`NoClassDefFoundError`-style layout mistakes, EDT violations, table-not-repainting issues) with vetted answers. Use for: debugging a specific stack trace or "my JTable won't show data" symptom.

No community preference stated by the user yet — revisit if/when the team hits a wall a doc page can't answer.

## Gaps
- No resource yet specifically on testing/verifying Swing UI manually in a structured way — the project has no automated UI tests (per `tasks/plan.md`, verification is manual). If manual-QA-for-Swing becomes a recurring pain point, look for one.
