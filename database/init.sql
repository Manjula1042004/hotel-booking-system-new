-- =============================================
-- Hotel Booking System Database Initialization
-- This script runs when MySQL container starts
-- =============================================

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS hotel_booking_db;
USE hotel_booking_db;

-- Create application user with limited privileges (for production safety)
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER ON hotel_booking_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;

-- Optional: Create some initial data for testing
-- This will be executed only if tables don't exist yet

-- Sample admin user (password: admin123 - already hashed in DataInitializer)
INSERT IGNORE INTO users (id, name, email, password, role, created_at, updated_at)
VALUES (1, 'Administrator', 'admin@hotel.com', '$2a$10$xyz123hashedpassword', 'ADMIN', NOW(), NOW());

-- Sample hotels data
INSERT IGNORE INTO hotels (id, name, city, address, description, rating, image_url, created_at, updated_at) VALUES
(1, 'Grand Plaza Hotel', 'New York', '123 Broadway, Manhattan', 'Luxury hotel in the heart of Manhattan with stunning city views.', 4.5, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=500', NOW(), NOW()),
(2, 'Paris Luxury Suites', 'Paris', '456 Champs-Élysées', 'Elegant suites with Eiffel Tower views in the heart of Paris.', 4.8, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=500', NOW(), NOW()),
(3, 'London Royal Hotel', 'London', '789 Oxford Street', 'Historic hotel with modern amenities in central London.', 4.3, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500', NOW(), NOW());

-- Sample rooms data
INSERT IGNORE INTO rooms (id, room_number, room_type, price, capacity, total_rooms, description, amenities, available, hotel_id, created_at, updated_at) VALUES
(1, '101', 'Deluxe King', 199.99, 2, 5, 'Spacious room with king bed and city view', 'Free WiFi, TV, Mini Bar, Air Conditioning', true, 1, NOW(), NOW()),
(2, '102', 'Standard Queen', 149.99, 2, 3, 'Comfortable room with queen bed', 'Free WiFi, TV, Air Conditioning', true, 1, NOW(), NOW()),
(3, '201', 'Executive Suite', 299.99, 3, 4, 'Luxurious suite with Eiffel Tower view', 'Free WiFi, TV, Mini Bar, Air Conditioning, Balcony', true, 2, NOW(), NOW()),
(4, '202', 'Deluxe Room', 229.99, 2, 6, 'Beautiful room with city views', 'Free WiFi, TV, Air Conditioning, Coffee Maker', true, 2, NOW(), NOW()),
(5, '301', 'Business Room', 179.99, 2, 4, 'Modern room for business travelers', 'Free WiFi, TV, Work Desk, Air Conditioning', true, 3, NOW(), NOW()),
(6, '302', 'Family Suite', 259.99, 4, 2, 'Spacious suite for families', 'Free WiFi, TV, Mini Bar, Air Conditioning, Sofa Bed', true, 3, NOW(), NOW());

-- Display success message (visible in docker logs)
SELECT 'Hotel Booking System database initialized successfully!' as status;