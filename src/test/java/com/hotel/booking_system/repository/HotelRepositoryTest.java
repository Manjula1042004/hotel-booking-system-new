package com.hotel.booking_system.repository;

import com.hotel.booking_system.model.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");
        testHotel.setDescription("A test hotel");
        testHotel.setRating(4.5);
        testHotel.setImageUrl("http://test.com/image.jpg");
        testHotel.setCreatedAt(LocalDateTime.now());
        testHotel.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findByCityContainingIgnoreCase_ShouldReturnHotels() {
        // Given
        hotelRepository.save(testHotel);

        // When
        List<Hotel> found = hotelRepository.findByCityContainingIgnoreCase("test");

        // Then
        assertFalse(found.isEmpty());
        assertEquals("Test Hotel", found.get(0).getName());
    }

    @Test
    void findByCityContainingIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        // Given
        hotelRepository.save(testHotel);

        // When
        List<Hotel> found = hotelRepository.findByCityContainingIgnoreCase("nonexistent");

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void searchByCity_ShouldReturnHotels() {
        // Given
        hotelRepository.save(testHotel);

        // When
        List<Hotel> found = hotelRepository.searchByCity("Test");

        // Then
        assertFalse(found.isEmpty());
        assertEquals("Test Hotel", found.get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnHotels() {
        // Given
        hotelRepository.save(testHotel);

        // When
        List<Hotel> found = hotelRepository.findByNameContainingIgnoreCase("test");

        // Then
        assertFalse(found.isEmpty());
        assertEquals("Test Hotel", found.get(0).getName());
    }

    @Test
    void save_ShouldPersistHotel_WithAllFields() {
        // When
        Hotel savedHotel = hotelRepository.save(testHotel);

        // Then
        assertNotNull(savedHotel.getId());
        assertEquals("Test Hotel", savedHotel.getName());
        assertEquals("Test City", savedHotel.getCity());
        assertEquals("123 Test St", savedHotel.getAddress());
        assertEquals(4.5, savedHotel.getRating());
        assertNotNull(savedHotel.getCreatedAt());
        assertNotNull(savedHotel.getUpdatedAt());
    }
}