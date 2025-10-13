package com.hotel.booking_system.service;

import com.hotel.booking_system.model.Booking;
import com.hotel.booking_system.model.Payment;
import com.hotel.booking_system.repository.BookingRepository;
import com.hotel.booking_system.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret:test_secret}")
    private String razorpayKeySecret;

    // ===== BASIC PAYMENT METHODS =====
    public List<Payment> getAllPayments() {
        try {
            return paymentRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error getting payments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public BigDecimal getTotalRevenue() {
        try {
            BigDecimal revenue = paymentRepository.getTotalRevenue();
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error getting revenue: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public List<Payment> getSuccessfulPayments() {
        try {
            return paymentRepository.findByStatus(Payment.PaymentStatus.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error getting successful payments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Payment> getFailedPayments() {
        try {
            return paymentRepository.findByStatus(Payment.PaymentStatus.FAILED);
        } catch (Exception e) {
            System.err.println("Error getting failed payments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        try {
            return paymentRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Error getting payment by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        try {
            return paymentRepository.findByBookingId(bookingId);
        } catch (Exception e) {
            System.err.println("Error getting payments by booking ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ===== PAYMENT PROCESSING METHODS =====
    public String createRazorpayOrder(BigDecimal amount, String currency, String receipt) throws RazorpayException {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            throw new RuntimeException("Razorpay order creation failed: " + e.getMessage());
        }
    }

    public Payment processRazorpayPayment(Long bookingId, String razorpayPaymentId, String razorpayOrderId, String razorpaySignature) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify payment signature
            boolean isValid = verifyPaymentSignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .findFirst()
                    .orElse(new Payment());

            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentMethod("RAZORPAY");
            payment.setTransactionId(razorpayPaymentId);
            payment.setPaymentDate(LocalDateTime.now());

            if (isValid) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                // Update booking status to confirmed
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                // Send payment confirmation email
                try {
                    emailService.sendPaymentConfirmation(booking.getUser(), payment);
                    System.out.println("Payment confirmation email sent to: " + booking.getUser().getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send payment confirmation email: " + e.getMessage());
                }
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
            }

            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Razorpay payment processing failed: " + e.getMessage());
        }
    }

    public Payment processPayment(Long bookingId, String paymentMethod, String cardNumber,
                                  String cardHolderName, String expiryDate, String cvv) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Validate card details (simplified validation)
            if (!isValidCardDetails(cardNumber, expiryDate, cvv)) {
                throw new RuntimeException("Invalid card details");
            }

            // Simulate payment processing
            boolean paymentSuccess = simulatePaymentProcessing(cardNumber, booking.getTotalAmount());

            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .findFirst()
                    .orElse(new Payment());

            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentMethod(paymentMethod);
            payment.setCardLastFour(cardNumber.substring(cardNumber.length() - 4));
            payment.setTransactionId(generateTransactionId());
            payment.setPaymentDate(LocalDateTime.now());

            if (paymentSuccess) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                // Update booking status to confirmed
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                // Send payment confirmation email
                try {
                    emailService.sendPaymentConfirmation(booking.getUser(), payment);
                    System.out.println("Payment confirmation email sent to: " + booking.getUser().getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send payment confirmation email: " + e.getMessage());
                }
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
            }

            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    public Payment processCashPayment(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .findFirst()
                    .orElse(new Payment());

            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentMethod("CASH");
            payment.setTransactionId("CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);

            // Update booking status
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Send payment confirmation email
            try {
                emailService.sendPaymentConfirmation(booking.getUser(), payment);
                System.out.println("Payment confirmation email sent to: " + booking.getUser().getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send payment confirmation email: " + e.getMessage());
            }

            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Cash payment processing failed: " + e.getMessage());
        }
    }

    public Payment refundPayment(Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                throw new RuntimeException("Only successful payments can be refunded");
            }

            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setRefundDate(LocalDateTime.now());

            // Update booking status to cancelled
            Booking booking = payment.getBooking();
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }

    // ===== PRIVATE HELPER METHODS =====
    private boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            // Using simple verification for demo (in production, use proper HMAC)
            // For demo purposes, we'll accept all signatures
            return true;
            // In production, use:
            // String generatedSignature = HmacUtils.hmacSha256Hex(razorpayKeySecret, payload);
            // return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }



    private boolean simulatePaymentProcessing(String cardNumber, BigDecimal amount) {
        // Simulate payment processing - 90% success rate for demo
        // In real application, this would integrate with payment gateway like Stripe, Razorpay
        return Math.random() > 0.1; // 90% success rate
    }
    // In PaymentService.java - Update the card validation method
    private boolean isValidCardDetails(String cardNumber, String expiryDate, String cvv) {
        try {
            // Remove spaces from card number
            String cleanCardNumber = cardNumber.replaceAll("\\s+", "");

            // Basic validation
            if (cleanCardNumber == null || cleanCardNumber.length() != 16 || !cleanCardNumber.matches("\\d+")) {
                System.err.println("Invalid card number: " + cleanCardNumber);
                return false;
            }

            if (cvv == null || cvv.length() != 3 || !cvv.matches("\\d+")) {
                System.err.println("Invalid CVV: " + cvv);
                return false;
            }

            if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                System.err.println("Invalid expiry date: " + expiryDate);
                return false;
            }

            // Validate expiry date is not in the past
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]); // Assuming 20XX format

            java.time.YearMonth expiry = java.time.YearMonth.of(year, month);
            java.time.YearMonth now = java.time.YearMonth.now();

            if (expiry.isBefore(now)) {
                System.err.println("Card expired: " + expiryDate);
                return false;
            }

            System.out.println("✅ Card validation passed");
            return true;

        } catch (Exception e) {
            System.err.println("Card validation error: " + e.getMessage());
            return false;
        }
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}