package com.hotel.hotel_booking_system.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.hotel.hotel_booking_system.model.RoomType;

@Repository
public class JdbcRoomTypeDao implements RoomTypeDao {

    private static final String SELECT =
            "SELECT id, name, base_price, capacity, description FROM room_types";

    private static final RowMapper<RoomType> ROW_MAPPER = (rs, rowNum) -> {
        RoomType rt = new RoomType();
        rt.setId(rs.getLong("id"));
        rt.setName(rs.getString("name"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setCapacity(rs.getInt("capacity"));
        rt.setDescription(rs.getString("description"));
        return rt;
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcRoomTypeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RoomType> findById(Long id) {
        return jdbcTemplate.query(SELECT + " WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<RoomType> findAll() {
        return jdbcTemplate.query(SELECT + " ORDER BY name", ROW_MAPPER);
    }

    @Override
    public void save(RoomType rt) {
        if (rt.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO room_types (name, base_price, capacity, description) VALUES (?, ?, ?, ?)",
                    rt.getName(), rt.getBasePrice(), rt.getCapacity(), rt.getDescription());
        } else {
            jdbcTemplate.update(
                    "UPDATE room_types SET name = ?, base_price = ?, capacity = ?, description = ? WHERE id = ?",
                    rt.getName(), rt.getBasePrice(), rt.getCapacity(), rt.getDescription(), rt.getId());
        }
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM room_types WHERE id = ?", id);
    }
}