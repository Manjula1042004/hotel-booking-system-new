package com.hotel.booking_system.repository;

import com.hotel.booking_system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Basic CRUD operations are inherited from JpaRepository
    // including findAll(), findById(), save(), deleteById(), etc.

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // FIXED: Include PENDING status in conflicting bookings
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND (b.status = 'CONFIRMED' OR b.status = 'PENDING') " +
            "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    List<Booking> findByStatus(Booking.BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Long countConfirmedBookings();

    // Add this method for better performance
    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> findAllOrderByCreatedAtDesc();

    // The findAll() method is automatically provided by JpaRepository
    // No need to declare it explicitly
}