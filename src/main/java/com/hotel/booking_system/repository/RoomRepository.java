package com.hotel.booking_system.repository;

import com.hotel.booking_system.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);

    // FIXED: Corrected the query to properly check availability
    @Query("SELECT r FROM Room r WHERE LOWER(r.hotel.city) LIKE LOWER(CONCAT('%', :city, '%')) " +
            "AND r.available = true " +
            "AND r.id NOT IN (" +
            "   SELECT b.room.id FROM Booking b WHERE " +
            "   (b.status = 'CONFIRMED' OR b.status = 'PENDING') AND " +
            "   ((b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate))" +
            ")")
    List<Room> findAvailableRoomsByCityAndDates(
            @Param("city") String city,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.available = true " +
            "AND r.id NOT IN (" +
            "   SELECT b.room.id FROM Booking b WHERE " +
            "   (b.status = 'CONFIRMED' OR b.status = 'PENDING') AND " +
            "   ((b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate))" +
            ")")
    List<Room> findAvailableRoomsByHotelAndDates(
            @Param("hotelId") Long hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
}