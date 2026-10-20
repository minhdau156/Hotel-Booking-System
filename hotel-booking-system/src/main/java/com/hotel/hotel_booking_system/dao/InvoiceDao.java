
package com.hotel.hotel_booking_system.dao;

import com.hotel.hotel_booking_system.model.Invoice;
import java.util.List;
import java.util.Optional;

public interface InvoiceDao {
    Invoice save(Invoice Invoice);
    Optional<Invoice> findById(long id);
    List<Invoice> findAll();
    Optional<Invoice> findByReservationId(long reservationId);
}
