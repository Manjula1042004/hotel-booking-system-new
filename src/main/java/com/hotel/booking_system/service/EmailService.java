package com.hotel.booking_system.service;

import com.hotel.booking_system.model.Booking;
import com.hotel.booking_system.model.Payment;
import com.hotel.booking_system.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendBookingConfirmation(User user, Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("userName", user.getName());
            context.setVariable("booking", booking);
            context.setVariable("hotel", booking.getRoom().getHotel());

            String htmlContent = templateEngine.process("email/booking-confirmation", context);

            helper.setTo(user.getEmail());
            helper.setSubject("Booking Confirmation - " + booking.getRoom().getHotel().getName());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Booking confirmation email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send booking confirmation email: " + e.getMessage());
        }
    }

    public void sendPaymentConfirmation(User user, Payment payment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("userName", user.getName());
            context.setVariable("payment", payment);
            context.setVariable("booking", payment.getBooking());

            String htmlContent = templateEngine.process("email/payment-confirmation", context);

            helper.setTo(user.getEmail());
            helper.setSubject("Payment Confirmation - Transaction #" + payment.getTransactionId());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Payment confirmation email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
        }
    }
}