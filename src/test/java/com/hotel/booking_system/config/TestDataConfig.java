package com.hotel.booking_system.config;

import com.hotel.booking_system.model.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TestConfiguration
public class TestDataConfig {

    @Bean
    @Primary
    public User testUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(false);
        user.setFailedLoginAttempts(0);
        user.setProvider("LOCAL");
        user.setTwoFactorEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    @Bean
    @Primary
    public User testAdmin() {
        User admin = new User();
        admin.setId(2L);
        admin.setName("Test Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("encodedPassword");
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setAccountNonExpired(true);
        admin.setAccountNonLocked(true);
        admin.setCredentialsNonExpired(true);
        admin.setEmailVerified(true);
        admin.setFailedLoginAttempts(0);
        admin.setProvider("LOCAL");
        admin.setTwoFactorEnabled(false);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        return admin;
    }

    @Bean
    @Primary
    public Hotel testHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Test City");
        hotel.setAddress("123 Test St");
        hotel.setDescription("A test hotel");
        hotel.setRating(4.5);
        hotel.setImageUrl("http://test.com/image.jpg");
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        return hotel;
    }

    @Bean
    @Primary
    public Room testRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setRoomType("Deluxe");
        room.setPrice(new BigDecimal("200.00"));
        room.setCapacity(2);
        room.setTotalRooms(5);
        room.setAvailable(true);
        room.setDescription("Test room");
        room.setAmenities("WiFi, TV, AC");
        room.setHotel(testHotel());
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        return room;
    }

    @Bean
    @Primary
    public Booking testBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(testUser());
        booking.setRoom(testRoom());
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setNumberOfGuests(2);
        booking.setRoomCount(1);
        booking.setTotalAmount(new BigDecimal("400.00"));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        return booking;
    }

    @Bean
    @Primary
    public Payment testPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBooking(testBooking());
        payment.setAmount(new BigDecimal("400.00"));
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setCardLastFour("1111");
        payment.setTransactionId("TXN123456");
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }
}