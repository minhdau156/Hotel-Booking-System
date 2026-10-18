
package com.hotel.hotel_booking_system.ui.billing;

import com.hotel.hotel_booking_system.dao.InvoiceDao;
import com.hotel.hotel_booking_system.dao.PaymentDao;
import com.hotel.hotel_booking_system.model.Invoice;
import com.hotel.hotel_booking_system.model.Payment;
import com.hotel.hotel_booking_system.model.PaymentMethod;
import com.hotel.hotel_booking_system.service.BillingService;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillingPanel extends JPanel {
    private final JdbcTemplate jdbcTemplate;
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final BillingService billingService;
    private final ReservationTableModel reservationModel =
        new ReservationTableModel();
    private final PaymentTableModel paymentModel =
        new PaymentTableModel();
    private final JTable reservationTable =
        new JTable(reservationModel);
    private final JTable paymentTable =
        new JTable(paymentModel);
    private final JLabel totalLabel =
        new JLabel("Tổng tiền: --");
    private final JLabel issuedLabel =
        new JLabel("Ngày phát hành: --");
    private final JButton paymentButton =
        new JButton("Record Payment");
    private Long selectedInvoiceId;
    public BillingPanel(
            JdbcTemplate jdbcTemplate,
            InvoiceDao invoiceDao,
            PaymentDao paymentDao,
            BillingService billingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.billingService = billingService;
        buildUI();
        loadReservations();
    }
    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reservationTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        reservationTable.setAutoCreateRowSorter(true);
        JScrollPane reservationScroll =
            new JScrollPane(reservationTable);
        reservationScroll.setBorder(
            BorderFactory.createTitledBorder(
                "Checked-out reservations"
            )
        );
        add(reservationScroll, BorderLayout.NORTH);
        JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
        JPanel summaryPanel = new JPanel(
            new GridLayout(2, 1, 5, 5)
        );
        summaryPanel.add(totalLabel);
        summaryPanel.add(issuedLabel);
        detailsPanel.add(summaryPanel, BorderLayout.NORTH);
        paymentTable.setFillsViewportHeight(true);
        JScrollPane paymentScroll =
            new JScrollPane(paymentTable);
        paymentScroll.setBorder(
            BorderFactory.createTitledBorder("Payments")
        );
        detailsPanel.add(paymentScroll, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.CENTER);
        paymentButton.setEnabled(false);
        JPanel bottomPanel = new JPanel(new FlowLayout(
            FlowLayout.RIGHT
        ));
        bottomPanel.add(paymentButton);
        add(bottomPanel, BorderLayout.SOUTH);
        reservationTable.getSelectionModel()
            .addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    onReservationSelected();
                }
            });
        paymentButton.addActionListener(
            event -> showRecordPaymentDialog()
        );
    }
    private void loadReservations() {
        String sql = """
            SELECT
                r.id AS reservation_id,
                g.full_name AS guest_name,
                rm.room_number,
                r.check_in,
                r.check_out
            FROM reservations r
            JOIN guests g ON g.id = r.guest_id
            JOIN rooms rm ON rm.id = r.room_id
            WHERE r.status = 'CHECKED_OUT'
            ORDER BY r.check_out DESC
            """;
        List<ReservationRow> rows = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new ReservationRow(
                rs.getLong("reservation_id"),
                rs.getString("guest_name"),
                rs.getString("room_number"),
                rs.getDate("check_in").toLocalDate().toString(),
                rs.getDate("check_out").toLocalDate().toString()
            )
        );
        reservationModel.setRows(rows);
    }
    private void onReservationSelected() {
        int viewRow = reservationTable.getSelectedRow();
        if (viewRow < 0) {
            clearInvoice();
            return;
        }
        int modelRow = reservationTable.convertRowIndexToModel(
            viewRow
        );
        ReservationRow reservation =
            reservationModel.getRow(modelRow);
        var invoiceOptional =
            invoiceDao.findByReservationId(reservation.id());
        if (invoiceOptional.isEmpty()) {
            clearInvoice();
            JOptionPane.showMessageDialog(
                this,
                "Chưa tìm thấy hóa đơn cho lượt đặt phòng này.",
                "Invoice not found",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        loadInvoice(invoiceOptional.get());
    }
    private void loadInvoice(Invoice invoice) {
        selectedInvoiceId = invoice.getId();
        totalLabel.setText(
            "Tổng tiền: " + invoice.getTotalAmount().toPlainString()
        );
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        issuedLabel.setText(
            "Ngày phát hành: "
                + (invoice.getIssuedAt() == null
                    ? "--"
                    : invoice.getIssuedAt().format(formatter))
        );
        paymentModel.setRows(
            paymentDao.findByInvoiceId(invoice.getId())
        );
        paymentButton.setEnabled(true);
    }
    private void clearInvoice() {
        selectedInvoiceId = null;
        totalLabel.setText("Tổng tiền: --");
        issuedLabel.setText("Ngày phát hành: --");
        paymentModel.setRows(List.of());
        paymentButton.setEnabled(false);
    }
    private void showRecordPaymentDialog() {
        if (selectedInvoiceId == null) {
            return;
        }
        JSpinner amountSpinner = new JSpinner(
            new SpinnerNumberModel(
                100000.0, 1.0, 1000000000.0, 10000.0
            )
        );
        JComboBox<PaymentMethod> methodCombo =
            new JComboBox<>(PaymentMethod.values());
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Amount:"));
        form.add(amountSpinner);
        form.add(new JLabel("Payment method:"));
        form.add(methodCombo);
        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Record Payment",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        BigDecimal amount = BigDecimal.valueOf(
            ((Number) amountSpinner.getValue()).doubleValue()
        );
        PaymentMethod method =
            (PaymentMethod) methodCombo.getSelectedItem();
        try {
            billingService.recordPayment(
                selectedInvoiceId,
                amount,
                method
            );
            paymentModel.setRows(
                paymentDao.findByInvoiceId(selectedInvoiceId)
            );
            JOptionPane.showMessageDialog(
                this,
                "Đã ghi nhận thanh toán thành công."
            );
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Payment error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private record ReservationRow(
        long id,
        String guestName,
        String roomNumber,
        String checkIn,
        String checkOut
    ) {}
    private static class ReservationTableModel
            extends AbstractTableModel {
        private final String[] columns = {
            "Reservation ID", "Guest", "Room",
            "Check-in", "Check-out"
        };
        private List<ReservationRow> rows = new ArrayList<>();
        public void setRows(List<ReservationRow> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }
        public ReservationRow getRow(int index) {
            return rows.get(index);
        }
        @Override
        public int getRowCount() {
            return rows.size();
        }
        @Override
        public int getColumnCount() {
            return columns.length;
        }
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }
        @Override
        public Object getValueAt(int row, int column) {
            ReservationRow item = rows.get(row);
            return switch (column) {
                case 0 -> item.id();
                case 1 -> item.guestName();
                case 2 -> item.roomNumber();
                case 3 -> item.checkIn();
                case 4 -> item.checkOut();
                default -> null;
            };
        }
    }
    private static class PaymentTableModel
            extends AbstractTableModel {
        private final String[] columns = {
            "Amount", "Method", "Paid At"
        };
        private List<Payment> rows = new ArrayList<>();
        public void setRows(List<Payment> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }
        @Override
        public int getRowCount() {
            return rows.size();
        }
        @Override
        public int getColumnCount() {
            return columns.length;
        }
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }
        @Override
        public Object getValueAt(int row, int column) {
            Payment item = rows.get(row);
            return switch (column) {
                case 0 -> item.getAmount();
                case 1 -> item.getMethod();
                case 2 -> item.getPaidAt();
                default -> null;
            };
        }
    }
}
