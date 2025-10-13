package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Booking;
import com.hotel.booking_system.model.User;
import com.hotel.booking_system.service.BookingService;
import com.hotel.booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/my-bookings")
    public String getUserBookings(Model model, Principal principal) {
        try {
            System.out.println("Loading bookings for user: " + principal.getName());

            String email = principal.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Booking> bookings = bookingService.getUserBookings(user.getId());

            System.out.println("Found " + bookings.size() + " bookings");
            model.addAttribute("bookings", bookings);
            return "bookings/my-bookings";

        } catch (Exception e) {
            System.out.println("ERROR in getUserBookings: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Unable to load bookings: " + e.getMessage());
            return "bookings/my-bookings";
        }
    }

    @PostMapping("/book")
    public String createBooking(@RequestParam Long roomId,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
                                @RequestParam Integer numberOfGuests,
                                @RequestParam(defaultValue = "1") Integer roomCount,
                                @RequestParam(required = false) String specialRequests,
                                Principal principal,
                                Model model) {

        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("Creating booking for room: " + roomId +
                    ", guests: " + numberOfGuests +
                    ", rooms: " + roomCount);

            // Validate guest count
            if (numberOfGuests < 1 || numberOfGuests > 20) {
                model.addAttribute("error", "Number of guests must be between 1 and 20");
                return "redirect:/search?error=invalid_guests";
            }

            // Validate room count
            if (roomCount < 1 || roomCount > 10) {
                model.addAttribute("error", "Number of rooms must be between 1 and 10");
                return "redirect:/search?error=invalid_rooms";
            }

            Booking booking = bookingService.createBooking(
                    user.getId(), roomId, checkInDate, checkOutDate,
                    numberOfGuests, roomCount, specialRequests);

            return "redirect:/bookings/confirmation?bookingId=" + booking.getId();
        } catch (Exception e) {
            System.out.println("ERROR creating booking: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/search?error=booking_failed&message=" + e.getMessage();
        }
    }

    @GetMapping("/confirmation")
    public String bookingConfirmation(@RequestParam Long bookingId, Model model) {
        try {
            Booking booking = bookingService.getBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            model.addAttribute("booking", booking);
            return "bookings/confirmation";
        } catch (Exception e) {
            model.addAttribute("error", "Booking not found: " + e.getMessage());
            return "bookings/confirmation";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Model model, Principal principal) {
        try {
            // Verify the booking belongs to the logged-in user
            String userEmail = principal.getName();
            Booking booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!booking.getUser().getEmail().equals(userEmail)) {
                return "redirect:/access-denied";
            }

            Booking cancelledBooking = bookingService.cancelBooking(id);
            return "redirect:/bookings/my-bookings?cancelled=true";
        } catch (Exception e) {
            System.out.println("ERROR cancelling booking: " + e.getMessage());
            return "redirect:/bookings/my-bookings?error=" + e.getMessage().replace(" ", "+");
        }
    }

    @GetMapping("/availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@RequestParam Long roomId,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            int availableRooms = bookingService.getAvailableRoomsForDateRange(roomId, checkInDate, checkOutDate);

            response.put("success", true);
            response.put("availableRooms", availableRooms);
            response.put("roomId", roomId);
            response.put("checkInDate", checkInDate);
            response.put("checkOutDate", checkOutDate);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}