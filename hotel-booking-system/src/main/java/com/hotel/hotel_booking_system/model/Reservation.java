package com.hotel.hotel_booking_system.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private long roomId;
    private long guestId;
    private long staffId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public Reservation() {
    }

    public Reservation(
            Long id,
            long roomId,
            long guestId,
            long staffId,
            LocalDate checkIn,
            LocalDate checkOut,
            ReservationStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.guestId = guestId;
        this.staffId = staffId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.createdAt = createdAt;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getGuestId() {
        return guestId;
    }

    public void setGuestId(long guestId) {
        this.guestId = guestId;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
