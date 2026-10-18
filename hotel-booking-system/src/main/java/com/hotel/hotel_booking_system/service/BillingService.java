
package com.hotel.hotel_booking_system.service;

import com.hotel.hotel_booking_system.dao.InvoiceDao;
import com.hotel.hotel_booking_system.dao.PaymentDao;
import com.hotel.hotel_booking_system.model.Invoice;
import com.hotel.hotel_booking_system.model.Payment;
import com.hotel.hotel_booking_system.model.PaymentMethod;
import com.hotel.hotel_booking_system.model.Reservation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BillingService {
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final JdbcTemplate jdbcTemplate;
    public BillingService(
            InvoiceDao invoiceDao,
            PaymentDao paymentDao,
            JdbcTemplate jdbcTemplate) {
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.jdbcTemplate = jdbcTemplate;
    }
    @Transactional
    public Invoice generateInvoice(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException(
                "Reservation không được để trống"
            );
        }
        if (reservation.getCheckIn() == null
                || reservation.getCheckOut() == null) {
            throw new IllegalArgumentException(
                "Ngày nhận phòng và trả phòng không hợp lệ"
            );
        }
        long nights = ChronoUnit.DAYS.between(
            reservation.getCheckIn(),
            reservation.getCheckOut()
        );
        if (nights <= 0) {
            throw new IllegalArgumentException(
                "Số đêm phải lớn hơn 0"
            );
        }
        var existing = invoiceDao.findByReservationId(
            reservation.getId()
        );
        if (existing.isPresent()) {
            return existing.get();
        }
        String sql = """
            SELECT rt.base_price
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            WHERE r.id = ?
            """;
        BigDecimal basePrice = jdbcTemplate.queryForObject(
            sql,
            BigDecimal.class,
            reservation.getRoomId()
        );
        if (basePrice == null
                || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                "Giá phòng không hợp lệ"
            );
        }
        BigDecimal total = basePrice.multiply(
            BigDecimal.valueOf(nights)
        );
        Invoice invoice = new Invoice();
        invoice.setReservationId(reservation.getId());
        invoice.setTotalAmount(total);
        invoice.setIssuedAt(LocalDateTime.now());
        return invoiceDao.save(invoice);
    }
    @Transactional
    public Payment recordPayment(
            long invoiceId,
            BigDecimal amount,
            PaymentMethod method) {
        Invoice invoice = invoiceDao.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy hóa đơn: " + invoiceId
            ));
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Số tiền thanh toán phải lớn hơn 0"
            );
        }
        if (method == null) {
            throw new IllegalArgumentException(
                "Phải chọn phương thức thanh toán"
            );
        }
        BigDecimal totalPaid = paymentDao
            .findByInvoiceId(invoiceId)
            .stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = invoice.getTotalAmount()
            .subtract(totalPaid);
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException(
                "Số tiền thanh toán vượt quá số tiền còn nợ"
            );
        }
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaidAt(LocalDateTime.now());
        return paymentDao.save(payment);
    }
}
