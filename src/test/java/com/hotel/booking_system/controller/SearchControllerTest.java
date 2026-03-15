package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    private Hotel testHotel;
    private Room testRoom;
    private List<Room> roomList;
    private final String city = "Test City";
    private final LocalDate checkInDate = LocalDate.now().plusDays(1);
    private final LocalDate checkOutDate = LocalDate.now().plusDays(3);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity(city);
        testHotel.setAddress("123 Test St");
        testHotel.setRating(4.5);

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setCapacity(2);
        testRoom.setTotalRooms(5);
        testRoom.setAvailable(true);
        testRoom.setDescription("Test room");
        testRoom.setAmenities("WiFi, TV, AC");
        testRoom.setHotel(testHotel);

        roomList = Arrays.asList(testRoom);
    }

    @Test
    void searchRooms_ShouldReturnResultsView_WithRooms() throws Exception {
        // Given
        when(roomService.getAvailableRoomsByCityAndDates(eq(city), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(roomList);

        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", checkInDate.format(formatter))
                        .param("checkOutDate", checkOutDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("rooms"))
                .andExpect(model().attributeExists("city"))
                .andExpect(model().attributeExists("checkInDate"))
                .andExpect(model().attributeExists("checkOutDate"))
                .andExpect(model().attributeExists("numberOfNights"))
                .andExpect(model().attribute("rooms", roomList));
    }

    @Test
    void searchRooms_ShouldReturnError_WhenCheckInDateMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkOutDate", checkOutDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Please select both check-in and check-out dates"));
    }

    @Test
    void searchRooms_ShouldReturnError_WhenCheckOutDateMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", checkInDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Please select both check-in and check-out dates"));
    }

    @Test
    void searchRooms_ShouldReturnError_WhenCheckInDateInPast() throws Exception {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", pastDate.format(formatter))
                        .param("checkOutDate", checkOutDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Check-in date cannot be in the past"));
    }

    @Test
    void searchRooms_ShouldReturnError_WhenCheckOutBeforeCheckIn() throws Exception {
        // Given
        LocalDate invalidCheckOut = checkInDate.minusDays(1);

        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", checkInDate.format(formatter))
                        .param("checkOutDate", invalidCheckOut.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Check-out date must be after check-in date"));
    }

    @Test
    void searchRooms_ShouldReturnError_WhenCheckInEqualsCheckOut() throws Exception {
        // Given
        LocalDate sameDate = checkInDate;

        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", sameDate.format(formatter))
                        .param("checkOutDate", sameDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Check-out date must be at least 1 day after check-in date"));
    }

    @Test
    void searchRooms_ShouldReturnError_WhenCityEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", "")
                        .param("checkInDate", checkInDate.format(formatter))
                        .param("checkOutDate", checkOutDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Please enter a city name"));
    }

    @Test
    void searchRooms_ShouldHandleServiceException() throws Exception {
        // Given
        when(roomService.getAvailableRoomsByCityAndDates(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", checkInDate.format(formatter))
                        .param("checkOutDate", checkOutDate.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Search failed due to system error. Please try again."));
    }

    @Test
    void searchRooms_ShouldDisplayErrorMessage_WhenErrorParamPresent() throws Exception {
        // When & Then
        mockMvc.perform(get("/search")
                        .param("city", city)
                        .param("checkInDate", checkInDate.format(formatter))
                        .param("checkOutDate", checkOutDate.format(formatter))
                        .param("error", "true")
                        .param("message", "Booking failed"))
                .andExpect(status().isOk())
                .andExpect(view().name("search/results"))
                .andExpect(model().attributeExists("error"));
    }
}