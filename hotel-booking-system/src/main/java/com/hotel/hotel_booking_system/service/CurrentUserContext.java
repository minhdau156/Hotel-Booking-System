package com.hotel.hotel_booking_system.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.model.Role;
import com.hotel.hotel_booking_system.model.Staff;

@Component 
public class CurrentUserContext {
    private Staff currentStaff = null;

    public Optional<Staff> getCurrentStaff() {
        return Optional.ofNullable(currentStaff);
    }

    public void setCurrentStaff(Staff staff) {
        currentStaff = staff;
    }

    public void clear() {
        currentStaff = null;
    }

    public boolean isLoggedIn() {
        return currentStaff != null;
    }

    public Role getRole() {
        return isLoggedIn() ? currentStaff.getRole() : null;
    }
}
