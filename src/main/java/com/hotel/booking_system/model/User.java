package com.hotel.booking_system.config;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        createAdminUser();
        createSampleHotels();
        System.out.println("🎯 ===== DATA INITIALIZER COMPLETED =====");
    }

    private void createAdminUser() {
        if (userRepository.findByEmail("admin@hotel.com").isEmpty()) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin@hotel.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);

            // Set all required fields with default values
            admin.setAccountNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setCredentialsNonExpired(true);
            admin.setEnabled(true);
            admin.setEmailVerified(false);
            admin.setFailedLoginAttempts(0);
            admin.setProvider("LOCAL");
            admin.setTwoFactorEnabled(false);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("✅ ADMIN USER CREATED");
        }
    }

    private void createSampleHotels() {
        if (hotelRepository.count() == 0) {
            // Create Grand Plaza Hotel
            Hotel grandPlaza = new Hotel();
            grandPlaza.setName("Grand Plaza Hotel");
            grandPlaza.setCity("New York");
            grandPlaza.setAddress("123 Broadway, Manhattan, NY 10001");
            grandPlaza.setDescription("Experience luxury in the heart of Manhattan.");
            grandPlaza.setRating(4.8);
            grandPlaza.setImageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500");
            grandPlaza.setCreatedAt(LocalDateTime.now());
            grandPlaza.setUpdatedAt(LocalDateTime.now());
            hotelRepository.save(grandPlaza);

            // Create rooms
            createRoom(grandPlaza, "101", "Deluxe King", new BigDecimal("299.99"), 2, 5);
            createRoom(grandPlaza, "102", "Executive Suite", new BigDecimal("499.99"), 4, 3);

            System.out.println("✅ Sample hotels created");
        }
    }

    private void createRoom(Hotel hotel, String roomNumber, String roomType,
                            BigDecimal price, int capacity, int totalRooms) {
        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setPrice(price);
        room.setCapacity(capacity);
        room.setTotalRooms(totalRooms);
        room.setAvailable(true);
        room.setDescription("Beautiful " + roomType + " with modern amenities.");
        room.setAmenities("Free WiFi, TV, Air Conditioning, Mini Bar");
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
    }
}