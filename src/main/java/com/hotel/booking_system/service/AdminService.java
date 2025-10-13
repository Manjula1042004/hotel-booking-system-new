package com.hotel.booking_system.service;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RoomRepository roomRepository;

    // Basic Count Methods
    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalHotels() {
        return hotelRepository.count();
    }

    public long getTotalBookings() {
        return bookingRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = paymentRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public long getConfirmedBookings() {
        Long count = bookingRepository.countConfirmedBookings();
        return count != null ? count : 0;
    }

    // Get All Methods
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Enhanced Statistics
    public Map<String, Long> getBookingStatusStats() {
        Map<String, Long> stats = new HashMap<>();
        List<Booking> allBookings = getAllBookings();

        stats.put("CONFIRMED", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .count());
        stats.put("COMPLETED", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                .count());
        stats.put("CANCELLED", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED)
                .count());
        stats.put("REFUNDED", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.REFUNDED)
                .count());

        return stats;
    }

    public Map<String, BigDecimal> getRevenueAnalytics() {
        Map<String, BigDecimal> analytics = new HashMap<>();
        List<Payment> payments = getAllPayments();

        // Today's revenue
        BigDecimal todayRevenue = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS &&
                        p.getPaymentDate() != null &&
                        p.getPaymentDate().toLocalDate().equals(LocalDate.now()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // This month's revenue
        BigDecimal monthlyRevenue = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS &&
                        p.getPaymentDate() != null &&
                        p.getPaymentDate().getMonth() == LocalDateTime.now().getMonth())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.put("today", todayRevenue);
        analytics.put("monthly", monthlyRevenue);
        analytics.put("total", getTotalRevenue());

        return analytics;
    }

    public Map<String, Long> getUserRegistrationStats() {
        Map<String, Long> stats = new HashMap<>();
        List<User> users = getAllUsers();

        long todayRegistrations = users.stream()
                .filter(u -> u.getCreatedAt() != null &&
                        u.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        long totalRegistrations = users.size();

        stats.put("today", todayRegistrations);
        stats.put("total", totalRegistrations);

        return stats;
    }

    public List<Booking> getRecentBookings(int limit) {
        return bookingRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    public List<User> getRecentUsers(int limit) {
        return userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    public long getTodaysBookings() {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getCreatedAt() != null &&
                        b.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();
    }
}