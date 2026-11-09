package com.hotel.hotel_booking_system.model;

public class Room {

    private Long id; // null = chưa lưu vào DB
    private String roomNumber;
    private Long roomTypeId;
    private RoomStatus status;

    public Room() {
    }

    public Room(Long id, String roomNumber, Long roomTypeId, RoomStatus status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    @Override
    public String toString() {
        return roomNumber;
    }
}