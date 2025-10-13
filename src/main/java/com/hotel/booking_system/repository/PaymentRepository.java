package com.hotel.booking_system.repository;

import com.hotel.booking_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
    List<Payment> findByPaymentMethod(String paymentMethod);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS'")
    Long countSuccessfulPayments();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED'")
    Long countFailedPayments();

    // Add this method to find payments by booking
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId")
    List<Payment> findPaymentsByBookingId(Long bookingId);
}