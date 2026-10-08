package com.hotel.hotel_booking_system.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.hotel.hotel_booking_system.model.Role;
import com.hotel.hotel_booking_system.model.Staff;

@Repository 
public class JdbcStaffDao implements StaffDao {
    private JdbcTemplate jdbcTemplate;
    public JdbcStaffDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private  RowMapper<Staff> mappingEntityToDomain() {
        return (rs, rowNum) -> {
            Staff staff = new Staff();
            staff.setId(rs.getLong("id"));
            staff.setUsername(rs.getString("username"));
            staff.setPasswordHash(rs.getString("password_hash"));
            staff.setFullName(rs.getString("full_name"));
            staff.setRole(Role.valueOf(rs.getString("role")));
            staff.setActive(rs.getBoolean("active"));
            return staff;
        };
    }

    @Override
    public Optional<Staff> findById(Long id) {
        try {
            System.out.println("Find by id: " + id + "successful");
            return Optional.of(jdbcTemplate.queryForObject("SELECT * FROM staff WHERE id = ?", mappingEntityToDomain(), id));
        }
        catch (Exception e) {
            System.out.println("Find by id: " + id + "failed");
            return Optional.empty();
        }
        
    }

    @Override
    public List<Staff> findAll() {
        System.out.println("Find all staff successful");
        return jdbcTemplate.query("SELECT * FROM staff", mappingEntityToDomain());
    }

    @Override
    public Optional<Staff> findByUsername(String username) {
        try {
            System.out.println("Find by username: " + username + "successful");
            return Optional.of(jdbcTemplate.queryForObject("SELECT * FROM staff WHERE username = ?", mappingEntityToDomain(), username));

        }
        catch (Exception e) {
            System.out.println("Find by username: " + username + "failed");
            return Optional.empty();
        }
    }

    @Override
    public void save(Staff staff) {
        boolean existing = staff.getId() != null;

        if (existing) {
            jdbcTemplate.update("UPDATE staff SET username = ?, password_hash = ?, full_name = ?, role = ?, active = ? WHERE id = ?",
                    staff.getUsername(), staff.getPasswordHash(), staff.getFullName(), staff.getRole().name(), staff.isActive(), staff.getId());

            System.out.println("Update the staff successful");
        }
        else {
            jdbcTemplate.update("INSERT INTO staff (username, password_hash, full_name, role, active) VALUES (?, ?, ?, ?, ?)",
                    staff.getUsername(), staff.getPasswordHash(), staff.getFullName(), staff.getRole().name(), staff.isActive());
            System.out.println("Save the staff successful");
        }

    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM staff WHERE id = ?", id);
        System.out.println("Delete successful");
    }



}
