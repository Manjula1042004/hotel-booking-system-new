package com.hotel.booking_system.controller;

import com.hotel.booking_system.config.TestSecurityConfig;
import com.hotel.booking_system.model.*;
import com.hotel.booking_system.service.BookingService;
import com.hotel.booking_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;
    private final Long userId = 1L;
    private final Long roomId = 1L;
    private final Long bookingId = 1L;
    private final LocalDate checkInDate = LocalDate.now().plusDays(1);
    private final LocalDate checkOutDate = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);

        testHotel = new Hotel();
        testHotel.setId(1L);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");

        testRoom = new Room();
        testRoom.setId(roomId);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setCapacity(2);
        testRoom.setHotel(testHotel);

        testBooking = new Booking();
        testBooking.setId(bookingId);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(checkInDate);
        testBooking.setCheckOutDate(checkOutDate);
        testBooking.setNumberOfGuests(2);
        testBooking.setRoomCount(1);
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.PENDING);
        testBooking.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void getUserBookings_ShouldReturnBookingsView() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getUserBookings(userId)).thenReturn(Arrays.asList(testBooking));

        // When & Then
        mockMvc.perform(get("/bookings/my-bookings")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/my-bookings"))
                .andExpect(model().attributeExists("bookings"));
    }

    @Test
    @WithMockUser
    void createBooking_ShouldCreateAndRedirect() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.createBooking(anyLong(), anyLong(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(testBooking);

        // When & Then
        mockMvc.perform(post("/bookings/book")
                        .with(csrf())
                        .principal(principal)
                        .param("roomId", roomId.toString())
                        .param("checkInDate", checkInDate.toString())
                        .param("checkOutDate", checkOutDate.toString())
                        .param("numberOfGuests", "2")
                        .param("roomCount", "1")
                        .param("specialRequests", "Test request"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/confirmation?bookingId=" + bookingId));

        verify(bookingService).createBooking(anyLong(), anyLong(), any(), any(), anyInt(), anyInt(), anyString());
    }

    @Test
    @WithMockUser
    void createBooking_ShouldRedirectWithError_WhenInvalidGuests() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then - Guest count too high
        mockMvc.perform(post("/bookings/book")
                        .with(csrf())
                        .principal(principal)
                        .param("roomId", roomId.toString())
                        .param("checkInDate", checkInDate.toString())
                        .param("checkOutDate", checkOutDate.toString())
                        .param("numberOfGuests", "25")
                        .param("roomCount", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search?error=invalid_guests"));

        verify(bookingService, never()).createBooking(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    void createBooking_ShouldRedirectWithError_WhenInvalidRooms() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then - Room count too high
        mockMvc.perform(post("/bookings/book")
                        .with(csrf())
                        .principal(principal)
                        .param("roomId", roomId.toString())
                        .param("checkInDate", checkInDate.toString())
                        .param("checkOutDate", checkOutDate.toString())
                        .param("numberOfGuests", "2")
                        .param("roomCount", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search?error=invalid_rooms"));

        verify(bookingService, never()).createBooking(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    void createBooking_ShouldRedirectWithError_WhenServiceThrowsException() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.createBooking(anyLong(), anyLong(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("Booking failed"));

        // When & Then
        mockMvc.perform(post("/bookings/book")
                        .with(csrf())
                        .principal(principal)
                        .param("roomId", roomId.toString())
                        .param("checkInDate", checkInDate.toString())
                        .param("checkOutDate", checkOutDate.toString())
                        .param("numberOfGuests", "2")
                        .param("roomCount", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search?error=booking_failed&message=Booking+failed"));

        verify(bookingService).createBooking(anyLong(), anyLong(), any(), any(), anyInt(), anyInt(), anyString());
    }

    @Test
    void bookingConfirmation_ShouldReturnConfirmationView() throws Exception {
        // Given
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        mockMvc.perform(get("/bookings/confirmation")
                        .param("bookingId", bookingId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/confirmation"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    void bookingConfirmation_ShouldReturnError_WhenBookingNotFound() throws Exception {
        // Given
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/bookings/confirmation")
                        .param("bookingId", bookingId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/confirmation"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelBooking_ShouldCancelAndRedirect() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingService.cancelBooking(bookingId)).thenReturn(testBooking);

        // When & Then
        mockMvc.perform(post("/bookings/{id}/cancel", bookingId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/my-bookings?cancelled=true"));

        verify(bookingService).cancelBooking(bookingId);
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void cancelBooking_ShouldReturnAccessDenied_WhenNotOwnBooking() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("other@example.com");
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));

        // When & Then
        mockMvc.perform(post("/bookings/{id}/cancel", bookingId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));

        verify(bookingService, never()).cancelBooking(anyLong());
    }

    @Test
    void checkAvailability_ShouldReturnAvailability() throws Exception {
        // Given
        int availableRooms = 3;
        when(bookingService.getAvailableRoomsForDateRange(roomId, checkInDate, checkOutDate))
                .thenReturn(availableRooms);

        // When & Then
        mockMvc.perform(get("/bookings/availability")
                        .param("roomId", roomId.toString())
                        .param("checkInDate", checkInDate.toString())
                        .param("checkOutDate", checkOutDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.availableRooms").value(availableRooms))
                .andExpect(jsonPath("$.roomId").value(roomId));
    }

    @Test
    void checkAvailability_ShouldReturnError_WhenExceptionOccurs() throws Exception {
        // Given
        when(bookingService.getAvailableRoomsForDateRange(roomId, checkInDate, checkOutDate))
                .thenThrow(new RuntimeException("Error checking availability"));

        // When & Then
        mockMvc.perform(get("/bookings/availability")
                        .param("roomId", roomId.toString())
                        .param("checkInDate", checkInDate.toString())
                        .param("checkOutDate", checkOutDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }
}