
package com.hotel.hotel_booking_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private long id;
    private long invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private LocalDateTime paidAt;
    public Payment() {
    }
    public Payment(long id, long invoiceId,
                   BigDecimal amount, PaymentMethod method,
                   LocalDateTime paidAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public PaymentMethod getMethod() {
        return method;
    }
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
