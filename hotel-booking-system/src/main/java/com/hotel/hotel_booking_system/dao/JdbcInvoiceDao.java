
package com.hotel.hotel_booking_system.dao;

import com.hotel.hotel_booking_system.model.Invoice;
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
public class JdbcInvoiceDao implements InvoiceDao {
    private final JdbcTemplate jdbcTemplate;
    public JdbcInvoiceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private final RowMapper<Invoice> rowMapper = (rs, rowNum) -> {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getLong("id"));
        invoice.setReservationId(rs.getLong("reservation_id"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        Timestamp timestamp = rs.getTimestamp("issued_at");
        if (timestamp != null) {
            invoice.setIssuedAt(timestamp.toLocalDateTime());
        }
        return invoice;
    };
    @Override
    public Invoice save(Invoice invoice) {
        String sql = """
            INSERT INTO invoices
                (reservation_id, total_amount, issued_at)
            VALUES (?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, invoice.getReservationId());
            ps.setBigDecimal(2, invoice.getTotalAmount());
            if (invoice.getIssuedAt() != null) {
                ps.setTimestamp(
                    3, Timestamp.valueOf(invoice.getIssuedAt())
                );
            } else {
                ps.setTimestamp(3, null);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException(
                "Không lấy được ID hóa đơn vừa tạo"
            );
        }
        invoice.setId(key.longValue());
        return invoice;
    }
    @Override
    public Optional<Invoice> findById(long id) {
        String sql = """
            SELECT id, reservation_id, total_amount, issued_at
            FROM invoices
            WHERE id = ?
            """;
        List<Invoice> results =
            jdbcTemplate.query(sql, rowMapper, id);
        return results.stream().findFirst();
    }
    @Override
    public List<Invoice> findAll() {
        String sql = """
            SELECT id, reservation_id, total_amount, issued_at
            FROM invoices
            ORDER BY id DESC
            """;
        return jdbcTemplate.query(sql, rowMapper);
    }
    @Override
    public Optional<Invoice> findByReservationId(
            long reservationId) {
        String sql = """
            SELECT id, reservation_id, total_amount, issued_at
            FROM invoices
            WHERE reservation_id = ?
            """;
        List<Invoice> results =
            jdbcTemplate.query(sql, rowMapper, reservationId);
        return results.stream().findFirst();
    }
}
