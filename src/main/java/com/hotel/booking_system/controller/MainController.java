package com.hotel.booking_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "✅ Application is working! Time: " + LocalDateTime.now();
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }

    @GetMapping("/debug")
    @ResponseBody
    public Map<String, Object> debug() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "running");
        info.put("time", LocalDateTime.now().toString());
        info.put("port", System.getProperty("server.port"));
        info.put("profile", System.getProperty("spring.profiles.active"));
        info.put("db_url", System.getenv("DATABASE_URL") != null ? "set" : "not set");
        return info;
    }
}