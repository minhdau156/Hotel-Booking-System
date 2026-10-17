package com.hotel.hotel_booking_system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hotel.hotel_booking_system.dao.GuestDao;
import com.hotel.hotel_booking_system.model.Guest;

@Service
public class GuestService{

    private final GuestDao guestDao;

    public GuestService(GuestDao guestDao){
        this.guestDao = guestDao;
    }

    public Optional<Guest> findById(long id){
        return guestDao.findById(id);
    }

    public List<Guest> findAll(){
        return guestDao.findAll();
    }

    public List<Guest> search(String query){
        return guestDao.search(query);
    }

    public void save(Guest guest){
        if (guest == null){
            throw new IllegalArgumentException("Guest must not be null");
        }

        if (guest.getFullName() == null || guest.getFullName().isBlank()){
            throw new IllegalArgumentException("Full name must not be blank");
        }

        boolean hasPhone = guest.getPhone() != null && !guest.getPhone().isBlank();
        boolean hasEmail = guest.getEmail() != null && !guest.getEmail().isBlank();

        if (!hasPhone && !hasEmail){
            throw new IllegalArgumentException("At least one of phone or email is required");
        }

        guestDao.save(guest);
    }
}
