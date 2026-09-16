# No prior Swing experience on the team

The user stated the team is "not good at Swing" when asking for more detail on the UI tasks in `tasks/todo/*.md`, and confirmed there's no existing Swing knowledge to build on. This sets the floor for lessons at true beginner level — start from `JFrame`/EDT basics (Lesson 1), don't assume familiarity with even the most common components (`JTable`, `JOptionPane`, layout managers).

## Implications
- Lessons should map directly onto concrete tasks in `tasks/todo/*.md`/`tasks/todo/swing-guide.md` rather than teaching Swing in the abstract — see [[MISSION.md]].
- Sequence lessons in dependency order matching the project's own task order: `JFrame`/EDT → layouts/`JPanel` composition → `JTable`+`AbstractTableModel` → dialogs/`JOptionPane` → `JComboBox`/`JSpinner` → `CardLayout` navigation. Lesson 1 (`lessons/0001-cua-so-swing-dau-tien.html`) covers the first of these.
