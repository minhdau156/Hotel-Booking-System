package com.hotel.hotel_booking_system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotel.hotel_booking_system.dao.StaffDao;
import com.hotel.hotel_booking_system.model.Role;
import com.hotel.hotel_booking_system.model.Staff;

@Service 
public class StaffService {
    private StaffDao staffDao;
    private PasswordEncoder passwordEncoder;

    public StaffService(StaffDao staffDao, PasswordEncoder passwordEncoder) {
        this.staffDao = staffDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Staff> findAll() {
        return staffDao.findAll();
    }

    public Staff findById(Long id) {
        Optional<Staff> staff = staffDao.findById(id);
        if (!staff.isPresent()) {
            throw new IllegalArgumentException("Staff not found");
        }
        return staff.get();
    }

    public void create(Staff staff, String rawPassword) {
        if (staff.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (staff.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        staff.setPasswordHash(encodedPassword);
        staff.setActive(true);
        staffDao.save(staff);
    }

    public void update(Staff existing, String username, String fullname, Role role, boolean active) {

        existing.setUsername(username);
        existing.setFullName(fullname);
        existing.setRole(role);
        existing.setActive(active);
        staffDao.save(existing);
    }

    public void resetPassword(Long staffId, String newRawPassword) {
        String encodedPassword = passwordEncoder.encode(newRawPassword);
        Optional<Staff> existingStaff = staffDao.findById(staffId);
        if (existingStaff.isPresent()) {
            Staff staff = existingStaff.get();
            staff.setPasswordHash(encodedPassword);
            staffDao.save(staff);
        }
        else {
            throw new IllegalArgumentException("Staff not found");
        }
    }

    public void deactivate(Long staffId) {
        Optional<Staff> existingStaff = staffDao.findById(staffId);
        if (existingStaff.isPresent()) {
            Staff staff = existingStaff.get();
            staff.setActive(false);
            staffDao.save(staff);
        }
        else {
            throw new IllegalArgumentException("Staff not found");
        }
    }
    

}
