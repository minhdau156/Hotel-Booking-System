package com.hotel.hotel_booking_system.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.hotel.hotel_booking_system.model.Reservation;
import com.hotel.hotel_booking_system.model.ReservationStatus;
import com.hotel.hotel_booking_system.model.ReservationView;

@Repository
public class JdbcReservationDao implements ReservationDao {
    private static final String SELECT_COLUMNS = """
            SELECT id, room_id, guest_id, staff_id, check_in, check_out,
                   status, created_at
            FROM reservations
            """;

    private static final RowMapper<Reservation> RESERVATION_MAPPER =
            (rs, rowNum) -> new Reservation(
                    rs.getLong("id"),
                    rs.getLong("room_id"),
                    rs.getLong("guest_id"),
                    rs.getLong("staff_id"),
                    rs.getDate("check_in").toLocalDate(),
                    rs.getDate("check_out").toLocalDate(),
                    ReservationStatus.valueOf(rs.getString("status")),
                    rs.getTimestamp("created_at").toLocalDateTime());

    private static final RowMapper<ReservationView> VIEW_MAPPER =
            (rs, rowNum) -> new ReservationView(
                    rs.getLong("id"),
                    rs.getString("room_number"),
                    rs.getString("guest_name"),
                    rs.getDate("check_in").toLocalDate(),
                    rs.getDate("check_out").toLocalDate(),
                    ReservationStatus.valueOf(rs.getString("status")));

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?",
                RESERVATION_MAPPER, id).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(SELECT_COLUMNS + " ORDER BY id DESC",
                RESERVATION_MAPPER);
    }

    @Override
    public void save(Reservation reservation) {
        if (reservation.getId() == null || reservation.getId() <= 0) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO reservations
                            (room_id, guest_id, staff_id, check_in, check_out,
                             status, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, reservation.getRoomId());
                statement.setLong(2, reservation.getGuestId());
                statement.setLong(3, reservation.getStaffId());
                statement.setDate(4, Date.valueOf(reservation.getCheckIn()));
                statement.setDate(5, Date.valueOf(reservation.getCheckOut()));
                statement.setString(6, reservation.getStatus().name());
                statement.setTimestamp(7,
                        java.sql.Timestamp.valueOf(reservation.getCreatedAt()));
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException(
                        "Could not retrieve the new reservation ID.");
            }
            reservation.setId(key.longValue());
        } else {
            jdbcTemplate.update("""
                    UPDATE reservations
                    SET room_id = ?, guest_id = ?, staff_id = ?, check_in = ?,
                        check_out = ?, status = ?
                    WHERE id = ?
                    """,
                    reservation.getRoomId(),
                    reservation.getGuestId(),
                    reservation.getStaffId(),
                    Date.valueOf(reservation.getCheckIn()),
                    Date.valueOf(reservation.getCheckOut()),
                    reservation.getStatus().name(),
                    reservation.getId());
        }
    }

    @Override
    public boolean hasConflict(
            long roomId,
            java.time.LocalDate checkIn,
            java.time.LocalDate checkOut) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservations
                WHERE room_id = ?
                  AND status IN ('BOOKED', 'CHECKED_IN')
                  AND check_in < ?
                  AND check_out > ?
                """, Integer.class, roomId, Date.valueOf(checkOut),
                Date.valueOf(checkIn));
        return count != null && count > 0;
    }

    @Override
    public boolean hasCheckedInReservation(long roomId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservations
                WHERE room_id = ? AND status = 'CHECKED_IN'
                """, Integer.class, roomId);
        return count != null && count > 0;
    }

    @Override
    public List<ReservationView> findActiveWithDetails() {
        return jdbcTemplate.query("""
                SELECT r.id, rm.room_number, g.full_name AS guest_name,
                       r.check_in, r.check_out, r.status
                FROM reservations r
                JOIN rooms rm ON rm.id = r.room_id
                JOIN guests g ON g.id = r.guest_id
                WHERE r.status IN ('BOOKED', 'CHECKED_IN')
                ORDER BY r.check_in, rm.room_number
                """, VIEW_MAPPER);
    }
}
