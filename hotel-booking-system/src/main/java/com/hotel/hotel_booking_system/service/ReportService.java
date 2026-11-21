
package com.hotel.hotel_booking_system.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {
    private final JdbcTemplate jdbcTemplate;
    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public record OccupancyRow(
        String roomTypeName,
        long nightsBooked,
        double occupancyRate
    ) {}
    public record RevenueRow(
        String period,
        BigDecimal totalRevenue
    ) {}
    public List<OccupancyRow> occupancyReport(
            LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        String sql = """
            SELECT
                rt.name AS room_type_name,
                COALESCE(SUM(
                    CASE
                        WHEN res.id IS NOT NULL THEN
                            GREATEST(
                                0,
                                DATEDIFF(
                                    LEAST(res.check_out, ?),
                                    GREATEST(res.check_in, ?)
                                )
                            )
                        ELSE 0
                    END
                ), 0) AS nights_booked,
                COALESCE(
                    100.0 * COALESCE(SUM(
                        CASE
                            WHEN res.id IS NOT NULL THEN
                                GREATEST(
                                    0,
                                    DATEDIFF(
                                        LEAST(res.check_out, ?),
                                        GREATEST(res.check_in, ?)
                                    )
                                )
                            ELSE 0
                        END
                    ), 0)
                    /
                    NULLIF(
                        COUNT(DISTINCT rooms.id)
                        * DATEDIFF(?, ?),
                        0
                    ),
                    0
                ) AS occupancy_rate
            FROM room_types rt
            LEFT JOIN rooms
                ON rooms.room_type_id = rt.id
            LEFT JOIN reservations res
                ON res.room_id = rooms.id
                AND res.status <> 'CANCELLED'
                AND res.check_in < ?
                AND res.check_out > ?
            GROUP BY rt.id, rt.name
            ORDER BY rt.name
            """;
        Date sqlFrom = Date.valueOf(from);
        Date sqlTo = Date.valueOf(to);
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new OccupancyRow(
                rs.getString("room_type_name"),
                rs.getLong("nights_booked"),
                rs.getDouble("occupancy_rate")
            ),
            sqlTo, sqlFrom,
            sqlTo, sqlFrom,
            sqlTo, sqlFrom,
            sqlTo, sqlFrom
        );
    }
        public List<RevenueRow> revenueByPeriod(
            LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        String sql = """
            SELECT
                DATE_FORMAT(i.issued_at, '%Y-%m') AS period,
                SUM(i.total_amount) AS total_revenue
            FROM invoices i
            JOIN reservations r
                ON r.id = i.reservation_id
            WHERE i.issued_at >= ?
              AND i.issued_at < ?
            GROUP BY DATE_FORMAT(i.issued_at, '%Y-%m')
            ORDER BY period
            """;
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new RevenueRow(
                rs.getString("period"),
                rs.getBigDecimal("total_revenue")
            ),
            Date.valueOf(from),
            Date.valueOf(to.plusDays(1))
        );
    }
    private void validateDateRange(
            LocalDate from, LocalDate to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException(
                "Ngày bắt đầu phải trước ngày kết thúc"
            );
        }
    }
}