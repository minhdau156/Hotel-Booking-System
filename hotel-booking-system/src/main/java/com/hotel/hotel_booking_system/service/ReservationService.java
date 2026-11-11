package com.hotel.hotel_booking_system.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.hotel_booking_system.dao.ReservationDao;
import com.hotel.hotel_booking_system.model.Reservation;
import com.hotel.hotel_booking_system.model.ReservationStatus;
import com.hotel.hotel_booking_system.model.ReservationView;
import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomStatus;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final RoomService roomService;
    private final GuestService guestService;
    private final BillingService billingService;

    public ReservationService(
            ReservationDao reservationDao,
            RoomService roomService,
            GuestService guestService,
            BillingService billingService) {
        this.reservationDao = reservationDao;
        this.roomService = roomService;
        this.guestService = guestService;
        this.billingService = billingService;
    }

    @Transactional
    public Reservation create(
            long roomId,
            long guestId,
            long staffId,
            LocalDate checkIn,
            LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out must be after check-in.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Check-in date cannot be in the past.");
        }
        if (staffId <= 0) {
            throw new IllegalArgumentException(
                    "A logged-in staff member is required to book a room.");
        }
        Room room = roomService.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room not found."));
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new IllegalArgumentException(
                    "This room is under maintenance.");
        }
        if (guestService.findById(guestId).isEmpty()) {
            throw new IllegalArgumentException("Guest not found.");
        }
        if (reservationDao.hasConflict(roomId, checkIn, checkOut)) {
            throw new IllegalArgumentException(
                    "This room has already been booked for the selected dates.");
        }

        Reservation reservation = new Reservation(
                null,
                roomId,
                guestId,
                staffId,
                checkIn,
                checkOut,
                ReservationStatus.BOOKED,
                LocalDateTime.now());
        reservationDao.save(reservation);
        return reservation;
    }

    @Transactional
    public void checkIn(long reservationId) {
        Reservation reservation = requireReservation(reservationId);
        requireStatus(reservation, ReservationStatus.BOOKED);
        Room room = requireRoom(reservation);
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new IllegalArgumentException(
                    "This room is under maintenance.");
        }
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservationDao.save(reservation);
        reservationDao.save(reservation);
        updateRoomStatus(room);
    }

    @Transactional
    public void checkOut(long reservationId) {
        Reservation reservation = requireReservation(reservationId);
        requireStatus(reservation, ReservationStatus.CHECKED_IN);
        Room room = requireRoom(reservation);

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservationDao.save(reservation);
        updateRoomStatus(room);
        billingService.generateInvoice(reservation);
    }

    @Transactional
    public void cancel(long reservationId) {
        Reservation reservation = requireReservation(reservationId);
        if (reservation.getStatus() != ReservationStatus.BOOKED
                && reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalArgumentException(
                    "Only booked or checked-in reservations can be cancelled.");
        }
        Room room = requireRoom(reservation);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationDao.save(reservation);
        updateRoomStatus(room);
    }

    public List<ReservationView> findActiveWithDetails() {
        return reservationDao.findActiveWithDetails();
    }

    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    private Reservation requireReservation(long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found."));
    }

    private Room requireRoom(Reservation reservation) {
        return roomService.findById(reservation.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "The room for this reservation was not found."));
    }

    private void updateRoomStatus(Room room) {
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            return;
        }
        room.setStatus(reservationDao.hasCheckedInReservation(room.getId())
                ? RoomStatus.OCCUPIED
                : RoomStatus.AVAILABLE);
        roomService.save(room);
    }

    private static void requireStatus(
            Reservation reservation,
            ReservationStatus expected) {
        if (reservation.getStatus() != expected) {
            throw new IllegalArgumentException(
                    "Reservation must be " + expected + " to perform this action.");
        }
    }
}
