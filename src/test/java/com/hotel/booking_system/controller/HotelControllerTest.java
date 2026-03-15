package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(HotelController.class)
public class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    private Hotel testHotel;
    private List<Hotel> hotelList;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");
        testHotel.setDescription("A test hotel");
        testHotel.setRating(4.5);
        testHotel.setImageUrl("http://test.com/image.jpg");
        testHotel.setCreatedAt(LocalDateTime.now());

        hotelList = Arrays.asList(testHotel);
    }

    @Test
    void getAllHotels_ShouldReturnHotelsView() throws Exception {
        // Given
        when(hotelService.getAllHotels()).thenReturn(hotelList);

        // When & Then
        mockMvc.perform(get("/hotels"))
                .andExpect(status().isOk())
                .andExpect(view().name("hotels/list"))
                .andExpect(model().attributeExists("hotels"))
                .andExpect(model().attribute("hotels", hotelList));
    }

    @Test
    void searchHotels_ShouldReturnFilteredHotels() throws Exception {
        // Given
        String city = "Test City";
        when(hotelService.searchHotelsByCity(city)).thenReturn(hotelList);

        // When & Then
        mockMvc.perform(get("/hotels/search")
                        .param("city", city))
                .andExpect(status().isOk())
                .andExpect(view().name("hotels/list"))
                .andExpect(model().attributeExists("hotels"))
                .andExpect(model().attributeExists("searchCity"))
                .andExpect(model().attribute("searchCity", city));
    }

    @Test
    void getHotelDetails_ShouldReturnHotelDetailsView() throws Exception {
        // Given
        when(hotelService.getHotelById(1L)).thenReturn(Optional.of(testHotel));

        // When & Then
        mockMvc.perform(get("/hotels/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("hotels/details"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(model().attribute("hotel", testHotel));
    }

    @Test
    void getHotelDetails_ShouldThrowException_WhenHotelNotFound() throws Exception {
        // Given
        when(hotelService.getHotelById(anyLong())).thenReturn(Optional.empty());

        // When & Then - Fixed the exception assertion for MockMvc
        mockMvc.perform(get("/hotels/{id}", 999L))
                .andExpect(status().isOk())
                .andExpect(view().name("hotels/details"))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertTrue(exception instanceof RuntimeException);
                    assertEquals("Hotel not found", exception.getMessage());
                });
    }
}