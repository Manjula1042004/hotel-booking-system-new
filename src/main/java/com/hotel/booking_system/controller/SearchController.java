package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/search")
    public String searchRooms(@RequestParam String city,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
                              @RequestParam(required = false) String error,
                              @RequestParam(required = false) String message,
                              Model model) {

        try {
            System.out.println("🔍 === SEARCH REQUEST RECEIVED ===");
            System.out.println("📍 City: " + city);
            System.out.println("📅 Check-in: " + checkInDate);
            System.out.println("📅 Check-out: " + checkOutDate);

            // Add error message if present in URL parameters
            if (error != null) {
                String errorMessage = "Booking failed. Please try again.";
                if (message != null) {
                    // Decode URL-encoded message
                    errorMessage = java.net.URLDecoder.decode(message, "UTF-8");
                    // Shorten very long error messages
                    if (errorMessage.length() > 200) {
                        errorMessage = errorMessage.substring(0, 200) + "...";
                    }
                }
                model.addAttribute("error", errorMessage);
            }

            // Validate dates
            if (checkInDate == null || checkOutDate == null) {
                model.addAttribute("error", "Please select both check-in and check-out dates");
                model.addAttribute("rooms", Collections.emptyList());
                return "search/results";
            }

            // Validate date logic
            LocalDate today = LocalDate.now();
            if (checkInDate.isBefore(today)) {
                model.addAttribute("error", "Check-in date cannot be in the past");
                model.addAttribute("rooms", Collections.emptyList());
                return "search/results";
            }

            if (checkInDate.isAfter(checkOutDate)) {
                model.addAttribute("error", "Check-out date must be after check-in date");
                model.addAttribute("rooms", Collections.emptyList());
                return "search/results";
            }

            if (checkInDate.equals(checkOutDate)) {
                model.addAttribute("error", "Check-out date must be at least 1 day after check-in date");
                model.addAttribute("rooms", Collections.emptyList());
                return "search/results";
            }

            // Calculate number of nights
            long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            model.addAttribute("numberOfNights", numberOfNights);
            System.out.println("🌙 Number of nights: " + numberOfNights);

            // Validate city
            if (city == null || city.trim().isEmpty()) {
                model.addAttribute("error", "Please enter a city name");
                model.addAttribute("rooms", Collections.emptyList());
                return "search/results";
            }

            // Get available rooms
            List<Room> availableRooms = Collections.emptyList();
            try {
                System.out.println("🔄 Calling RoomService to find available rooms...");
                availableRooms = roomService.getAvailableRoomsByCityAndDates(city.trim(), checkInDate, checkOutDate);
                System.out.println("✅ RoomService returned " + availableRooms.size() + " available rooms");

                // Log room details for debugging
                availableRooms.forEach(room -> {
                    System.out.println("   🏨 " + room.getRoomType() + " at " + room.getHotel().getName() +
                            " - $" + room.getPrice() + "/night");
                });

            } catch (Exception e) {
                System.err.println("❌ RoomService error: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("error", "Search service temporarily unavailable. Please try again.");
            }

            // Add data to model
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("city", city);
            model.addAttribute("checkInDate", checkInDate);
            model.addAttribute("checkOutDate", checkOutDate);

            System.out.println("✅ === SEARCH COMPLETED ===");
            System.out.println("   📊 Results: " + availableRooms.size() + " rooms found");
            return "search/results";

        } catch (Exception e) {
            System.err.println("❌ === SEARCH CONTROLLER ERROR ===");
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Search failed due to system error. Please try again.");
            model.addAttribute("rooms", Collections.emptyList());
            model.addAttribute("city", city);
            model.addAttribute("checkInDate", checkInDate);
            model.addAttribute("checkOutDate", checkOutDate);

            return "search/results";
        }
    }
}