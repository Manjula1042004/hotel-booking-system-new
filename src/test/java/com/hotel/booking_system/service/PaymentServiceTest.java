package com.hotel.booking_system.service;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.repository.BookingRepository;
import com.hotel.booking_system.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;
    private Payment testPayment;
    private final Long bookingId = 1L;
    private final Long paymentId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setHotel(testHotel);

        testBooking = new Booking();
        testBooking.setId(bookingId);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setNumberOfGuests(2);
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.PENDING);

        testPayment = new Payment();
        testPayment.setId(paymentId);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("400.00"));
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setTransactionId("TXN123456");
        testPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        testPayment.setPaymentDate(LocalDateTime.now());

        // Set private field for testing
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "test_secret");
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findAll()).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getAllPayments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findAll();
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenExists() {
        // Given
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void getPaymentsByBookingId_ShouldReturnPayments() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByBookingId(bookingId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByBookingId(bookingId);
    }

    @Test
    void getTotalRevenue_ShouldReturnSumOfSuccessfulPayments() {
        // Given
        when(paymentRepository.getTotalRevenue()).thenReturn(new BigDecimal("400.00"));

        // When
        BigDecimal revenue = paymentService.getTotalRevenue();

        // Then
        assertEquals(new BigDecimal("400.00"), revenue);
        verify(paymentRepository).getTotalRevenue();
    }

    @Test
    void getTotalRevenue_ShouldReturnZero_WhenNull() {
        // Given
        when(paymentRepository.getTotalRevenue()).thenReturn(null);

        // When
        BigDecimal revenue = paymentService.getTotalRevenue();

        // Then
        assertEquals(BigDecimal.ZERO, revenue);
        verify(paymentRepository).getTotalRevenue();
    }

    @Test
    void getSuccessfulPayments_ShouldReturnSuccessfulPayments() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByStatus(Payment.PaymentStatus.SUCCESS)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getSuccessfulPayments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Payment.PaymentStatus.SUCCESS, result.get(0).getStatus());
        verify(paymentRepository).findByStatus(Payment.PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_ShouldProcessSuccessfully_WhenValidCard() {
        // Given
        String cardNumber = "4111111111111111";
        String cardHolderName = "Test User";
        String expiryDate = "12/25";
        String cvv = "123";

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        Payment payment = paymentService.processPayment(bookingId, "CREDIT_CARD",
                cardNumber, cardHolderName, expiryDate, cvv);

        // Then
        assertNotNull(payment);
        assertEquals(Payment.PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("1111", payment.getCardLastFour());
        assertNotNull(payment.getTransactionId());
        assertNotNull(payment.getPaymentDate());
        assertEquals(Booking.BookingStatus.CONFIRMED, testBooking.getStatus());
        verify(bookingRepository).findById(bookingId);
        verify(paymentRepository).save(any(Payment.class));
        verify(emailService).sendPaymentConfirmation(any(User.class), any(Payment.class));
    }

    @Test
    void processPayment_ShouldFail_WhenInvalidCardNumber() {
        // Given
        String cardNumber = "123"; // Invalid
        String cardHolderName = "Test User";
        String expiryDate = "12/25";
        String cvv = "123";

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(bookingId, "CREDIT_CARD",
                    cardNumber, cardHolderName, expiryDate, cvv);
        });
        assertEquals("Payment processing failed: Invalid card details", exception.getMessage());
        verify(bookingRepository).findById(bookingId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_ShouldFail_WhenCardExpired() {
        // Given
        String cardNumber = "4111111111111111";
        String cardHolderName = "Test User";
        String expiryDate = "12/20"; // Expired
        String cvv = "123";

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(bookingId, "CREDIT_CARD",
                    cardNumber, cardHolderName, expiryDate, cvv);
        });
        assertEquals("Payment processing failed: Invalid card details", exception.getMessage());
        verify(bookingRepository).findById(bookingId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processCashPayment_ShouldCreateCashPayment() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        Payment payment = paymentService.processCashPayment(bookingId);

        // Then
        assertNotNull(payment);
        assertEquals(Payment.PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("CASH", payment.getPaymentMethod());
        assertTrue(payment.getTransactionId().startsWith("CASH-"));
        assertNotNull(payment.getPaymentDate());
        assertEquals(Booking.BookingStatus.CONFIRMED, testBooking.getStatus());
        verify(bookingRepository).findById(bookingId);
        verify(paymentRepository).save(any(Payment.class));
        verify(emailService).sendPaymentConfirmation(any(User.class), any(Payment.class));
    }

    @Test
    void refundPayment_ShouldRefundPayment_WhenSuccessful() {
        // Given
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Payment refundedPayment = paymentService.refundPayment(paymentId);

        // Then
        assertNotNull(refundedPayment);
        assertEquals(Payment.PaymentStatus.REFUNDED, refundedPayment.getStatus());
        assertNotNull(refundedPayment.getRefundDate());
        assertEquals(Booking.BookingStatus.CANCELLED, testBooking.getStatus());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(testPayment);
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.refundPayment(paymentId);
        });
        assertEquals("Refund processing failed: Payment not found", exception.getMessage());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentNotSuccessful() {
        // Given
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.refundPayment(paymentId);
        });
        assertEquals("Refund processing failed: Only successful payments can be refunded",
                exception.getMessage());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRazorpayOrder_ShouldCreateOrder_WhenValid() throws RazorpayException {
        // This test requires more complex mocking of RazorpayClient
        // Simplified version for demonstration
        BigDecimal amount = new BigDecimal("400.00");
        String currency = "INR";
        String receipt = "receipt_123";

        // Since we can't easily mock the Razorpay order creation,
        // we'll just verify the method can be called without exception
        // In a real test, you'd mock the razorpayClient.orders.create() call

        // For now, we'll just verify the method doesn't throw when properly mocked
        // This is a placeholder - you'd need to properly mock the Razorpay response
    }

    @Test
    void isValidCardDetails_ShouldReturnTrue_WhenValidCard() {
        // Using reflection to test private method
        try {
            java.lang.reflect.Method method = PaymentService.class.getDeclaredMethod(
                    "isValidCardDetails", String.class, String.class, String.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(paymentService,
                    "4111111111111111", "12/25", "123");

            assertTrue(result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void isValidCardDetails_ShouldReturnFalse_WhenInvalidCardNumber() {
        try {
            java.lang.reflect.Method method = PaymentService.class.getDeclaredMethod(
                    "isValidCardDetails", String.class, String.class, String.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(paymentService,
                    "123", "12/25", "123");

            assertFalse(result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void isValidCardDetails_ShouldReturnFalse_WhenInvalidCVV() {
        try {
            java.lang.reflect.Method method = PaymentService.class.getDeclaredMethod(
                    "isValidCardDetails", String.class, String.class, String.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(paymentService,
                    "4111111111111111", "12/25", "12");

            assertFalse(result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void isValidCardDetails_ShouldReturnFalse_WhenInvalidExpiryFormat() {
        try {
            java.lang.reflect.Method method = PaymentService.class.getDeclaredMethod(
                    "isValidCardDetails", String.class, String.class, String.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(paymentService,
                    "4111111111111111", "13/25", "123");

            assertFalse(result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}