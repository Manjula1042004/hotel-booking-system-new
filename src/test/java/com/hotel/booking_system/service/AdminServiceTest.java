package com.hotel.booking_system.service;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.repository.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AdminService adminService;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;
    private Payment testPayment;
    private List<User> userList;
    private List<Hotel> hotelList;
    private List<Booking> bookingList;
    private List<Payment> paymentList;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setHotel(testHotel);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        testBooking.setCreatedAt(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("400.00"));
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        testPayment.setPaymentDate(LocalDateTime.now());

        userList = Arrays.asList(testUser);
        hotelList = Arrays.asList(testHotel);
        bookingList = Arrays.asList(testBooking);
        paymentList = Arrays.asList(testPayment);
    }

    @Test
    void getTotalUsers_ShouldReturnCount() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long count = adminService.getTotalUsers();

        // Then
        assertEquals(5L, count);
        verify(userRepository).count();
    }

    @Test
    void getTotalHotels_ShouldReturnCount() {
        // Given
        when(hotelRepository.count()).thenReturn(3L);

        // When
        long count = adminService.getTotalHotels();

        // Then
        assertEquals(3L, count);
        verify(hotelRepository).count();
    }

    @Test
    void getTotalBookings_ShouldReturnCount() {
        // Given
        when(bookingRepository.count()).thenReturn(10L);

        // When
        long count = adminService.getTotalBookings();

        // Then
        assertEquals(10L, count);
        verify(bookingRepository).count();
    }

    @Test
    void getTotalRevenue_ShouldReturnRevenue() {
        // Given
        when(paymentRepository.getTotalRevenue()).thenReturn(new BigDecimal("4000.00"));

        // When
        BigDecimal revenue = adminService.getTotalRevenue();

        // Then
        assertEquals(new BigDecimal("4000.00"), revenue);
        verify(paymentRepository).getTotalRevenue();
    }

    @Test
    void getTotalRevenue_ShouldReturnZero_WhenNull() {
        // Given
        when(paymentRepository.getTotalRevenue()).thenReturn(null);

        // When
        BigDecimal revenue = adminService.getTotalRevenue();

        // Then
        assertEquals(BigDecimal.ZERO, revenue);
        verify(paymentRepository).getTotalRevenue();
    }

    @Test
    void getConfirmedBookings_ShouldReturnCount() {
        // Given
        when(bookingRepository.countConfirmedBookings()).thenReturn(5L);

        // When
        long count = adminService.getConfirmedBookings();

        // Then
        assertEquals(5L, count);
        verify(bookingRepository).countConfirmedBookings();
    }

    @Test
    void getAllUsers_ShouldReturnUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<User> result = adminService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getAllHotels_ShouldReturnHotels() {
        // Given
        when(hotelRepository.findAll()).thenReturn(hotelList);

        // When
        List<Hotel> result = adminService.getAllHotels();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(hotelRepository).findAll();
    }

    @Test
    void getAllBookings_ShouldReturnBookings() {
        // Given
        when(bookingRepository.findAll()).thenReturn(bookingList);

        // When
        List<Booking> result = adminService.getAllBookings();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAll();
    }

    @Test
    void getAllPayments_ShouldReturnPayments() {
        // Given
        when(paymentRepository.findAll()).thenReturn(paymentList);

        // When
        List<Payment> result = adminService.getAllPayments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findAll();
    }

    @Test
    void getBookingStatusStats_ShouldReturnStats() {
        // Given
        Booking confirmed = new Booking();
        confirmed.setStatus(Booking.BookingStatus.CONFIRMED);

        Booking cancelled = new Booking();
        cancelled.setStatus(Booking.BookingStatus.CANCELLED);

        Booking completed = new Booking();
        completed.setStatus(Booking.BookingStatus.COMPLETED);

        Booking refunded = new Booking();
        refunded.setStatus(Booking.BookingStatus.REFUNDED);

        List<Booking> bookings = Arrays.asList(confirmed, cancelled, completed, refunded, confirmed);
        when(bookingRepository.findAll()).thenReturn(bookings);

        // When
        Map<String, Long> stats = adminService.getBookingStatusStats();

        // Then
        assertEquals(2L, stats.get("CONFIRMED"));
        assertEquals(1L, stats.get("CANCELLED"));
        assertEquals(1L, stats.get("COMPLETED"));
        assertEquals(1L, stats.get("REFUNDED"));
        verify(bookingRepository).findAll();
    }

    @Test
    void getRevenueAnalytics_ShouldReturnAnalytics() {
        // Given
        Payment todayPayment = new Payment();
        todayPayment.setAmount(new BigDecimal("200.00"));
        todayPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        todayPayment.setPaymentDate(LocalDateTime.now());

        Payment monthPayment = new Payment();
        monthPayment.setAmount(new BigDecimal("300.00"));
        monthPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        monthPayment.setPaymentDate(LocalDateTime.now().minusDays(5));

        List<Payment> payments = Arrays.asList(todayPayment, monthPayment);
        when(paymentRepository.findAll()).thenReturn(payments);
        when(paymentRepository.getTotalRevenue()).thenReturn(new BigDecimal("500.00"));

        // When
        Map<String, BigDecimal> analytics = adminService.getRevenueAnalytics();

        // Then
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("today"));
        assertTrue(analytics.containsKey("monthly"));
        assertTrue(analytics.containsKey("total"));
        verify(paymentRepository).findAll();
        verify(paymentRepository).getTotalRevenue();
    }

    @Test
    void getUserRegistrationStats_ShouldReturnStats() {
        // Given
        User todayUser = new User();
        todayUser.setCreatedAt(LocalDateTime.now());

        User yesterdayUser = new User();
        yesterdayUser.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<User> users = Arrays.asList(todayUser, yesterdayUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        Map<String, Long> stats = adminService.getUserRegistrationStats();

        // Then
        assertNotNull(stats);
        assertEquals(1L, stats.get("today"));
        assertEquals(2L, stats.get("total"));
        verify(userRepository).findAll();
    }

    @Test
    void getRecentBookings_ShouldReturnLimitedBookings() {
        // Given
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Booking booking = new Booking();
            booking.setId((long) i);
            booking.setCreatedAt(LocalDateTime.now().minusHours(i));
            bookings.add(booking);
        }
        when(bookingRepository.findAll()).thenReturn(bookings);

        // When
        List<Booking> recent = adminService.getRecentBookings(3);

        // Then
        assertNotNull(recent);
        assertEquals(3, recent.size());
        verify(bookingRepository).findAll();
    }

    @Test
    void getRecentUsers_ShouldReturnLimitedUsers() {
        // Given
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId((long) i);
            user.setCreatedAt(LocalDateTime.now().minusHours(i));
            users.add(user);
        }
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> recent = adminService.getRecentUsers(3);

        // Then
        assertNotNull(recent);
        assertEquals(3, recent.size());
        verify(userRepository).findAll();
    }

    @Test
    void getTodaysBookings_ShouldReturnCount() {
        // Given
        Booking todayBooking = new Booking();
        todayBooking.setCreatedAt(LocalDateTime.now());

        Booking yesterdayBooking = new Booking();
        yesterdayBooking.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<Booking> bookings = Arrays.asList(todayBooking, yesterdayBooking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        // When
        long count = adminService.getTodaysBookings();

        // Then
        assertEquals(1L, count);
        verify(bookingRepository).findAll();
    }
}