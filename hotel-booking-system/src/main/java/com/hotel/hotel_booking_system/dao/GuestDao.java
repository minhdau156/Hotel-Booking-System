package com.hotel.hotel_booking_system.dao;

import java.util.List;
import java.util.Optional;

import com.hotel.hotel_booking_system.model.Guest;

public interface GuestDao{

    Optional<Guest> findById(long id);
    List<Guest> findAll();
    void save(Guest guest);
    List<Guest> search(String query);
}

