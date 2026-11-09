package com.hotel.hotel_booking_system.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.hotel.hotel_booking_system.dao.RoomDao;
import com.hotel.hotel_booking_system.dao.RoomTypeDao;
import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomStatus;
import com.hotel.hotel_booking_system.model.RoomType;

@Service
public class RoomService {

    private final RoomDao roomDao;
    private final RoomTypeDao roomTypeDao;

    public RoomService(RoomDao roomDao, RoomTypeDao roomTypeDao) {
        this.roomDao = roomDao;
        this.roomTypeDao = roomTypeDao;
    }

    // ---------- Room ----------

    /** @throws IllegalArgumentException nếu dữ liệu không hợp lệ; UI hiển thị message cho người dùng. */
    public void save(Room room) {
        String number = room.getRoomNumber() == null ? "" : room.getRoomNumber().trim();
        if (number.isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        if (number.length() > 10) {
            throw new IllegalArgumentException("Room number must be at most 10 characters.");
        }
        room.setRoomNumber(number);

        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }

        if (room.getRoomTypeId() == null || roomTypeDao.findById(room.getRoomTypeId()).isEmpty()) {
            throw new IllegalArgumentException("Room type does not exist.");
        }

        if (room.getId() == null) {
            if (roomDao.existsByRoomNumber(number)) {
                throw new IllegalArgumentException("Room number " + number + " already exists.");
            }
        } else {
            Room existing = roomDao.findById(room.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found."));
            // Chỉ kiểm tra trùng khi đổi số phòng (tránh tự trùng với chính nó)
            if (!existing.getRoomNumber().equals(number) && roomDao.existsByRoomNumber(number)) {
                throw new IllegalArgumentException("Room number " + number + " already exists.");
            }
        }

        try {
            roomDao.save(room);
        } catch (DuplicateKeyException e) { // lớp bảo vệ cuối: ràng buộc UNIQUE trong DB
            throw new IllegalArgumentException("Room number " + number + " already exists.");
        }
    }

    public void deleteRoom(Long id) {
        try {
            roomDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Cannot delete this room because it has reservations. Set it to MAINTENANCE instead.");
        }
    }

    public List<Room> findAll() {
        return roomDao.findAll();
    }

    public Optional<Room> findById(Long id) {
        return roomDao.findById(id);
    }

    /** Module 3 (Booking) gọi method này để tìm phòng trống. */
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        return roomDao.findAvailable(checkIn, checkOut);
    }

    // ---------- Room type ----------

    public List<RoomType> findAllRoomTypes() {
        return roomTypeDao.findAll();
    }

    public void saveRoomType(RoomType rt) {
        String name = rt.getName() == null ? "" : rt.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name must be at most 50 characters.");
        }
        if (rt.getBasePrice() == null || rt.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base price must be greater than 0.");
        }
        if (rt.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }
        rt.setName(name);

        try {
            roomTypeDao.save(rt);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Room type name \"" + name + "\" already exists.");
        }
    }

    public void deleteRoomType(Long id) {
        try {
            roomTypeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Cannot delete this room type because rooms are still using it.");
        }
    }
}