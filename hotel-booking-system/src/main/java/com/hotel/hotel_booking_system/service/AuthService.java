package com.hotel.hotel_booking_system.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotel.hotel_booking_system.dao.StaffDao;
import com.hotel.hotel_booking_system.model.Staff;

@Service 
public class AuthService {
    private StaffDao staffDao;
    private CurrentUserContext currentUserContext;
    private PasswordEncoder passwordEncoder;

    public AuthService(StaffDao staffDao, CurrentUserContext currentUserContext, BCryptPasswordEncoder passwordEncoder) {
        this.staffDao = staffDao;
        this.currentUserContext = currentUserContext;
        this.passwordEncoder = passwordEncoder;
    }

    public void login(String username, String password) throws Exception {
        Optional<Staff> existingStaff = staffDao.findByUsername(username);
        if (!existingStaff.isPresent()) {
            throw new IllegalArgumentException("Login failed");
        }
        Staff staff = existingStaff.get();
        if (staff.isActive() == false) {
            throw new IllegalArgumentException("Login failed");
        }

        boolean isValid =passwordEncoder.matches(password, staff.getPasswordHash());

        if (!isValid) {
            throw new IllegalArgumentException("Login failed");
        }

        currentUserContext.setCurrentStaff(staff);

    }

    public void logout() {
        currentUserContext.clear();
    }
}
