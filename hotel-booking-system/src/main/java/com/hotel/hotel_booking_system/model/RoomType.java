package com.hotel.hotel_booking_system.model;

import java.math.BigDecimal;

public class RoomType {

    private Long id; // null = chưa lưu vào DB
    private String name;
    private BigDecimal basePrice;
    private int capacity;
    private String description;

    public RoomType() {
    }

    public RoomType(Long id, String name, BigDecimal basePrice, int capacity, String description) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /** JComboBox<RoomType> sẽ hiển thị tên thay vì "RoomType@1a2b". */
    @Override
    public String toString() {
        return name;
    }
}