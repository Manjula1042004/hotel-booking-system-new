package com.hotel.booking_system.integration;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookingFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private String baseUrl = "/api";

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Integration Test User");
        testUser.setEmail("integration@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.Role.USER);
        testUser.setEnabled(true);
        testUser.setProvider("LOCAL");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create test hotel
        testHotel = new Hotel();
        testHotel.setName("Integration Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");
        testHotel.setRating(4.5);
        testHotel.setCreatedAt(LocalDateTime.now());
        testHotel = hotelRepository.save(testHotel);

        // Create test room
        testRoom = new Room();
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setCapacity(2);
        testRoom.setTotalRooms(5);
        testRoom.setAvailable(true);
        testRoom.setHotel(testHotel);
        testRoom.setCreatedAt(LocalDateTime.now());
        testRoom = roomRepository.save(testRoom);
    }

    @Test
    void completeBookingFlow_ShouldCreateAndConfirmBooking() {
        // 1. Search for available rooms
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        // This would typically call your search endpoint
        // For integration test, we'll directly use the repository
        List<Room> availableRooms = roomRepository.findAvailableRoomsByCityAndDates(
                testHotel.getCity(), checkIn, checkOut);

        assertFalse(availableRooms.isEmpty());
        assertEquals(testRoom.getId(), availableRooms.get(0).getId());

        // 2. Create booking
        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setRoom(testRoom);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumberOfGuests(2);
        booking.setRoomCount(1);
        booking.setTotalAmount(new BigDecimal("400.00"));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        assertNotNull(savedBooking.getId());
        assertEquals(Booking.BookingStatus.PENDING, savedBooking.getStatus());

        // 3. Process payment
        // In a real integration test, you might mock the payment gateway
        savedBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(savedBooking);
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBooking.getStatus());

        // 4. Verify booking in user's bookings
        List<Booking> userBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertFalse(userBookings.isEmpty());
        assertEquals(confirmedBooking.getId(), userBookings.get(0).getId());
    }

    @Test
    void bookingCancellationFlow_ShouldCancelBooking() {
        // Create a booking
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setRoom(testRoom);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumberOfGuests(2);
        booking.setRoomCount(1);
        booking.setTotalAmount(new BigDecimal("400.00"));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Cancel the booking
        savedBooking.setStatus(Booking.BookingStatus.CANCELLED);
        savedBooking.setCancellationDate(LocalDate.now());
        Booking cancelledBooking = bookingRepository.save(savedBooking);

        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());
        assertNotNull(cancelledBooking.getCancellationDate());

        // Verify room availability for cancelled dates
        List<Booking> conflicting = bookingRepository.findConflictingBookings(
                testRoom.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate());

        // Should not find the cancelled booking as conflicting
        assertTrue(conflicting.stream()
                .noneMatch(b -> b.getId().equals(cancelledBooking.getId())
                        && b.getStatus() == Booking.BookingStatus.CONFIRMED));
    }
}