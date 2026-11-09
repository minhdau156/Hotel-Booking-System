package com.hotel.hotel_booking_system.dao;

import java.util.List;
import java.util.Optional;

import com.hotel.hotel_booking_system.model.RoomType;

public interface RoomTypeDao {

    Optional<RoomType> findById(Long id);

    List<RoomType> findAll();

    /** Quy ước: id == null -> INSERT; ngược lại -> UPDATE ... WHERE id = ? */
    void save(RoomType roomType);

    void delete(Long id);
}