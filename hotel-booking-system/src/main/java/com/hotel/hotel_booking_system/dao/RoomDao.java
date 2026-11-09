package com.hotel.hotel_booking_system.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.hotel.hotel_booking_system.model.Room;

public interface RoomDao {

    Optional<Room> findById(Long id);

    List<Room> findAll();

    /** id == null -> INSERT, ngược lại -> UPDATE (cùng quy ước với RoomTypeDao). */
    void save(Room room);

    void delete(Long id);

    boolean existsByRoomNumber(String roomNumber);

    /** Phòng trống trong [checkIn, checkOut): không bị đặt chồng lấn và không bảo trì. */
    List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut);
}