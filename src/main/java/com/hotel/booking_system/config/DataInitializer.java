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
import java.util.List;

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

        // Step 1: Create Admin User
        createAdminUser();

        // Step 2: Create Sample Hotels and Rooms
        createSampleHotels();

        System.out.println("🎯 ===== DATA INITIALIZER COMPLETED =====");
        System.out.println("✅ Application is ready! Access URLs:");
        System.out.println("📍 Homepage: http://localhost:8080");
        System.out.println("📍 Admin Login: http://localhost:8080/login (admin@hotel.com / admin123)");
        System.out.println("📍 User Registration: http://localhost:8080/register");
    }

    private void createAdminUser() {
        System.out.println("👤 Checking admin user...");

        if (userRepository.findByEmail("admin@hotel.com").isEmpty()) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin@hotel.com");
            // Use proper BCrypt encoding
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ ADMIN USER CREATED:");
            System.out.println("   📧 Email: admin@hotel.com");
            System.out.println("   🔑 Password: admin123");
            System.out.println("   👑 Role: ADMIN");
        } else {
            System.out.println("✅ Admin user already exists - resetting password...");
            // Update existing admin password
            User admin = userRepository.findByEmail("admin@hotel.com").get();
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("✅ Admin password reset to: admin123");
        }
    }

    private void createSampleHotels() {
        System.out.println("🏨 Checking hotels in database...");

        long hotelCount = hotelRepository.count();
        System.out.println("   📊 Current hotel count: " + hotelCount);

        if (hotelCount == 0) {
            System.out.println("📝 Creating sample hotels and rooms...");

            // Hotel 1 - New York
            System.out.println("   🏙 Creating Grand Plaza Hotel (New York)...");
            Hotel hotel1 = createHotel(
                    "Grand Plaza Hotel",
                    "New York",
                    "123 Broadway, Manhattan",
                    "Luxury hotel in the heart of Manhattan with stunning city views.",
                    4.5,
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=500"
            );

            createRoom(hotel1, "101", "Deluxe King", new BigDecimal("199.99"), 2, 5,
                    "Spacious room with king bed and city view",
                    "Free WiFi, TV, Mini Bar, Air Conditioning");
            createRoom(hotel1, "102", "Standard Queen", new BigDecimal("149.99"), 2, 3,
                    "Comfortable room with queen bed",
                    "Free WiFi, TV, Air Conditioning");

            // Hotel 2 - Paris
            System.out.println("   🗼 Creating Paris Luxury Suites (Paris)...");
            Hotel hotel2 = createHotel(
                    "Paris Luxury Suites",
                    "Paris",
                    "456 Champs-Élysées",
                    "Elegant suites with Eiffel Tower views in the heart of Paris.",
                    4.8,
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=500"
            );

            createRoom(hotel2, "201", "Executive Suite", new BigDecimal("299.99"), 3, 4,
                    "Luxurious suite with Eiffel Tower view",
                    "Free WiFi, TV, Mini Bar, Air Conditioning, Balcony");
            createRoom(hotel2, "202", "Deluxe Room", new BigDecimal("229.99"), 2, 6,
                    "Beautiful room with city views",
                    "Free WiFi, TV, Air Conditioning, Coffee Maker");

            // Hotel 3 - London
            System.out.println("   🇬🇧 Creating London Royal Hotel (London)...");
            Hotel hotel3 = createHotel(
                    "London Royal Hotel",
                    "London",
                    "789 Oxford Street",
                    "Historic hotel with modern amenities in central London.",
                    4.3,
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500"
            );

            createRoom(hotel3, "301", "Business Room", new BigDecimal("179.99"), 2, 4,
                    "Modern room for business travelers",
                    "Free WiFi, TV, Work Desk, Air Conditioning");
            createRoom(hotel3, "302", "Family Suite", new BigDecimal("259.99"), 4, 2,
                    "Spacious suite for families",
                    "Free WiFi, TV, Mini Bar, Air Conditioning, Sofa Bed");

            // Verify creation
            long finalHotelCount = hotelRepository.count();
            long finalRoomCount = roomRepository.count();

            System.out.println("✅ SAMPLE DATA CREATION COMPLETE:");
            System.out.println("   🏨 Total Hotels: " + finalHotelCount);
            System.out.println("   🚪 Total Rooms: " + finalRoomCount);
            System.out.println("   ✅ Application is ready with sample data!");

        } else {
            System.out.println("✅ Hotels already exist in database");
            System.out.println("   🏨 Total Hotels: " + hotelCount);
            System.out.println("   🚪 Total Rooms: " + roomRepository.count());
        }
    }

    private Hotel createHotel(String name, String city, String address, String description,
                              Double rating, String imageUrl) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setCity(city);
        hotel.setAddress(address);
        hotel.setDescription(description);
        hotel.setRating(rating);
        hotel.setImageUrl(imageUrl);
        return hotelRepository.save(hotel);
    }

    private void createRoom(Hotel hotel, String roomNumber, String roomType, BigDecimal price,
                            Integer capacity, Integer totalRooms, String description, String amenities) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setPrice(price);
        room.setCapacity(capacity);
        room.setTotalRooms(totalRooms);
        room.setDescription(description);
        room.setAmenities(amenities);
        room.setAvailable(true);
        room.setHotel(hotel);
        roomRepository.save(room);

        System.out.println("      ✅ Room " + roomNumber + " - " + roomType + " ($" + price + ")");
    }
}