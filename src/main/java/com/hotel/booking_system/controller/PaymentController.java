package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Booking;
import com.hotel.booking_system.model.Payment;
import com.hotel.booking_system.service.BookingService;
import com.hotel.booking_system.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/process/{bookingId}")
    public String processPayment(@PathVariable Long bookingId, Model model, Principal principal) {
        try {
            System.out.println("🔄 Processing payment for booking: " + bookingId);

            Booking booking = bookingService.getBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify the booking belongs to the logged-in user
            String userEmail = principal.getName();
            if (!booking.getUser().getEmail().equals(userEmail)) {
                return "redirect:/access-denied";
            }

            model.addAttribute("booking", booking);
            System.out.println("✅ Payment page loaded for booking: " + bookingId);
            return "payments/process";

        } catch (Exception e) {
            System.err.println("❌ Error loading payment page: " + e.getMessage());
            model.addAttribute("error", "Unable to process payment: " + e.getMessage());
            return "redirect:/bookings/my-bookings";
        }
    }

    // FIXED: Process online payment with better error handling
    @PostMapping("/process/online")
    public String processOnlinePayment(@RequestParam Long bookingId,
                                       @RequestParam String paymentMethod,
                                       @RequestParam String cardNumber,
                                       @RequestParam String cardHolderName,
                                       @RequestParam String expiryDate,
                                       @RequestParam String cvv,
                                       Principal principal,
                                       Model model) {

        try {
            System.out.println("💳 Processing online payment for booking: " + bookingId);
            System.out.println("Payment Method: " + paymentMethod);
            System.out.println("Card: " + cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4));

            // Verify the booking belongs to the logged-in user
            String userEmail = principal.getName();
            Booking booking = bookingService.getBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!booking.getUser().getEmail().equals(userEmail)) {
                return "redirect:/access-denied";
            }

            // Process payment
            Payment payment = paymentService.processPayment(bookingId, paymentMethod,
                    cardNumber, cardHolderName, expiryDate, cvv);

            if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                System.out.println("✅ Payment successful for booking: " + bookingId);
                return "redirect:/payments/success?paymentId=" + payment.getId();
            } else {
                System.err.println("❌ Payment failed for booking: " + bookingId);
                return "redirect:/payments/failed?bookingId=" + bookingId + "&error=payment_failed";
            }

        } catch (Exception e) {
            System.err.println("❌ Payment processing error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/payments/process/" + bookingId + "?error=" +
                    e.getMessage().replace(" ", "%20").substring(0, Math.min(e.getMessage().length(), 50));
        }
    }

    // FIXED: Process cash payment
    @PostMapping("/process/cash")
    public String processCashPayment(@RequestParam Long bookingId, Principal principal) {
        try {
            System.out.println("💵 Processing cash payment for booking: " + bookingId);

            // Verify the booking belongs to the logged-in user
            String userEmail = principal.getName();
            Booking booking = bookingService.getBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!booking.getUser().getEmail().equals(userEmail)) {
                return "redirect:/access-denied";
            }

            Payment payment = paymentService.processCashPayment(bookingId);
            System.out.println("✅ Cash payment processed for booking: " + bookingId);
            return "redirect:/payments/success?paymentId=" + payment.getId();

        } catch (Exception e) {
            System.err.println("❌ Cash payment error: " + e.getMessage());
            return "redirect:/payments/process/" + bookingId + "?error=cash_payment_failed";
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Long paymentId, Model model) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            model.addAttribute("payment", payment);
            model.addAttribute("booking", payment.getBooking());
            System.out.println("✅ Payment success page loaded for payment: " + paymentId);
            return "payments/success";
        } catch (Exception e) {
            System.err.println("❌ Error loading success page: " + e.getMessage());
            model.addAttribute("error", "Payment not found");
            return "payments/success";
        }
    }

    @GetMapping("/failed")
    public String paymentFailed(@RequestParam Long bookingId,
                                @RequestParam(required = false) String error,
                                Model model) {
        try {
            Booking booking = bookingService.getBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            model.addAttribute("booking", booking);
            model.addAttribute("error", error != null ? error : "Payment processing failed. Please try again.");
            System.out.println("❌ Payment failed for booking: " + bookingId);
            return "payments/failed";
        } catch (Exception e) {
            System.err.println("❌ Error loading failed page: " + e.getMessage());
            model.addAttribute("error", "Booking not found");
            return "payments/failed";
        }
    }

    // ... rest of your existing methods (RazorPay, refund, etc.)
}