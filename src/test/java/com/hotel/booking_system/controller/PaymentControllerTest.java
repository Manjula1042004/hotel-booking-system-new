package com.hotel.booking_system.controller;

import com.hotel.booking_system.config.TestSecurityConfig;
import com.hotel.booking_system.model.*;
import com.hotel.booking_system.service.BookingService;
import com.hotel.booking_system.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(TestSecurityConfig.class) // Import test security config
@ActiveProfiles("test")
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private BookingService bookingService;

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
        testUser.setRole(User.Role.USER);

        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");

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
        testBooking.setCreatedAt(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId(paymentId);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("400.00"));
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setTransactionId("TXN123456");
        testPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        testPayment.setPaymentDate(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void processPayment_ShouldReturnPaymentView() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        mockMvc.perform(get("/payments/process/{bookingId}", bookingId)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/process"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void processPayment_ShouldRedirectToAccessDenied_WhenNotOwnBooking() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("other@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        mockMvc.perform(get("/payments/process/{bookingId}", bookingId)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void processOnlinePayment_ShouldProcessAndRedirectToSuccess() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));
        when(paymentService.processPayment(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testPayment);

        // When & Then
        mockMvc.perform(post("/payments/process/online")
                        .with(csrf())
                        .principal(principal)
                        .param("bookingId", bookingId.toString())
                        .param("paymentMethod", "VISA")
                        .param("cardNumber", "4111111111111111")
                        .param("cardHolderName", "Test User")
                        .param("expiryDate", "12/25")
                        .param("cvv", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/success?paymentId=" + paymentId));

        verify(paymentService).processPayment(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void processOnlinePayment_ShouldRedirectToProcess_WhenExceptionOccurs() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));
        when(paymentService.processPayment(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Payment failed"));

        // When & Then
        mockMvc.perform(post("/payments/process/online")
                        .with(csrf())
                        .principal(principal)
                        .param("bookingId", bookingId.toString())
                        .param("paymentMethod", "VISA")
                        .param("cardNumber", "4111111111111111")
                        .param("cardHolderName", "Test User")
                        .param("expiryDate", "12/25")
                        .param("cvv", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/process/" + bookingId + "?error=Payment+failed"));

        verify(paymentService).processPayment(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void processCashPayment_ShouldProcessAndRedirectToSuccess() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));
        when(paymentService.processCashPayment(bookingId)).thenReturn(testPayment);

        // When & Then
        mockMvc.perform(post("/payments/process/cash")
                        .with(csrf())
                        .principal(principal)
                        .param("bookingId", bookingId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/success?paymentId=" + paymentId));

        verify(paymentService).processCashPayment(bookingId);
    }

    @Test
    void paymentSuccess_ShouldReturnSuccessView() throws Exception {
        // Given
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(testPayment));

        // When & Then
        mockMvc.perform(get("/payments/success")
                        .param("paymentId", paymentId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/success"))
                .andExpect(model().attributeExists("payment"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    void paymentSuccess_ShouldReturnError_WhenPaymentNotFound() throws Exception {
        // Given
        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/payments/success")
                        .param("paymentId", paymentId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/success"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void paymentFailed_ShouldReturnFailedView() throws Exception {
        // Given
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        mockMvc.perform(get("/payments/failed")
                        .param("bookingId", bookingId.toString())
                        .param("error", "payment_failed"))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/failed"))
                .andExpect(model().attributeExists("booking"))
                .andExpect(model().attributeExists("error"));
    }
}