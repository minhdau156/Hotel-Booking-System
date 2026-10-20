
package com.hotel.hotel_booking_system.dao;

import com.hotel.hotel_booking_system.model.Payment;
import com.hotel.hotel_booking_system.model.PaymentMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPaymentDao implements PaymentDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcPaymentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private final RowMapper<Payment> rowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setInvoiceId(rs.getLong("invoice_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setMethod(
            PaymentMethod.valueOf(rs.getString("method"))
        );
        Timestamp timestamp = rs.getTimestamp("paid_at");
        if (timestamp != null) {
            payment.setPaidAt(timestamp.toLocalDateTime());
        }
        return payment;
    };
    @Override
    public Payment save(Payment payment) {
        String sql = """
            INSERT INTO payments
                (invoice_id, amount, method, paid_at)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, payment.getInvoiceId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            if (payment.getPaidAt() != null) {
                ps.setTimestamp(
                    4, Timestamp.valueOf(payment.getPaidAt())
                );
            } else {
                ps.setTimestamp(4, null);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException(
                "Không lấy được ID thanh toán vừa tạo"
            );
        }
        payment.setId(key.longValue());
        return payment;
    }
    @Override
    public Optional<Payment> findById(long id) {
        String sql = """
            SELECT id, invoice_id, amount, method, paid_at
            FROM payments
            WHERE id = ?
            """;
        List<Payment> results =
            jdbcTemplate.query(sql, rowMapper, id);

        return results.stream().findFirst();
    }
    @Override
    public List<Payment> findAll() {
        String sql = """
            SELECT id, invoice_id, amount, method, paid_at
            FROM payments
            ORDER BY id DESC
            """;
        return jdbcTemplate.query(sql, rowMapper);
    }
    @Override
    public List<Payment> findByInvoiceId(long invoiceId) {
        String sql = """
            SELECT id, invoice_id, amount, method, paid_at
            FROM payments
            WHERE invoice_id = ?
            ORDER BY paid_at DESC
            """;
        return jdbcTemplate.query(sql, rowMapper, invoiceId);
    }
}
