
package com.hotel.hotel_booking_system.dao;

import com.hotel.hotel_booking_system.model.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    Payment save(Payment payment);
    Optional<Payment> findById(long id);
    List<Payment> findAll();
    List<Payment> findByInvoiceId(long invoiceId);
}
