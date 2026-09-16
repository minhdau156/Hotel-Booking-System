# Module 5 — Invoicing, Payments & Reports

Owner: teammate E. Depends on Module 3 (`tasks/todo/module-3-booking.md`, specifically `ReservationService`). See `tasks/plan.md` for full context and DDL. Sequence top-to-bottom.

- [ ] 5.1 `Invoice` model
  - Acceptance: POJO with `id, reservationId, totalAmount, issuedAt`.
  - Verify: compiles.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/model/Invoice.java`
- [ ] 5.2 `Payment` model
  - Acceptance: POJO with `id, invoiceId, amount, method, paidAt` (`method` as a `PaymentMethod` enum: CASH/CARD/TRANSFER).
  - Verify: compiles.
  - Dependencies: 0.1
  - Files: `src/main/java/com/hotel/model/Payment.java`, `src/main/java/com/hotel/model/PaymentMethod.java`
- [ ] 5.3 `InvoiceDao` interface + `JdbcInvoiceDao` impl
  - Acceptance: CRUD + `Optional<Invoice> findByReservationId(long)`.
  - Verify: manually insert an invoice row, confirm `findByReservationId` returns it.
  - Dependencies: 5.1, 0.3
  - Files: `src/main/java/com/hotel/dao/InvoiceDao.java`, `src/main/java/com/hotel/dao/JdbcInvoiceDao.java`
- [ ] 5.4 `PaymentDao` interface + `JdbcPaymentDao` impl
  - Acceptance: CRUD + `List<Payment> findByInvoiceId(long)`.
  - Verify: manually insert a payment row, confirm it's returned for its invoice.
  - Dependencies: 5.2, 0.3
  - Files: `src/main/java/com/hotel/dao/PaymentDao.java`, `src/main/java/com/hotel/dao/JdbcPaymentDao.java`
- [ ] 5.5 `BillingService` — generate invoice on checkout
  - Acceptance: `@Transactional` method, called from checkout flow (hooks into Module 3's 3.5), computes `total = nights × room_type.base_price` and inserts the invoice.
  - Verify: check a reservation out, confirm an invoice exists with `total_amount` matching a manual nights × rate calculation.
  - Dependencies: 5.3, 3.5 (Module 3), 1.7 (Module 1's `RoomService`)
  - Files: `src/main/java/com/hotel/service/BillingService.java`
- [ ] 5.6 `BillingService` — record payment
  - Acceptance: `@Transactional` method to record a payment against an existing invoice.
  - Verify: record a payment, confirm `payments.invoice_id` links correctly and the amount is stored as entered.
  - Dependencies: 5.5, 5.4
  - Files: `src/main/java/com/hotel/service/BillingService.java` (same file as 5.5, additional method)
- [ ] 5.7 `ReportService` — occupancy report
  - Acceptance: raw SQL aggregate query returning occupancy counts/rates for a given date range.
  - Verify: run against seeded + test reservation data, confirm numbers match a manual count.
  - Dependencies: 0.3, 3.3 (Module 3)
  - Files: `src/main/java/com/hotel/service/ReportService.java`
- [ ] 5.8 `ReportService` — revenue-by-period report
  - Acceptance: raw SQL aggregate query (`GROUP BY`/`SUM`/`JOIN` across invoices/reservations) returning revenue bucketed by period.
  - Verify: run against test invoice data, confirm totals match a manual sum.
  - Dependencies: 5.7, 5.5
  - Files: `src/main/java/com/hotel/service/ReportService.java` (same file as 5.7, additional method)
**Swing note:** read `tasks/todo/swing-guide.md` first, especially section 3 for the report date-range pickers. Neither panel here is a "list + search of editable rows" like Modules 1/2/4 — `BillingPanel` is "pick a checked-out reservation, view its invoice, record a payment against it" and `ReportsPanel` is "pick a date range, show read-only aggregate numbers" (no add/edit dialogs at all).

- [ ] 5.9 `BillingPanel` UI
  - Acceptance: `BorderLayout` panel. `NORTH`: `JScrollPane`-wrapped `JTable` (`BaseTableModel<ReservationView>`, reusing Module 3's joined-listing row shape) listing CHECKED_OUT reservations — selecting a row loads its invoice. `CENTER`: a small read-only summary area (a few `JLabel`s, or a 1-row non-editable `JTable`) showing the selected invoice's total amount, issued date, and a `JScrollPane`-wrapped `JTable` of its payments (`BaseTableModel<Payment>`, columns Amount / Method / Paid At) via `paymentDao.findByInvoiceId`/`billingService`. `SOUTH`: "Record Payment" button opens a small `Dialogs.showForm` dialog with a `JTextField`/`JSpinner(SpinnerNumberModel)` for amount and a `JComboBox<PaymentMethod>` for method; on OK calls `billingService`'s record-payment method, then refreshes the payments table.
  - Verify: after a checkout (3.5/5.5), select that reservation here, confirm the invoice total matches nights × rate; record a payment, confirm it shows in the payments table with the right amount/method.
  - Dependencies: 5.6, 0.7, 0.8
  - Files: `src/main/java/com/hotel/ui/billing/BillingPanel.java`
- [ ] 5.10 `ReportsPanel` UI (admin-only)
  - Acceptance: `BorderLayout` panel with two sub-sections stacked via `BoxLayout(Y_AXIS)` (or two tabs via `JTabbedPane` if that reads cleaner) — each sub-section has its own `NORTH` bar with two `JSpinner`s (from/to date, guide section 3) + a "Run" button, and its own `CENTER` read-only `JTable`: Occupancy section shows `ReportService`'s occupancy-by-range results (e.g. columns Room Type / Nights Booked / Occupancy Rate); Revenue section shows revenue-by-period results (e.g. columns Period / Total Revenue). Both tables are display-only — no `BaseTableModel` mutation methods needed beyond `setRows`, since nothing here is editable.
  - Verify: as admin, pick a date range covering the reservations/payments created during 3.x/5.x testing, run both reports, and confirm the numbers match a manual calculation from the underlying test data.
  - Dependencies: 5.8, 0.7
  - Files: `src/main/java/com/hotel/ui/reports/ReportsPanel.java`

**Exposes to other modules:** none further downstream — last module in the dependency chain.
