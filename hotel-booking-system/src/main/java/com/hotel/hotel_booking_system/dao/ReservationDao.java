package com.hotel.hotel_booking_system.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.hotel.hotel_booking_system.model.Reservation;
import com.hotel.hotel_booking_system.model.ReservationView;

public interface ReservationDao {
    Optional<Reservation> findById(long id);

    List<Reservation> findAll();

    void save(Reservation reservation);

    boolean hasConflict(long roomId, LocalDate checkIn, LocalDate checkOut);

    boolean hasCheckedInReservation(long roomId);

    List<ReservationView> findActiveWithDetails();
}
