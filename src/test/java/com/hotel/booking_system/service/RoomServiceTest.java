package com.hotel.booking_system.service;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Hotel testHotel;
    private Room testRoom;
    private final Long roomId = 1L;
    private final Long hotelId = 1L;
    private final LocalDate checkInDate = LocalDate.now().plusDays(1);
    private final LocalDate checkOutDate = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(hotelId);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");

        testRoom = new Room();
        testRoom.setId(roomId);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setCapacity(2);
        testRoom.setTotalRooms(5);
        testRoom.setAvailable(true);
        testRoom.setDescription("Test room");
        testRoom.setAmenities("WiFi, TV, AC");
        testRoom.setHotel(testHotel);
        testRoom.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createRoom_ShouldSaveAndReturnRoom() {
        // Given
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // When
        Room createdRoom = roomService.createRoom(testRoom);

        // Then
        assertNotNull(createdRoom);
        assertEquals(testRoom.getId(), createdRoom.getId());
        assertEquals(testRoom.getRoomNumber(), createdRoom.getRoomNumber());
        verify(roomRepository).save(testRoom);
    }

    @Test
    void getRoomsByHotelId_ShouldReturnRooms() {
        // Given
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findByHotelId(hotelId)).thenReturn(rooms);

        // When
        List<Room> result = roomService.getRoomsByHotelId(hotelId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(hotelId, result.get(0).getHotel().getId());
        verify(roomRepository).findByHotelId(hotelId);
    }

    @Test
    void getRoomById_ShouldReturnRoom_WhenExists() {
        // Given
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));

        // When
        Room result = roomService.getRoomById(roomId);

        // Then
        assertNotNull(result);
        assertEquals(roomId, result.getId());
        verify(roomRepository).findById(roomId);
    }

    @Test
    void getRoomById_ShouldReturnNull_WhenNotExists() {
        // Given
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When
        Room result = roomService.getRoomById(roomId);

        // Then
        assertNull(result);
        verify(roomRepository).findById(roomId);
    }

    @Test
    void updateRoom_ShouldUpdateAndReturnRoom_WhenExists() {
        // Given
        Room updateDetails = new Room();
        updateDetails.setRoomNumber("202");
        updateDetails.setRoomType("Suite");
        updateDetails.setPrice(new BigDecimal("300.00"));
        updateDetails.setCapacity(4);
        updateDetails.setDescription("Updated room");
        updateDetails.setAmenities("WiFi, TV, AC, Mini Bar");
        updateDetails.setAvailable(false);
        updateDetails.setTotalRooms(3);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // When
        Room updatedRoom = roomService.updateRoom(roomId, updateDetails);

        // Then
        assertNotNull(updatedRoom);
        assertEquals("202", testRoom.getRoomNumber());
        assertEquals("Suite", testRoom.getRoomType());
        assertEquals(new BigDecimal("300.00"), testRoom.getPrice());
        assertEquals(4, testRoom.getCapacity());
        assertEquals("Updated room", testRoom.getDescription());
        assertEquals("WiFi, TV, AC, Mini Bar", testRoom.getAmenities());
        assertFalse(testRoom.getAvailable());
        assertEquals(3, testRoom.getTotalRooms());
        verify(roomRepository).findById(roomId);
        verify(roomRepository).save(testRoom);
    }

    @Test
    void updateRoom_ShouldThrowException_WhenNotExists() {
        // Given
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roomService.updateRoom(roomId, testRoom);
        });
        assertEquals("Room not found", exception.getMessage());
        verify(roomRepository).findById(roomId);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void deleteRoom_ShouldDeleteRoom() {
        // Given
        doNothing().when(roomRepository).deleteById(roomId);

        // When
        roomService.deleteRoom(roomId);

        // Then
        verify(roomRepository).deleteById(roomId);
    }

    @Test
    void getAvailableRoomsByCityAndDates_ShouldReturnAvailableRooms() {
        // Given
        String city = "Test City";
        List<Room> availableRooms = Arrays.asList(testRoom);
        when(roomRepository.findAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate))
                .thenReturn(availableRooms);

        // When
        List<Room> result = roomService.getAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(city, result.get(0).getHotel().getCity());
        verify(roomRepository).findAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate);
    }

    @Test
    void getAvailableRoomsByCityAndDates_ShouldReturnEmpty_WhenCityEmpty() {
        // Given
        String city = "";

        // When
        List<Room> result = roomService.getAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomRepository, never()).findAvailableRoomsByCityAndDates(any(), any(), any());
    }

    @Test
    void getAvailableRoomsByCityAndDates_ShouldReturnEmpty_WhenExceptionOccurs() {
        // Given
        String city = "Test City";
        when(roomRepository.findAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate))
                .thenThrow(new RuntimeException("Database error"));

        // When
        List<Room> result = roomService.getAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomRepository).findAvailableRoomsByCityAndDates(city, checkInDate, checkOutDate);
    }

    @Test
    void getAvailableRoomsByHotelAndDates_ShouldReturnAvailableRooms() {
        // Given
        List<Room> availableRooms = Arrays.asList(testRoom);
        when(roomRepository.findAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate))
                .thenReturn(availableRooms);

        // When
        List<Room> result = roomService.getAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(hotelId, result.get(0).getHotel().getId());
        verify(roomRepository).findAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);
    }

    @Test
    void getAvailableRoomsByHotelAndDates_ShouldReturnEmpty_WhenExceptionOccurs() {
        // Given
        when(roomRepository.findAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate))
                .thenThrow(new RuntimeException("Database error"));

        // When
        List<Room> result = roomService.getAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomRepository).findAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);
    }
}