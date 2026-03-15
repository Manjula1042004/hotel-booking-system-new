package com.hotel.booking_system.config;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.model.User;
import com.hotel.booking_system.repository.HotelRepository;
import com.hotel.booking_system.repository.RoomRepository;
import com.hotel.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🎯 ===== DATA INITIALIZER STARTED =====");

        // Create Admin User with ADMIN role
        createAdminUser();

        // Create Sample Hotels and Rooms
        createSampleHotels();

        System.out.println("🎯 ===== DATA INITIALIZER COMPLETED =====");
        System.out.println("✅ Application is ready! Access URLs:");
        System.out.println("📍 Homepage: http://localhost:8080");
        System.out.println("📍 Admin Login: http://localhost:8080/login (admin@hotel.com / admin123) - Redirects to Admin Dashboard");
        System.out.println("📍 User Registration: http://localhost:8080/register - Redirects to User Dashboard");
    }

    private void createAdminUser() {
        System.out.println("👤 Checking admin user...");

        if (userRepository.findByEmail("admin@hotel.com").isEmpty()) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin@hotel.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN); // Explicitly set ADMIN role
            userRepository.save(admin);
            System.out.println("✅ ADMIN USER CREATED:");
            System.out.println("   📧 Email: admin@hotel.com");
            System.out.println("   🔑 Password: admin123");
            System.out.println("   👑 Role: ADMIN");
        } else {
            System.out.println("✅ Admin user already exists");
            User admin = userRepository.findByEmail("admin@hotel.com").get();
            admin.setRole(User.Role.ADMIN); // Ensure role is ADMIN
            userRepository.save(admin);
        }
    }

    private void createSampleHotels() {
        // ... your existing hotel creation code ...
    }
}