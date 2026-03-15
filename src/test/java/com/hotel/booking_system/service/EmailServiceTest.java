package com.hotel.booking_system.service;

import com.hotel.booking_system.model.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setHotel(testHotel);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setNumberOfGuests(2);
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.CONFIRMED);

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("400.00"));
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setTransactionId("TXN123456");
        testPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        testPayment.setPaymentDate(LocalDateTime.now());
    }

    @Test
    void sendBookingConfirmation_ShouldSendEmail() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/booking-confirmation"), any(Context.class)))
                .thenReturn("<html>Booking Confirmation</html>");

        // When
        emailService.sendBookingConfirmation(testUser, testBooking);

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/booking-confirmation"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBookingConfirmation_ShouldHandleException() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/booking-confirmation"), any(Context.class)))
                .thenThrow(new RuntimeException("Template error"));

        // When - Should not throw exception
        emailService.sendBookingConfirmation(testUser, testBooking);

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/booking-confirmation"), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPaymentConfirmation_ShouldSendEmail() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/payment-confirmation"), any(Context.class)))
                .thenReturn("<html>Payment Confirmation</html>");

        // When
        emailService.sendPaymentConfirmation(testUser, testPayment);

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/payment-confirmation"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPaymentConfirmation_ShouldHandleException() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/payment-confirmation"), any(Context.class)))
                .thenThrow(new RuntimeException("Template error"));

        // When - Should not throw exception
        emailService.sendPaymentConfirmation(testUser, testPayment);

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/payment-confirmation"), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}