# Task 6 — Integration

Owner: whoever finishes first. Runs after all 5 modules land. See `tasks/plan.md` for the full Verification checklist.

**Swing note:** see `tasks/todo/swing-guide.md` section 6 for the `MainFrame`/`CardLayout` shape these tasks build on.

- [ ] 6.1 Wire role-based menu visibility in `MainFrame` (Admin: Staff + Reports visible; Receptionist: hidden)
  - Acceptance: `MainFrame` takes/reads `CurrentUserContext` at construction time (i.e. it's built *after* login succeeds, per 6.2) and calls `.setVisible(role == Role.ADMIN)` on the Staff and Reports sidebar `JButton`s — keep a field reference to those two buttons specifically (not just a generic list) so this is a one-line toggle each, not a loop with a lookup table.
  - Steps:
    - [ ] Add a `CurrentUserContext` (or `Staff`/`Role`) parameter to `MainFrame`'s constructor so the logged-in user's role is known at build time.
    - [ ] Change the two Staff/Reports sidebar `JButton` locals to instance fields (e.g. `staffButton`, `reportsButton`) so they're reachable after construction.
    - [ ] Read the role off the passed-in context (e.g. `context.getStaff().getRole()`) once, into a local `Role role` variable.
    - [ ] Call `staffButton.setVisible(role == Role.ADMIN)`.
    - [ ] Call `reportsButton.setVisible(role == Role.ADMIN)`.
  - Verify: log in as the seeded admin, confirm Staff + Reports buttons are visible; log in as a receptionist account (create one via 4.8 first), confirm those two buttons are gone from the sidebar entirely (not just disabled/greyed).
  - Dependencies: 0.9 (Task 0), 4.5 (Module 4), 4.8 (Module 4), 5.10 (Module 5)
  - Files: `src/main/java/com/hotel/ui/MainFrame.java`
- [ ] 6.2 Wire login → MainFrame → logout flow end-to-end
  - Acceptance: `HotelBookingApplication`'s Swing startup shows `LoginFrame` first (not `MainFrame`); `LoginFrame`'s on-success handler constructs `MainFrame` (registering every module's panel via `addCard`, per guide section 6), disposes the `LoginFrame`, and shows `MainFrame`. `MainFrame` gets a "Logout" `JButton` (bottom of the sidebar) whose listener calls `authService.logout()` (clearing `CurrentUserContext`), disposes `MainFrame`, and constructs/shows a fresh `LoginFrame` — don't try to reuse/reset the old `LoginFrame` instance, a new one is simpler and avoids stale field state.
  - Steps:
    - [ ] Change `HotelBookingApplication`'s `SwingUtilities.invokeLater` block to construct and show `LoginFrame` (not `MainFrame`) as the first window.
    - [ ] In `LoginFrame`'s on-success handler (after `authService.login(...)` succeeds), construct a new `MainFrame`, passing the now-populated `CurrentUserContext` (needed by 6.1).
    - [ ] Register every module's top-level panel on the new `MainFrame` via `addCard("rooms", roomTypePanel)` / `addCard("guests", guestPanel)` / etc. for all six cards (Rooms, Guests, Booking, Staff, Billing, Reports).
    - [ ] Call `loginFrame.dispose()` and `mainFrame.setVisible(true)` in that same on-success handler.
    - [ ] Add a "Logout" `JButton` to the bottom of `MainFrame`'s sidebar (below the module buttons).
    - [ ] Wire the Logout button's listener to call `authService.logout()`.
    - [ ] In that same listener, call `mainFrame.dispose()`.
    - [ ] In that same listener, construct a brand-new `LoginFrame` instance and `setVisible(true)` on it.
  - Verify: login → navigate a couple of module panels → click Logout → confirm you land back on a blank login screen and `CurrentUserContext` is empty (e.g. attempting an admin-only action would fail); log back in and confirm it still works (no leftover state from the previous session breaks the second login).
  - Dependencies: 6.1
  - Files: `src/main/java/com/hotel/ui/login/LoginFrame.java`, `src/main/java/com/hotel/ui/MainFrame.java`, `src/main/java/com/hotel/HotelBookingApplication.java`
- [ ] 6.3 Full manual verification pass against `SPEC.md` Success Criteria (see `tasks/plan.md` Verification section, steps 1-8)
  - Steps:
    - [ ] Load a clean schema: `mysql < schema.sql`, `mysql < data.sql` against a local MySQL 8 instance; set real credentials in `application-local.properties`.
    - [ ] Run `mvn spring-boot:run` and confirm the app opens to the login screen (not any other screen).
    - [ ] Log in as the seeded admin → confirm Staff management + Reports are visible; log in as a receptionist account → confirm they're hidden.
    - [ ] As receptionist: search available rooms for a date range, create a reservation, confirm a second overlapping booking for the same room is rejected.
    - [ ] Check the guest in, then check out → confirm an invoice is generated with the correct total (nights × rate) and room status flips back to AVAILABLE.
    - [ ] Record a payment against the invoice.
    - [ ] As admin: view occupancy report and revenue-by-period report and confirm numbers match the bookings created in the steps above.
    - [ ] Confirm all 5 modules' screens are reachable from one running app against the one schema (no separate mini-apps).
  - Verify: walk all 8 steps end-to-end on a clean schema.
  - Dependencies: 6.2, all modules
  - Files: none (manual QA pass)
- [ ] 6.4 Confirm `SPEC.md` still matches what was built
  - Steps:
    - [ ] Re-read `SPEC.md`'s Assumptions section against the final app's actual behavior.
    - [ ] Re-read `SPEC.md`'s Success Criteria section against the final app's actual behavior.
    - [ ] Update only the sections/lines where something diverged during implementation; leave the rest untouched.
  - Verify: re-read `SPEC.md` Assumptions/Success Criteria against the final app; update only if something diverged during implementation.
  - Dependencies: 6.3
  - Files: `SPEC.md` (only if a divergence is found)
