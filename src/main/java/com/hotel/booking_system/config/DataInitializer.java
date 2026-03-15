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
        System.out.println("👤 Checking admin user...");

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
            System.out.println("✅ ADMIN USER CREATED:");
            System.out.println("   📧 Email: admin@hotel.com");
            System.out.println("   🔑 Password: admin123");
            System.out.println("   👑 Role: ADMIN");
        } else {
            System.out.println("✅ Admin user already exists");
        }
    }

    private void createSampleHotels() {
        System.out.println("🏨 Creating sample hotels...");

        if (hotelRepository.count() == 0) {
            // Create Grand Plaza Hotel
            Hotel grandPlaza = new Hotel();
            grandPlaza.setName("Grand Plaza Hotel");
            grandPlaza.setCity("New York");
            grandPlaza.setAddress("123 Broadway, Manhattan, NY 10001");
            grandPlaza.setDescription("Experience luxury in the heart of Manhattan with stunning city views.");
            grandPlaza.setRating(4.8);
            grandPlaza.setImageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500");
            grandPlaza.setCreatedAt(LocalDateTime.now());
            grandPlaza.setUpdatedAt(LocalDateTime.now());
            hotelRepository.save(grandPlaza);

            // Create rooms for Grand Plaza
            createRoom(grandPlaza, "101", "Deluxe King", new BigDecimal("299.99"), 2, 5);
            createRoom(grandPlaza, "102", "Executive Suite", new BigDecimal("499.99"), 4, 3);
            createRoom(grandPlaza, "103", "Standard Double", new BigDecimal("199.99"), 2, 8);

            // Create Paris Luxury Suites
            Hotel parisLuxury = new Hotel();
            parisLuxury.setName("Paris Luxury Suites");
            parisLuxury.setCity("Paris");
            parisLuxury.setAddress("456 Champs-Élysées, 75008 Paris");
            parisLuxury.setDescription("Elegant suites with Eiffel Tower views in the most romantic city.");
            parisLuxury.setRating(4.9);
            parisLuxury.setImageUrl("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=500");
            parisLuxury.setCreatedAt(LocalDateTime.now());
            parisLuxury.setUpdatedAt(LocalDateTime.now());
            hotelRepository.save(parisLuxury);

            // Create rooms for Paris Luxury
            createRoom(parisLuxury, "201", "Eiffel Suite", new BigDecimal("599.99"), 2, 2);
            createRoom(parisLuxury, "202", "Deluxe King", new BigDecimal("399.99"), 2, 4);
            createRoom(parisLuxury, "203", "Family Suite", new BigDecimal("699.99"), 4, 2);

            System.out.println("✅ Sample hotels and rooms created successfully!");
        } else {
            System.out.println("✅ Sample hotels already exist");
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