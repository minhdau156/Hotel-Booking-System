package com.hotel.hotel_booking_system.dao;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomStatus;

@Repository
public class JdbcRoomDao implements RoomDao {

    private static final String SELECT = "SELECT id, room_number, room_type_id, status FROM rooms";

    private static final RowMapper<Room> ROW_MAPPER = (rs, rowNum) -> {
        Room r = new Room();
        r.setId(rs.getLong("id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setStatus(RoomStatus.valueOf(rs.getString("status")));
        return r;
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcRoomDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Room> findById(Long id) {
        return jdbcTemplate.query(SELECT + " WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Room> findAll() {
        return jdbcTemplate.query(SELECT + " ORDER BY room_number", ROW_MAPPER);
    }

    @Override
    public void save(Room room) {
        if (room.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO rooms (room_number, room_type_id, status) VALUES (?, ?, ?)",
                    room.getRoomNumber(), room.getRoomTypeId(), room.getStatus().name());
        } else {
            jdbcTemplate.update(
                    "UPDATE rooms SET room_number = ?, room_type_id = ?, status = ? WHERE id = ?",
                    room.getRoomNumber(), room.getRoomTypeId(), room.getStatus().name(), room.getId());
        }
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM rooms WHERE id = ?", id);
    }

    @Override
    public boolean existsByRoomNumber(String roomNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rooms WHERE room_number = ?", Integer.class, roomNumber);
        return count != null && count > 0;
    }

    @Override
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        // Hai khoảng chồng lấn khi: existing.check_in < newCheckOut AND existing.check_out > newCheckIn
        String sql = SELECT + """
                 WHERE status <> 'MAINTENANCE'
                   AND id NOT IN (
                       SELECT room_id FROM reservations
                       WHERE status IN ('BOOKED', 'CHECKED_IN')
                         AND check_in < ? AND check_out > ?)
                 ORDER BY room_number
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, Date.valueOf(checkOut), Date.valueOf(checkIn));
    }
}