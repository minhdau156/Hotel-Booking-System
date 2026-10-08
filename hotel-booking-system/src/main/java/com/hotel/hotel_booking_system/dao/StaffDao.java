package com.hotel.hotel_booking_system.dao;

import java.util.List;
import java.util.Optional;

import com.hotel.hotel_booking_system.model.Staff;

public interface StaffDao {
    Optional<Staff> findById(Long id);
    List<Staff> findAll();
    Optional<Staff> findByUsername(String username);
    void save(Staff staff);
    void delete(Long id);
}
