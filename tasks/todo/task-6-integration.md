# Task 6 — Integration

Owner: whoever finishes first. Runs after all 5 modules land. See `tasks/plan.md` for the full Verification checklist.

**Swing note:** see `tasks/todo/swing-guide.md` section 6 for the `MainFrame`/`CardLayout` shape these tasks build on.

- [ ] 6.1 Wire role-based menu visibility in `MainFrame` (Admin: Staff + Reports visible; Receptionist: hidden)
  - Acceptance: `MainFrame` takes/reads `CurrentUserContext` at construction time (i.e. it's built *after* login succeeds, per 6.2) and calls `.setVisible(role == Role.ADMIN)` on the Staff and Reports sidebar `JButton`s — keep a field reference to those two buttons specifically (not just a generic list) so this is a one-line toggle each, not a loop with a lookup table.
  - Verify: log in as the seeded admin, confirm Staff + Reports buttons are visible; log in as a receptionist account (create one via 4.8 first), confirm those two buttons are gone from the sidebar entirely (not just disabled/greyed).
  - Dependencies: 0.9 (Task 0), 4.5 (Module 4), 4.8 (Module 4), 5.10 (Module 5)
  - Files: `src/main/java/com/hotel/ui/MainFrame.java`
- [ ] 6.2 Wire login → MainFrame → logout flow end-to-end
  - Acceptance: `HotelBookingApplication`'s Swing startup shows `LoginFrame` first (not `MainFrame`); `LoginFrame`'s on-success handler constructs `MainFrame` (registering every module's panel via `addCard`, per guide section 6), disposes the `LoginFrame`, and shows `MainFrame`. `MainFrame` gets a "Logout" `JButton` (bottom of the sidebar) whose listener calls `authService.logout()` (clearing `CurrentUserContext`), disposes `MainFrame`, and constructs/shows a fresh `LoginFrame` — don't try to reuse/reset the old `LoginFrame` instance, a new one is simpler and avoids stale field state.
  - Verify: login → navigate a couple of module panels → click Logout → confirm you land back on a blank login screen and `CurrentUserContext` is empty (e.g. attempting an admin-only action would fail); log back in and confirm it still works (no leftover state from the previous session breaks the second login).
  - Dependencies: 6.1
  - Files: `src/main/java/com/hotel/ui/login/LoginFrame.java`, `src/main/java/com/hotel/ui/MainFrame.java`, `src/main/java/com/hotel/HotelBookingApplication.java`
- [ ] 6.3 Full manual verification pass against `SPEC.md` Success Criteria (see `tasks/plan.md` Verification section, steps 1-8)
  - Verify: walk all 8 steps end-to-end on a clean schema.
  - Dependencies: 6.2, all modules
  - Files: none (manual QA pass)
- [ ] 6.4 Confirm `SPEC.md` still matches what was built
  - Verify: re-read `SPEC.md` Assumptions/Success Criteria against the final app; update only if something diverged during implementation.
  - Dependencies: 6.3
  - Files: `SPEC.md` (only if a divergence is found)
