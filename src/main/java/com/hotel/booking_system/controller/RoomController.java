package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.service.HotelService;
import com.hotel.booking_system.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotel/{hotelId}")
    public String getRoomsByHotel(@PathVariable Long hotelId, Model model) {
        try {
            Hotel hotel = hotelService.getHotelById(hotelId)
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));
            List<Room> rooms = roomService.getRoomsByHotelId(hotelId);

            model.addAttribute("hotel", hotel);
            model.addAttribute("rooms", rooms);
            return "admin/rooms";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading rooms: " + e.getMessage());
            return "redirect:/admin/hotels";
        }
    }

    @GetMapping("/hotel/{hotelId}/new")
    public String createRoomForm(@PathVariable Long hotelId, Model model) {
        try {
            Hotel hotel = hotelService.getHotelById(hotelId)
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));

            Room room = new Room();
            room.setHotel(hotel);

            model.addAttribute("room", room);
            model.addAttribute("hotel", hotel);
            return "admin/room-form";
        } catch (Exception e) {
            return "redirect:/admin/hotels?error=" + e.getMessage();
        }
    }

    @PostMapping("/hotel/{hotelId}")
    public String createRoom(@PathVariable Long hotelId, @ModelAttribute Room room, Model model) {
        try {
            Hotel hotel = hotelService.getHotelById(hotelId)
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));

            room.setHotel(hotel);
            roomService.createRoom(room);

            return "redirect:/admin/rooms/hotel/" + hotelId + "?created=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating room: " + e.getMessage());
            model.addAttribute("room", room);
            model.addAttribute("hotel", hotelService.getHotelById(hotelId).orElse(null));
            return "admin/room-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        try {
            Room room = roomService.getRoomById(id);
            if (room == null) {
                throw new RuntimeException("Room not found");
            }

            model.addAttribute("room", room);
            model.addAttribute("hotel", room.getHotel());
            return "admin/room-form";
        } catch (Exception e) {
            return "redirect:/admin/hotels?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}")
    public String updateRoom(@PathVariable Long id, @ModelAttribute Room roomDetails, Model model) {
        try {
            Room existingRoom = roomService.getRoomById(id);
            if (existingRoom == null) {
                throw new RuntimeException("Room not found");
            }

            roomService.updateRoom(id, roomDetails);
            return "redirect:/admin/rooms/hotel/" + existingRoom.getHotel().getId() + "?updated=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating room: " + e.getMessage());
            model.addAttribute("room", roomDetails);
            return "admin/room-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteRoom(@PathVariable Long id) {
        try {
            Room room = roomService.getRoomById(id);
            if (room == null) {
                throw new RuntimeException("Room not found");
            }

            Long hotelId = room.getHotel().getId();
            roomService.deleteRoom(id);
            return "redirect:/admin/rooms/hotel/" + hotelId + "?deleted=true";
        } catch (Exception e) {
            return "redirect:/admin/hotels?error=" + e.getMessage();
        }
    }
}