package com.hotel.booking_system.service;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.repository.BookingRepository;
import com.hotel.booking_system.repository.PaymentRepository;
import com.hotel.booking_system.repository.RoomRepository;
import com.hotel.booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;
    private Payment testPayment;
    private final Long userId = 1L;
    private final Long roomId = 1L;
    private final Long bookingId = 1L;
    private final LocalDate checkInDate = LocalDate.now().plusDays(1);
    private final LocalDate checkOutDate = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testHotel = new Hotel();
        testHotel.setId(1L);
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
        testRoom.setHotel(testHotel);

        testBooking = new Booking();
        testBooking.setId(bookingId);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(checkInDate);
        testBooking.setCheckOutDate(checkOutDate);
        testBooking.setNumberOfGuests(2);
        testBooking.setRoomCount(1);
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.PENDING);
        testBooking.setCreatedAt(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("400.00"));
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
    }

    // ... rest of the test methods remain the same but fix the date issue at line 306

    @Test
    void cancelBooking_ShouldCancelBooking_WhenValid() {
        // Given - Fixed: Use plusDays instead of plusHours for LocalDate
        testBooking.setCheckInDate(LocalDate.now().plusDays(2)); // More than 1 day in future
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Arrays.asList(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Booking cancelledBooking = bookingService.cancelBooking(bookingId);

        // Then
        assertNotNull(cancelledBooking);
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());
        assertNotNull(cancelledBooking.getCancellationDate());
        assertEquals(Payment.PaymentStatus.REFUNDED, testPayment.getStatus());
        assertNotNull(testPayment.getRefundDate());
        verify(bookingRepository).findById(bookingId);
        verify(paymentRepository).findByBookingId(bookingId);
        verify(paymentRepository).save(testPayment);
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void cancelBooking_ShouldThrowException_WhenTooCloseToCheckIn() {
        // Given - Fixed: Use plusDays(0) for same day, cannot use plusHours with LocalDate
        testBooking.setCheckInDate(LocalDate.now().plusDays(0)); // Today - less than 1 day
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking(bookingId);
        });
        assertTrue(exception.getMessage().contains("Cancellation not allowed"));
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository, never()).save(any());
    }
}