package com.hotel.hotel_booking_system.model;

import java.time.LocalDate;

public class ReservationView {
    private final Long id;
    private final String roomNumber;
    private final String guestName;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final ReservationStatus status;

    public ReservationView(
        Long id,
        String roomNumber,
        String guestName,
        LocalDate checkIn,
        LocalDate checkOut,
        ReservationStatus status
    ) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}