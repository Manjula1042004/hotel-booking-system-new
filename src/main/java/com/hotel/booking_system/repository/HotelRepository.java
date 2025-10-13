package com.hotel.booking_system.repository;

import com.hotel.booking_system.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByCityContainingIgnoreCase(String city);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Hotel> searchByCity(@Param("city") String city);

    List<Hotel> findByNameContainingIgnoreCase(String name);
}