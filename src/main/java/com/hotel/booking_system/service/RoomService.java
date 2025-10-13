package com.hotel.booking_system.service;

import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAvailableRoomsByCityAndDates(String city, LocalDate checkInDate, LocalDate checkOutDate) {
        try {
            System.out.println("RoomService: Searching for rooms in " + city + " from " + checkInDate + " to " + checkOutDate);

            if (city == null || city.trim().isEmpty()) {
                System.out.println("RoomService: City parameter is empty");
                return Collections.emptyList();
            }

            List<Room> rooms = roomRepository.findAvailableRoomsByCityAndDates(city.trim(), checkInDate, checkOutDate);
            System.out.println("RoomService: Found " + rooms.size() + " rooms");
            return rooms;

        } catch (Exception e) {
            System.out.println("RoomService ERROR: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of throwing exception
            return Collections.emptyList();
        }
    }

    // ... rest of your existing methods
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setRoomType(roomDetails.getRoomType());
        room.setPrice(roomDetails.getPrice());
        room.setCapacity(roomDetails.getCapacity());
        room.setDescription(roomDetails.getDescription());
        room.setAmenities(roomDetails.getAmenities());
        room.setAvailable(roomDetails.getAvailable());
        room.setTotalRooms(roomDetails.getTotalRooms());

        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> getAvailableRoomsByHotelAndDates(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        try {
            return roomRepository.findAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);
        } catch (Exception e) {
            System.out.println("Error getting available rooms: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}