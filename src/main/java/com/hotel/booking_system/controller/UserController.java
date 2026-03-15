package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.User;
import com.hotel.booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            model.addAttribute("user", user);
            System.out.println("User dashboard loaded for: " + email);
            return "user/dashboard";
        } catch (Exception e) {
            System.err.println("Error loading user dashboard: " + e.getMessage());
            return "redirect:/?error";
        }
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User userDetails, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.updateUser(user.getId(), userDetails);
        return "redirect:/user/profile?success";
    }
}