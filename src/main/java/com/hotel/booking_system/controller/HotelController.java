package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping
    public String getAllHotels(Model model) {
        List<Hotel> hotels = hotelService.getAllHotels();
        model.addAttribute("hotels", hotels);
        return "hotels/list";
    }

    @GetMapping("/search")
    public String searchHotels(@RequestParam String city, Model model) {
        List<Hotel> hotels = hotelService.searchHotelsByCity(city);
        model.addAttribute("hotels", hotels);
        model.addAttribute("searchCity", city);
        return "hotels/list";
    }

    @GetMapping("/{id}")
    public String getHotelDetails(@PathVariable Long id, Model model) {
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        model.addAttribute("hotel", hotel);
        return "hotels/details";
    }
}