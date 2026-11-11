package com.hotel.hotel_booking_system.dao;

import java.util.List;
import java.util.Optional;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.hotel.hotel_booking_system.model.Guest;

@Repository
public class JdbcGuestDao implements GuestDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcGuestDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Guest> mappingEntityToDomain(){
        return (rs, rowNum)->{
            Guest guest = new Guest();
            guest.setId(rs.getLong("id"));
            guest.setFullName(rs.getString("full_name"));
            guest.setPhone(rs.getString("phone"));
            guest.setEmail(rs.getString("email"));
            guest.setIdNumber(rs.getString("id_number"));
            return guest;
        };
    }

    @Override
    public Optional<Guest> findById(long id){
        try{
            return Optional.of(jdbcTemplate.queryForObject("SELECT * FROM guests WHERE id = ?", mappingEntityToDomain(), id));
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Guest> findAll(){
        return jdbcTemplate.query("SELECT * FROM guests", mappingEntityToDomain());
    }

    @Override
    public void save(Guest guest){
        if (guest.getId() > 0){
            jdbcTemplate.update("UPDATE guests SET full_name = ?, phone = ?, email = ?, id_number = ? WHERE id = ?", guest.getFullName(), guest.getPhone(), guest.getEmail(), guest.getIdNumber(), guest.getId());
        } else{
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement(
                        "INSERT INTO guests (full_name, phone, email, id_number) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, guest.getFullName());
                statement.setString(2, guest.getPhone());
                statement.setString(3, guest.getEmail());
                statement.setString(4, guest.getIdNumber());
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException(
                        "Could not retrieve the new guest ID.");
            }
            guest.setId(key.longValue());
        }
    }

    @Override
    public void delete(long id){
        jdbcTemplate.update("DELETE FROM guests WHERE id = ?", id);
    }

    @Override
    public List<Guest> search(String query){
        String keyword = "%" + (query == null ? "" : query.trim()) + "%";

        return jdbcTemplate.query("SELECT * FROM guests WHERE full_name LIKE ? OR phone LIKE ?", mappingEntityToDomain(), keyword, keyword);
    }
}
