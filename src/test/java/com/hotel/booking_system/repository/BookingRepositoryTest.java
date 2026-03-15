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
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setProvider("LOCAL");
        testUser.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testUser);

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
        testRoom.setHotel(testHotel);
        testRoom.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testRoom);

        testBooking = new Booking();
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setNumberOfGuests(2);
        testBooking.setRoomCount(1);
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.PENDING);
        testBooking.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnUserBookings() {
        // Given
        entityManager.persist(testBooking);
        entityManager.flush();

        // When
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        // Then
        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals(testBooking.getId(), bookings.get(0).getId());
        assertEquals(testUser.getId(), bookings.get(0).getUser().getId());
    }

    @Test
    void findConflictingBookings_ShouldReturnConflictingBookings() {
        // Given
        entityManager.persist(testBooking);
        entityManager.flush();

        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(4);

        // When
        List<Booking> conflicting = bookingRepository.findConflictingBookings(
                testRoom.getId(), checkIn, checkOut);

        // Then
        assertFalse(conflicting.isEmpty());
        assertEquals(1, conflicting.size());
        assertEquals(testBooking.getId(), conflicting.get(0).getId());
    }

    @Test
    void findConflictingBookings_ShouldNotReturn_WhenNoConflict() {
        // Given
        entityManager.persist(testBooking);
        entityManager.flush();

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        // When
        List<Booking> conflicting = bookingRepository.findConflictingBookings(
                testRoom.getId(), checkIn, checkOut);

        // Then
        assertTrue(conflicting.isEmpty());
    }

    @Test
    void findByStatus_ShouldReturnBookingsWithStatus() {
        // Given
        entityManager.persist(testBooking);

        Booking confirmedBooking = new Booking();
        confirmedBooking.setUser(testUser);
        confirmedBooking.setRoom(testRoom);
        confirmedBooking.setCheckInDate(LocalDate.now().plusDays(5));
        confirmedBooking.setCheckOutDate(LocalDate.now().plusDays(7));
        confirmedBooking.setNumberOfGuests(2);
        confirmedBooking.setTotalAmount(new BigDecimal("400.00"));
        confirmedBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        confirmedBooking.setCreatedAt(LocalDateTime.now());
        entityManager.persist(confirmedBooking);

        entityManager.flush();

        // When
        List<Booking> pendingBookings = bookingRepository.findByStatus(Booking.BookingStatus.PENDING);
        List<Booking> confirmedBookings = bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED);

        // Then
        assertEquals(1, pendingBookings.size());
        assertEquals(Booking.BookingStatus.PENDING, pendingBookings.get(0).getStatus());

        assertEquals(1, confirmedBookings.size());
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBookings.get(0).getStatus());
    }

    @Test
    void countConfirmedBookings_ShouldReturnCorrectCount() {
        // Given
        entityManager.persist(testBooking); // PENDING

        Booking confirmed1 = new Booking();
        confirmed1.setUser(testUser);
        confirmed1.setRoom(testRoom);
        confirmed1.setCheckInDate(LocalDate.now().plusDays(5));
        confirmed1.setCheckOutDate(LocalDate.now().plusDays(7));
        confirmed1.setNumberOfGuests(2);
        confirmed1.setTotalAmount(new BigDecimal("400.00"));
        confirmed1.setStatus(Booking.BookingStatus.CONFIRMED);
        confirmed1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(confirmed1);

        Booking confirmed2 = new Booking();
        confirmed2.setUser(testUser);
        confirmed2.setRoom(testRoom);
        confirmed2.setCheckInDate(LocalDate.now().plusDays(10));
        confirmed2.setCheckOutDate(LocalDate.now().plusDays(12));
        confirmed2.setNumberOfGuests(2);
        confirmed2.setTotalAmount(new BigDecimal("400.00"));
        confirmed2.setStatus(Booking.BookingStatus.CONFIRMED);
        confirmed2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(confirmed2);

        entityManager.flush();

        // When
        Long count = bookingRepository.countConfirmedBookings();

        // Then
        assertEquals(2L, count);
    }

    @Test
    void findAllOrderByCreatedAtDesc_ShouldReturnBookingsInOrder() {
        // Given
        entityManager.persist(testBooking);

        Booking olderBooking = new Booking();
        olderBooking.setUser(testUser);
        olderBooking.setRoom(testRoom);
        olderBooking.setCheckInDate(LocalDate.now().plusDays(10));
        olderBooking.setCheckOutDate(LocalDate.now().plusDays(12));
        olderBooking.setNumberOfGuests(2);
        olderBooking.setTotalAmount(new BigDecimal("400.00"));
        olderBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        olderBooking.setCreatedAt(LocalDateTime.now().minusDays(5));
        entityManager.persist(olderBooking);

        entityManager.flush();

        // When
        List<Booking> bookings = bookingRepository.findAllOrderByCreatedAtDesc();

        // Then
        assertFalse(bookings.isEmpty());
        assertTrue(bookings.get(0).getCreatedAt().isAfter(bookings.get(1).getCreatedAt()) ||
                bookings.get(0).getCreatedAt().equals(bookings.get(1).getCreatedAt()));
    }
}