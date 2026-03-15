package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.Hotel;
import com.hotel.booking_system.model.Room;
import com.hotel.booking_system.service.HotelService;
import com.hotel.booking_system.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private HotelService hotelService;

    private Hotel testHotel;
    private Room testRoom;
    private List<Room> roomList;
    private final Long hotelId = 1L;
    private final Long roomId = 1L;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setId(hotelId);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setAddress("123 Test St");
        testHotel.setRating(4.5);
        testHotel.setCreatedAt(LocalDateTime.now());

        testRoom = new Room();
        testRoom.setId(roomId);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setCapacity(2);
        testRoom.setTotalRooms(5);
        testRoom.setAvailable(true);
        testRoom.setDescription("Test room");
        testRoom.setAmenities("WiFi, TV, AC");
        testRoom.setHotel(testHotel);
        testRoom.setCreatedAt(LocalDateTime.now());

        roomList = Arrays.asList(testRoom);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoomsByHotel_ShouldReturnRoomsView() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.of(testHotel));
        when(roomService.getRoomsByHotelId(hotelId)).thenReturn(roomList);

        // When & Then
        mockMvc.perform(get("/admin/rooms/hotel/{hotelId}", hotelId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rooms"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(model().attributeExists("rooms"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoomsByHotel_ShouldRedirect_WhenHotelNotFound() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/admin/rooms/hotel/{hotelId}", hotelId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoomForm_ShouldReturnFormView() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.of(testHotel));

        // When & Then
        mockMvc.perform(get("/admin/rooms/hotel/{hotelId}/new", hotelId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/room-form"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("hotel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoomForm_ShouldRedirect_WhenHotelNotFound() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/admin/rooms/hotel/{hotelId}/new", hotelId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels?error=Hotel+not+found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_ShouldCreateAndRedirect() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.of(testHotel));
        when(roomService.createRoom(any(Room.class))).thenReturn(testRoom);

        // When & Then
        mockMvc.perform(post("/admin/rooms/hotel/{hotelId}", hotelId)
                        .with(csrf())
                        .param("roomNumber", "102")
                        .param("roomType", "Suite")
                        .param("price", "300.00")
                        .param("capacity", "4")
                        .param("totalRooms", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms/hotel/" + hotelId + "?created=true"));

        verify(roomService).createRoom(any(Room.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_ShouldReturnFormWithError_WhenExceptionOccurs() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.of(testHotel));
        when(roomService.createRoom(any(Room.class))).thenThrow(new RuntimeException("Error creating room"));

        // When & Then
        mockMvc.perform(post("/admin/rooms/hotel/{hotelId}", hotelId)
                        .with(csrf())
                        .param("roomNumber", "102")
                        .param("roomType", "Suite")
                        .param("price", "300.00")
                        .param("capacity", "4"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/room-form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editRoomForm_ShouldReturnFormView() throws Exception {
        // Given
        when(roomService.getRoomById(roomId)).thenReturn(testRoom);

        // When & Then
        mockMvc.perform(get("/admin/rooms/{id}/edit", roomId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/room-form"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("hotel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editRoomForm_ShouldRedirect_WhenRoomNotFound() throws Exception {
        // Given
        when(roomService.getRoomById(roomId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/admin/rooms/{id}/edit", roomId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels?error=Room+not+found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoom_ShouldUpdateAndRedirect() throws Exception {
        // Given
        Room existingRoom = testRoom;
        when(roomService.getRoomById(roomId)).thenReturn(existingRoom);
        when(roomService.updateRoom(anyLong(), any(Room.class))).thenReturn(testRoom);

        // When & Then
        mockMvc.perform(post("/admin/rooms/{id}", roomId)
                        .with(csrf())
                        .param("roomNumber", "202")
                        .param("roomType", "Updated Suite")
                        .param("price", "350.00")
                        .param("capacity", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms/hotel/" + hotelId + "?updated=true"));

        verify(roomService).updateRoom(eq(roomId), any(Room.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoom_ShouldReturnFormWithError_WhenRoomNotFound() throws Exception {
        // Given
        when(roomService.getRoomById(roomId)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/admin/rooms/{id}", roomId)
                        .with(csrf())
                        .param("roomNumber", "202"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/room-form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoom_ShouldDeleteAndRedirect() throws Exception {
        // Given
        when(roomService.getRoomById(roomId)).thenReturn(testRoom);
        doNothing().when(roomService).deleteRoom(roomId);

        // When & Then
        mockMvc.perform(post("/admin/rooms/{id}/delete", roomId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms/hotel/" + hotelId + "?deleted=true"));

        verify(roomService).deleteRoom(roomId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoom_ShouldRedirectToHotels_WhenRoomNotFound() throws Exception {
        // Given
        when(roomService.getRoomById(roomId)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/admin/rooms/{id}/delete", roomId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels?error=Room+not+found"));

        verify(roomService, never()).deleteRoom(anyLong());
    }
}