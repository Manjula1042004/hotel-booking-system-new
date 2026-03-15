package com.hotel.booking_system.service;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel testHotel;
    private final Long hotelId = 1L;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(hotelId);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");
        testHotel.setDescription("A test hotel");
        testHotel.setRating(4.5);
        testHotel.setImageUrl("http://test.com/image.jpg");
        testHotel.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createHotel_ShouldSaveAndReturnHotel() {
        // Given
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);

        // When
        Hotel createdHotel = hotelService.createHotel(testHotel);

        // Then
        assertNotNull(createdHotel);
        assertEquals(testHotel.getId(), createdHotel.getId());
        assertEquals(testHotel.getName(), createdHotel.getName());
        verify(hotelRepository).save(testHotel);
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() {
        // Given
        List<Hotel> hotels = Arrays.asList(testHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        // When
        List<Hotel> result = hotelService.getAllHotels();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHotel.getId(), result.get(0).getId());
        verify(hotelRepository).findAll();
    }

    @Test
    void getHotelById_ShouldReturnHotel_WhenExists() {
        // Given
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(testHotel));

        // When
        Optional<Hotel> result = hotelService.getHotelById(hotelId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(hotelId, result.get().getId());
        verify(hotelRepository).findById(hotelId);
    }

    @Test
    void getHotelById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // When
        Optional<Hotel> result = hotelService.getHotelById(hotelId);

        // Then
        assertFalse(result.isPresent());
        verify(hotelRepository).findById(hotelId);
    }

    @Test
    void updateHotel_ShouldUpdateAndReturnHotel_WhenExists() {
        // Given
        Hotel updateDetails = new Hotel();
        updateDetails.setName("Updated Hotel");
        updateDetails.setCity("Updated City");
        updateDetails.setAddress("456 Updated St");
        updateDetails.setDescription("Updated description");
        updateDetails.setRating(4.8);
        updateDetails.setImageUrl("http://test.com/updated.jpg");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(testHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);

        // When
        Hotel updatedHotel = hotelService.updateHotel(hotelId, updateDetails);

        // Then
        assertNotNull(updatedHotel);
        assertEquals("Updated Hotel", testHotel.getName());
        assertEquals("Updated City", testHotel.getCity());
        assertEquals("456 Updated St", testHotel.getAddress());
        assertEquals("Updated description", testHotel.getDescription());
        assertEquals(4.8, testHotel.getRating());
        assertEquals("http://test.com/updated.jpg", testHotel.getImageUrl());
        verify(hotelRepository).findById(hotelId);
        verify(hotelRepository).save(testHotel);
    }

    @Test
    void updateHotel_ShouldThrowException_WhenNotExists() {
        // Given
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            hotelService.updateHotel(hotelId, testHotel);
        });
        assertEquals("Hotel not found", exception.getMessage());
        verify(hotelRepository).findById(hotelId);
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void deleteHotel_ShouldDeleteHotel() {
        // Given
        doNothing().when(hotelRepository).deleteById(hotelId);

        // When
        hotelService.deleteHotel(hotelId);

        // Then
        verify(hotelRepository).deleteById(hotelId);
    }

    @Test
    void searchHotelsByCity_ShouldReturnHotelsInCity() {
        // Given
        String city = "Test City";
        List<Hotel> hotels = Arrays.asList(testHotel);
        when(hotelRepository.searchByCity(city)).thenReturn(hotels);

        // When
        List<Hotel> result = hotelService.searchHotelsByCity(city);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(city, result.get(0).getCity());
        verify(hotelRepository).searchByCity(city);
    }

    @Test
    void searchHotelsByName_ShouldReturnHotelsWithName() {
        // Given
        String name = "Test";
        List<Hotel> hotels = Arrays.asList(testHotel);
        when(hotelRepository.findByNameContainingIgnoreCase(name)).thenReturn(hotels);

        // When
        List<Hotel> result = hotelService.searchHotelsByName(name);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains(name));
        verify(hotelRepository).findByNameContainingIgnoreCase(name);
    }
}