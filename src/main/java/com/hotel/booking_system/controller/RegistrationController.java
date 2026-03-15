package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.User;
import com.hotel.booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam String role,
                               Model model) {
        try {
            System.out.println("🔹 Registration request received");
            System.out.println("   - Name: " + user.getName());
            System.out.println("   - Email: " + user.getEmail());
            System.out.println("   - Selected Role: " + role);

            // Set the selected role
            if ("ADMIN".equalsIgnoreCase(role)) {
                user.setRole(User.Role.ADMIN);
                System.out.println("   → Setting role to ADMIN");
            } else {
                user.setRole(User.Role.USER);
                System.out.println("   → Setting role to USER");
            }

            // Register the user
            User registeredUser = userService.registerUser(user);

            System.out.println("✅ Registration successful!");
            System.out.println("   - User ID: " + registeredUser.getId());
            System.out.println("   - Final Role: " + registeredUser.getRole());

            // Different success message based on role
            if (registeredUser.getRole() == User.Role.ADMIN) {
                return "redirect:/login?admin_registered";
            } else {
                return "redirect:/login?user_registered";
            }

        } catch (RuntimeException e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }
}