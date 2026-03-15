package com.hotel.booking_system.repository;

import com.hotel.booking_system.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    private Hotel testHotel;
    private Room testRoom;
    private User testUser;
    private final LocalDate checkInDate = LocalDate.now().plusDays(1);
    private final LocalDate checkOutDate = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");
        testHotel.setRating(4.5);
        testHotel.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testHotel);

        testRoom = new Room();
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
        entityManager.persist(testRoom);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setProvider("LOCAL");
        testUser.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testUser);
    }

    @Test
    void findByHotelId_ShouldReturnRooms() {
        // When
        List<Room> rooms = roomRepository.findByHotelId(testHotel.getId());

        // Then
        assertFalse(rooms.isEmpty());
        assertEquals(1, rooms.size());
        assertEquals(testRoom.getId(), rooms.get(0).getId());
    }

    @Test
    void findAvailableRoomsByCityAndDates_ShouldReturnAvailableRooms() {
        // Given - No conflicting bookings

        // When
        List<Room> availableRooms = roomRepository.findAvailableRoomsByCityAndDates(
                "Test City", checkInDate, checkOutDate);

        // Then
        assertFalse(availableRooms.isEmpty());
        assertEquals(1, availableRooms.size());
        assertEquals(testRoom.getId(), availableRooms.get(0).getId());
    }

    @Test
    void findAvailableRoomsByCityAndDates_ShouldExcludeRoomsWithConflictingBookings() {
        // Given - Create conflicting booking
        Booking conflictingBooking = new Booking();
        conflictingBooking.setUser(testUser);
        conflictingBooking.setRoom(testRoom);
        conflictingBooking.setCheckInDate(checkInDate);
        conflictingBooking.setCheckOutDate(checkOutDate);
        conflictingBooking.setNumberOfGuests(2);
        conflictingBooking.setTotalAmount(new BigDecimal("400.00"));
        conflictingBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        conflictingBooking.setCreatedAt(LocalDateTime.now());
        entityManager.persist(conflictingBooking);
        entityManager.flush();

        // When
        List<Room> availableRooms = roomRepository.findAvailableRoomsByCityAndDates(
                "Test City", checkInDate, checkOutDate);

        // Then
        assertTrue(availableRooms.isEmpty());
    }

    @Test
    void findAvailableRoomsByCityAndDates_ShouldIncludeRoomsWithCancelledBookings() {
        // Given - Create cancelled booking (should not block availability)
        Booking cancelledBooking = new Booking();
        cancelledBooking.setUser(testUser);
        cancelledBooking.setRoom(testRoom);
        cancelledBooking.setCheckInDate(checkInDate);
        cancelledBooking.setCheckOutDate(checkOutDate);
        cancelledBooking.setNumberOfGuests(2);
        cancelledBooking.setTotalAmount(new BigDecimal("400.00"));
        cancelledBooking.setStatus(Booking.BookingStatus.CANCELLED);
        cancelledBooking.setCreatedAt(LocalDateTime.now());
        entityManager.persist(cancelledBooking);
        entityManager.flush();

        // When
        List<Room> availableRooms = roomRepository.findAvailableRoomsByCityAndDates(
                "Test City", checkInDate, checkOutDate);

        // Then
        assertFalse(availableRooms.isEmpty());
        assertEquals(1, availableRooms.size());
        assertEquals(testRoom.getId(), availableRooms.get(0).getId());
    }

    @Test
    void findAvailableRoomsByHotelAndDates_ShouldReturnAvailableRooms() {
        // When
        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelAndDates(
                testHotel.getId(), checkInDate, checkOutDate);

        // Then
        assertFalse(availableRooms.isEmpty());
        assertEquals(1, availableRooms.size());
        assertEquals(testRoom.getId(), availableRooms.get(0).getId());
    }

    @Test
    void findAvailableRoomsByHotelAndDates_ShouldExcludeRoomsWithConflictingBookings() {
        // Given - Create conflicting booking
        Booking conflictingBooking = new Booking();
        conflictingBooking.setUser(testUser);
        conflictingBooking.setRoom(testRoom);
        conflictingBooking.setCheckInDate(checkInDate);
        conflictingBooking.setCheckOutDate(checkOutDate);
        conflictingBooking.setNumberOfGuests(2);
        conflictingBooking.setTotalAmount(new BigDecimal("400.00"));
        conflictingBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        conflictingBooking.setCreatedAt(LocalDateTime.now());
        entityManager.persist(conflictingBooking);
        entityManager.flush();

        // When
        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelAndDates(
                testHotel.getId(), checkInDate, checkOutDate);

        // Then
        assertTrue(availableRooms.isEmpty());
    }
}