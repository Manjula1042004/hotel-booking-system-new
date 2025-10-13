package com.hotel.booking_system.service;

import com.hotel.booking_system.model.Booking;
import com.hotel.booking_system.model.Payment;
import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.model.User;
import com.hotel.booking_system.repository.BookingRepository;
import com.hotel.booking_system.repository.PaymentRepository;
import com.hotel.booking_system.repository.RoomRepository;
import com.hotel.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    // ===== BOOKING CREATION WITH COMPREHENSIVE VALIDATION =====
    @Transactional
    public Booking createBooking(Long userId, Long roomId, LocalDate checkInDate,
                                 LocalDate checkOutDate, Integer numberOfGuests,
                                 Integer roomCount, String specialRequests) {

        System.out.println("🎯 === BOOKING CREATION DEBUG START ===");
        System.out.println("📝 Parameters received:");
        System.out.println("   👤 User ID: " + userId);
        System.out.println("   🏨 Room ID: " + roomId);
        System.out.println("   📅 Check-in: " + checkInDate);
        System.out.println("   📅 Check-out: " + checkOutDate);
        System.out.println("   👥 Guests: " + numberOfGuests);
        System.out.println("   🚪 Rooms: " + roomCount);
        System.out.println("   💬 Special Requests: " + specialRequests);

        try {
            // Validate basic parameters
            System.out.println("🔍 Step 1: Validating parameters...");
            if (userId == null || roomId == null || checkInDate == null || checkOutDate == null) {
                throw new RuntimeException("Missing required booking parameters");
            }

            if (numberOfGuests == null || numberOfGuests < 1 || numberOfGuests > 20) {
                throw new RuntimeException("Number of guests must be between 1 and 20");
            }

            if (roomCount == null || roomCount < 1 || roomCount > 10) {
                throw new RuntimeException("Number of rooms must be between 1 and 10");
            }

            // Get user and room
            System.out.println("🔍 Step 2: Fetching user and room...");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ User not found with ID: " + userId);
                        return new RuntimeException("User not found with ID: " + userId);
                    });

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> {
                        System.err.println("❌ Room not found with ID: " + roomId);
                        return new RuntimeException("Room not found with ID: " + roomId);
                    });

            System.out.println("✅ Found user: " + user.getEmail() + " and room: " + room.getRoomNumber());
            System.out.println("   Hotel: " + room.getHotel().getName());
            System.out.println("   Room available: " + room.getAvailable());

            // Check room availability
            if (!room.getAvailable()) {
                System.err.println("❌ Room " + room.getRoomNumber() + " is not available for booking");
                throw new RuntimeException("Room " + room.getRoomNumber() + " is not available for booking");
            }

            // Validate dates
            System.out.println("🔍 Step 3: Validating dates...");
            LocalDate today = LocalDate.now();
            System.out.println("   Today: " + today);
            System.out.println("   Check-in: " + checkInDate);

            if (checkInDate.isBefore(today)) {
                System.err.println("❌ Check-in date cannot be in the past");
                throw new RuntimeException("Check-in date cannot be in the past");
            }

            if (checkInDate.isAfter(checkOutDate) || checkInDate.equals(checkOutDate)) {
                System.err.println("❌ Check-out date must be after check-in date");
                throw new RuntimeException("Check-out date must be after check-in date");
            }

            // Check guest capacity
            System.out.println("🔍 Step 4: Checking guest capacity...");
            int maxCapacity = room.getCapacity() * roomCount;
            System.out.println("   Room capacity: " + room.getCapacity());
            System.out.println("   Max capacity for " + roomCount + " rooms: " + maxCapacity);
            System.out.println("   Requested guests: " + numberOfGuests);

            if (numberOfGuests > maxCapacity) {
                System.err.println("❌ Guest capacity exceeded");
                throw new RuntimeException("Number of guests (" + numberOfGuests +
                        ") exceeds room capacity. Maximum: " + maxCapacity + " guests for " + roomCount + " rooms");
            }

            // Check room availability for dates
            System.out.println("🔍 Step 5: Checking room availability for dates...");
            int availableRooms = getAvailableRoomCount(roomId, checkInDate, checkOutDate);
            System.out.println("   Available rooms for these dates: " + availableRooms);

            if (roomCount > availableRooms) {
                System.err.println("❌ Not enough rooms available");
                throw new RuntimeException("Only " + availableRooms + " room(s) available for selected dates. Please reduce room count or choose different dates.");
            }

            // Calculate total amount
            System.out.println("🔍 Step 6: Calculating total amount...");
            long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            System.out.println("   Number of nights: " + numberOfNights);

            if (numberOfNights <= 0) {
                throw new RuntimeException("Invalid date range");
            }

            BigDecimal totalAmount = room.getPrice()
                    .multiply(BigDecimal.valueOf(numberOfNights))
                    .multiply(BigDecimal.valueOf(roomCount));

            System.out.println("   Room price per night: $" + room.getPrice());
            System.out.println("   Total amount: $" + totalAmount + " for " + numberOfNights + " nights");

            // Create and save booking
            System.out.println("🔍 Step 7: Creating booking object...");
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRoom(room);
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setNumberOfGuests(numberOfGuests);
            booking.setRoomCount(roomCount);
            booking.setTotalAmount(totalAmount);
            booking.setSpecialRequests(specialRequests);
            booking.setStatus(Booking.BookingStatus.PENDING);

            System.out.println("💾 Saving booking with status: " + booking.getStatus());
            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("✅ Booking created successfully with ID: " + savedBooking.getId());

            // Create pending payment record
            System.out.println("🔍 Step 8: Creating pending payment...");
            Payment payment = new Payment();
            payment.setBooking(savedBooking);
            payment.setAmount(totalAmount);
            payment.setPaymentMethod("PENDING");
            payment.setTransactionId("PENDING-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus(Payment.PaymentStatus.PENDING);

            paymentRepository.save(payment);
            System.out.println("✅ Pending payment created for booking ID: " + savedBooking.getId());

            System.out.println("🎉 === BOOKING CREATION COMPLETED SUCCESSFULLY ===");
            return savedBooking;

        } catch (Exception e) {
            System.err.println("❌ === BOOKING CREATION FAILED ===");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Error type: " + e.getClass().getName());
            e.printStackTrace();

            // Return user-friendly error message
            String userMessage = "Booking failed. Please try again.";
            if (e.getMessage().contains("Data truncated")) {
                userMessage = "System error: Database configuration issue. Please contact support.";
            } else if (e.getMessage().contains("could not execute statement")) {
                userMessage = "System temporarily unavailable. Please try again in a moment.";
            } else if (e.getMessage().contains("constraint")) {
                userMessage = "Database constraint violation. Please check your input.";
            }

            System.err.println("   User message: " + userMessage);
            throw new RuntimeException(userMessage);
        }
    }

    // ===== COMPLETE PAYMENT PROCESS =====
    @Transactional
    public Booking completePayment(Long bookingId, String paymentMethod, String transactionId) {
        System.out.println("💰 === PAYMENT COMPLETION DEBUG START ===");
        System.out.println("   Booking ID: " + bookingId);
        System.out.println("   Payment Method: " + paymentMethod);
        System.out.println("   Transaction ID: " + transactionId);

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        System.err.println("❌ Booking not found for payment: " + bookingId);
                        return new RuntimeException("Booking not found");
                    });

            // Update payment record
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> {
                        System.err.println("❌ Payment not found for booking: " + bookingId);
                        return new RuntimeException("Payment not found");
                    });

            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId(transactionId);
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());

            paymentRepository.save(payment);
            System.out.println("✅ Payment updated successfully");

            // Update booking status
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            Booking confirmedBooking = bookingRepository.save(booking);
            System.out.println("✅ Booking status updated to CONFIRMED");

            // Send confirmation email
            try {
                emailService.sendBookingConfirmation(booking.getUser(), confirmedBooking);
                System.out.println("✅ Booking confirmation email sent to: " + booking.getUser().getEmail());
            } catch (Exception e) {
                System.err.println("⚠ Failed to send booking confirmation email: " + e.getMessage());
            }

            System.out.println("🎉 === PAYMENT COMPLETION FINISHED ===");
            return confirmedBooking;

        } catch (Exception e) {
            System.err.println("❌ PAYMENT COMPLETION FAILED: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment completion failed: " + e.getMessage());
        }
    }

    // ===== BASIC BOOKING METHODS =====
    public List<Booking> getAllBookings() {
        try {
            return bookingRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error getting bookings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<Booking> getBookingById(Long id) {
        try {
            return bookingRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Error getting booking by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Booking> getUserBookings(Long userId) {
        try {
            return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            System.err.println("Error getting user bookings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        try {
            return bookingRepository.findByStatus(status);
        } catch (Exception e) {
            System.err.println("Error getting bookings by status: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long getTotalBookingsCount() {
        try {
            return bookingRepository.count();
        } catch (Exception e) {
            System.err.println("Error getting total bookings count: " + e.getMessage());
            return 0;
        }
    }

    public long getConfirmedBookingsCount() {
        try {
            return bookingRepository.findAll().stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                    .count();
        } catch (Exception e) {
            System.err.println("Error getting confirmed bookings count: " + e.getMessage());
            return 0;
        }
    }

    public List<Booking> getRecentBookings(int limit) {
        try {
            return bookingRepository.findAllOrderByCreatedAtDesc().stream()
                    .limit(limit)
                    .toList();
        } catch (Exception e) {
            System.err.println("Error getting recent bookings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Booking> getTodaysBookings() {
        try {
            return bookingRepository.findAll().stream()
                    .filter(b -> b.getCreatedAt() != null &&
                            b.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                    .toList();
        } catch (Exception e) {
            System.err.println("Error getting today's bookings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public BigDecimal getTotalRevenue() {
        try {
            return bookingRepository.findAll().stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                    .map(Booking::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        System.out.println("🗑 === CANCELLING BOOKING DEBUG START ===");
        System.out.println("   Booking ID: " + bookingId);

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        System.err.println("❌ Booking not found for cancellation: " + bookingId);
                        return new RuntimeException("Booking not found");
                    });

            // Check if cancellation is allowed (at least 1 day before check-in)
            if (LocalDate.now().isAfter(booking.getCheckInDate().minusDays(1))) {
                System.err.println("❌ Cancellation not allowed - too close to check-in");
                throw new RuntimeException("Cancellation not allowed. Must cancel at least 1 day before check-in.");
            }

            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setCancellationDate(LocalDate.now());

            // Also update payment status if exists
            List<Payment> payments = paymentRepository.findByBookingId(bookingId);
            for (Payment payment : payments) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                payment.setRefundDate(LocalDateTime.now());
                paymentRepository.save(payment);
            }

            Booking cancelledBooking = bookingRepository.save(booking);
            System.out.println("✅ Booking cancelled successfully: " + bookingId);

            return cancelledBooking;

        } catch (Exception e) {
            System.err.println("❌ Booking cancellation failed: " + e.getMessage());
            throw new RuntimeException("Booking cancellation failed: " + e.getMessage());
        }
    }

    public Booking updateBooking(Booking booking) {
        try {
            return bookingRepository.save(booking);
        } catch (Exception e) {
            System.err.println("❌ Booking update failed: " + e.getMessage());
            throw new RuntimeException("Booking update failed: " + e.getMessage());
        }
    }

    public int getAvailableRoomsForDateRange(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        try {
            return getAvailableRoomCount(roomId, checkInDate, checkOutDate);
        } catch (Exception e) {
            System.err.println("Error getting available rooms: " + e.getMessage());
            return 0;
        }
    }

    // ===== PAYMENT-RELATED METHODS =====
    @Transactional
    public Booking processBookingPayment(Long bookingId, String paymentMethod, String transactionId) {
        System.out.println("💳 === PROCESSING BOOKING PAYMENT DEBUG START ===");
        System.out.println("   Booking ID: " + bookingId);

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Update payment
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId(transactionId);
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            // Update booking status
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            Booking confirmedBooking = bookingRepository.save(booking);

            // Send confirmation
            try {
                emailService.sendBookingConfirmation(booking.getUser(), confirmedBooking);
                System.out.println("✅ Booking confirmation email sent for booking: " + bookingId);
            } catch (Exception e) {
                System.err.println("⚠ Failed to send confirmation email: " + e.getMessage());
            }

            return confirmedBooking;

        } catch (Exception e) {
            System.err.println("❌ Payment processing failed: " + e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    @Transactional
    public Booking refundBooking(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Update booking status
            booking.setStatus(Booking.BookingStatus.REFUNDED);
            booking.setRefundDate(LocalDate.now());

            // Calculate refund amount (full refund if cancelled within policy)
            BigDecimal refundAmount = booking.getTotalAmount();
            booking.setRefundAmount(refundAmount);

            // Update payment status
            List<Payment> payments = paymentRepository.findByBookingId(bookingId);
            for (Payment payment : payments) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                payment.setRefundDate(LocalDateTime.now());
                paymentRepository.save(payment);
            }

            Booking refundedBooking = bookingRepository.save(booking);
            System.out.println("✅ Booking refund processed: " + bookingId);

            return refundedBooking;

        } catch (Exception e) {
            System.err.println("❌ Refund processing failed: " + e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }

    // ===== ANALYTICS METHODS =====
    public Map<String, Long> getBookingStatistics() {
        try {
            Map<String, Long> stats = new HashMap<>();
            List<Booking> allBookings = getAllBookings();

            stats.put("total", (long) allBookings.size());
            stats.put("confirmed", allBookings.stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                    .count());
            stats.put("pending", allBookings.stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING)
                    .count());
            stats.put("cancelled", allBookings.stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED)
                    .count());
            stats.put("completed", allBookings.stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                    .count());

            return stats;
        } catch (Exception e) {
            System.err.println("Error getting booking statistics: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public BigDecimal getMonthlyRevenue() {
        try {
            return bookingRepository.findAll().stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED &&
                            b.getCreatedAt() != null &&
                            b.getCreatedAt().getMonth() == LocalDateTime.now().getMonth())
                    .map(Booking::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            System.err.println("Error getting monthly revenue: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ===== PRIVATE HELPER METHODS =====
    private int getAvailableRoomCount(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        System.out.println("🔍 === AVAILABLE ROOM COUNT DEBUG ===");
        System.out.println("   Room ID: " + roomId);
        System.out.println("   Check-in: " + checkInDate);
        System.out.println("   Check-out: " + checkOutDate);

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> {
                        System.err.println("❌ Room not found for availability check: " + roomId);
                        return new RuntimeException("Room not found");
                    });

            System.out.println("   Room found: " + room.getRoomNumber() + " - " + room.getRoomType());
            System.out.println("   Room available flag: " + room.getAvailable());
            System.out.println("   Total rooms of this type: " + room.getTotalRooms());

            if (!room.getAvailable()) {
                System.out.println("   ❌ Room is marked as not available");
                return 0;
            }

            List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                    roomId, checkInDate, checkOutDate);

            System.out.println("   Found " + conflictingBookings.size() + " conflicting bookings for room " + roomId);

            int bookedRooms = conflictingBookings.stream()
                    .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED ||
                            b.getStatus() == Booking.BookingStatus.PENDING)
                    .mapToInt(b -> b.getRoomCount() != null ? b.getRoomCount() : 1)
                    .sum();

            int totalRooms = room.getTotalRooms() != null ? room.getTotalRooms() : 1;
            int available = Math.max(0, totalRooms - bookedRooms);

            System.out.println("   Room " + roomId + " - Total: " + totalRooms +
                    ", Booked: " + bookedRooms + ", Available: " + available);

            return available;
        } catch (Exception e) {
            System.err.println("❌ Error calculating available rooms: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // ===== VALIDATION METHODS =====
    private void validateBookingParameters(Long userId, Long roomId, LocalDate checkInDate,
                                           LocalDate checkOutDate, Integer numberOfGuests, Integer roomCount) {
        if (userId == null) throw new RuntimeException("User ID is required");
        if (roomId == null) throw new RuntimeException("Room ID is required");
        if (checkInDate == null) throw new RuntimeException("Check-in date is required");
        if (checkOutDate == null) throw new RuntimeException("Check-out date is required");
        if (numberOfGuests == null || numberOfGuests < 1) throw new RuntimeException("Number of guests is required");
        if (roomCount == null || roomCount < 1) throw new RuntimeException("Number of rooms is required");

        LocalDate today = LocalDate.now();
        if (checkInDate.isBefore(today)) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        if (checkInDate.isAfter(checkOutDate) || checkInDate.equals(checkOutDate)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
    }

    private void validateRoomCapacity(Room room, Integer numberOfGuests, Integer roomCount) {
        int maxCapacity = room.getCapacity() * roomCount;
        if (numberOfGuests > maxCapacity) {
            throw new RuntimeException("Number of guests (" + numberOfGuests +
                    ") exceeds room capacity. Maximum: " + maxCapacity);
        }
    }

    // ===== BULK OPERATIONS =====
    @Transactional
    public List<Booking> createMultipleBookings(List<BookingRequest> bookingRequests) {
        List<Booking> createdBookings = new ArrayList<>();

        for (BookingRequest request : bookingRequests) {
            try {
                Booking booking = createBooking(
                        request.getUserId(),
                        request.getRoomId(),
                        request.getCheckInDate(),
                        request.getCheckOutDate(),
                        request.getNumberOfGuests(),
                        request.getRoomCount(),
                        request.getSpecialRequests()
                );
                createdBookings.add(booking);
            } catch (Exception e) {
                System.err.println("Failed to create booking for request: " + request + " - " + e.getMessage());
                // Continue with other bookings
            }
        }

        return createdBookings;
    }

    @Transactional
    public void cancelExpiredPendingBookings() {
        try {
            List<Booking> pendingBookings = getBookingsByStatus(Booking.BookingStatus.PENDING);
            LocalDateTime expiryTime = LocalDateTime.now().minusHours(24); // 24-hour expiry

            int cancelledCount = 0;
            for (Booking booking : pendingBookings) {
                if (booking.getCreatedAt().isBefore(expiryTime)) {
                    booking.setStatus(Booking.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    cancelledCount++;
                }
            }

            if (cancelledCount > 0) {
                System.out.println("✅ Cancelled " + cancelledCount + " expired pending bookings");
            }
        } catch (Exception e) {
            System.err.println("Error cancelling expired bookings: " + e.getMessage());
        }
    }

    // ===== DTO FOR BULK OPERATIONS =====
    public static class BookingRequest {
        private Long userId;
        private Long roomId;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer numberOfGuests;
        private Integer roomCount;
        private String specialRequests;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

        public Integer getNumberOfGuests() { return numberOfGuests; }
        public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

        public Integer getRoomCount() { return roomCount; }
        public void setRoomCount(Integer roomCount) { this.roomCount = roomCount; }

        public String getSpecialRequests() { return specialRequests; }
        public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    }
}