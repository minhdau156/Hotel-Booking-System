
package com.hotel.hotel_booking_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private long id;
    private long reservationId;
    private BigDecimal totalAmount;
    private LocalDateTime issuedAt;
    public Invoice() {
    }
    public Invoice(long id, long reservationId,
                   BigDecimal totalAmount, LocalDateTime issuedAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.totalAmount = totalAmount;
        this.issuedAt = issuedAt;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getReservationId() {
        return reservationId;
    }
    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
