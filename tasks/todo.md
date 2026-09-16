# Task List: Hotel Booking System

See `tasks/plan.md` for full context, DDL, and rationale. Each module's tasks now live in their own file under `tasks/todo/` so the 5 teammates can work without touching a shared file. Tasks within each file are split roughly one-class-per-task and sequenced top-to-bottom.

**New to Swing?** Read [`tasks/todo/swing-guide.md`](todo/swing-guide.md) before starting any UI task (0.7-0.9, and every panel/dialog task in Modules 1-5). It has copy-paste-able patterns (list+search panels, form dialogs, date pickers, table models, `CardLayout` navigation) that every UI task below now explicitly references instead of re-explaining.

## Order
1. [`tasks/todo/task-0-scaffolding.md`](todo/task-0-scaffolding.md) — do first, blocks everyone else.
2. In parallel once Task 0 lands:
   - [`tasks/todo/module-1-rooms.md`](todo/module-1-rooms.md) — Rooms & Room Types (teammate A)
   - [`tasks/todo/module-2-guests.md`](todo/module-2-guests.md) — Guests (teammate B)
   - [`tasks/todo/module-4-staff-auth.md`](todo/module-4-staff-auth.md) — Staff & Auth (teammate C)
3. [`tasks/todo/module-3-booking.md`](todo/module-3-booking.md) — Reservations/Booking (teammate D) — depends on Modules 1 & 2.
4. [`tasks/todo/module-5-billing-reports.md`](todo/module-5-billing-reports.md) — Invoicing, Payments & Reports (teammate E) — depends on Module 3.
5. [`tasks/todo/task-6-integration.md`](todo/task-6-integration.md) — after all 5 modules land.
