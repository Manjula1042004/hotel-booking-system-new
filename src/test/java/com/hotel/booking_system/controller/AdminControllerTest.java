package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.*;
import com.hotel.booking_system.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    private User testAdmin;
    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;
    private Payment testPayment;
    private final Long userId = 1L;
    private final Long hotelId = 1L;
    private final Long roomId = 1L;
    private final Long bookingId = 1L;
    private final Long paymentId = 1L;

    @BeforeEach
    void setUp() {
        testAdmin = new User();
        testAdmin.setId(2L);
        testAdmin.setName("Admin User");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setRole(User.Role.ADMIN);

        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("user@test.com");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());

        testHotel = new Hotel();
        testHotel.setId(hotelId);
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setRating(4.5);

        testRoom = new Room();
        testRoom.setId(roomId);
        testRoom.setRoomNumber("101");
        testRoom.setRoomType("Deluxe");
        testRoom.setPrice(new BigDecimal("200.00"));
        testRoom.setHotel(testHotel);

        testBooking = new Booking();
        testBooking.setId(bookingId);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setTotalAmount(new BigDecimal("400.00"));
        testBooking.setStatus(Booking.BookingStatus.PENDING);
        testBooking.setCreatedAt(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId(paymentId);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("400.00"));
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        testPayment.setPaymentDate(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboard_ShouldReturnDashboardView() throws Exception {
        // Given
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testAdmin));
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser));
        when(hotelService.getAllHotels()).thenReturn(Arrays.asList(testHotel));
        when(bookingService.getAllBookings()).thenReturn(Arrays.asList(testBooking));
        when(paymentService.getAllPayments()).thenReturn(Arrays.asList(testPayment));
        when(paymentService.getTotalRevenue()).thenReturn(new BigDecimal("400.00"));

        // When & Then
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalUsers"))
                .andExpect(model().attributeExists("totalHotels"))
                .andExpect(model().attributeExists("totalBookings"))
                .andExpect(model().attributeExists("totalRevenue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageUsers_ShouldReturnUsersView() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, testAdmin));

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("totalUsers"))
                .andExpect(model().attributeExists("adminUsers"))
                .andExpect(model().attributeExists("regularUsers"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldRedirectToUsers() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(post("/admin/users/{id}/delete", userId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?deleted=true"));

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void makeUserAdmin_ShouldUpdateUserRole() throws Exception {
        // Given
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/admin/users/{id}/make-admin", userId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?role_updated=true"));

        verify(userService).getUserById(userId);
        verify(userService).updateUser(anyLong(), any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageHotels_ShouldReturnHotelsView() throws Exception {
        // Given
        when(hotelService.getAllHotels()).thenReturn(Arrays.asList(testHotel));

        // When & Then
        mockMvc.perform(get("/admin/hotels"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/hotels"))
                .andExpect(model().attributeExists("hotels"))
                .andExpect(model().attributeExists("totalHotels"))
                .andExpect(model().attributeExists("averageRating"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createHotelForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/admin/hotels/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/hotel-form"))
                .andExpect(model().attributeExists("hotel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createHotel_ShouldRedirectToHotels() throws Exception {
        // Given
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(testHotel);

        // When & Then
        mockMvc.perform(post("/admin/hotels")
                        .with(csrf())
                        .param("name", "New Hotel")
                        .param("city", "New City")
                        .param("address", "123 New St")
                        .param("rating", "4.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels?created=true"));

        verify(hotelService).createHotel(any(Hotel.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editHotelForm_ShouldReturnFormView() throws Exception {
        // Given
        when(hotelService.getHotelById(hotelId)).thenReturn(Optional.of(testHotel));

        // When & Then
        mockMvc.perform(get("/admin/hotels/{id}/edit", hotelId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/hotel-form"))
                .andExpect(model().attributeExists("hotel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateHotel_ShouldRedirectToHotels() throws Exception {
        // Given
        when(hotelService.updateHotel(anyLong(), any(Hotel.class))).thenReturn(testHotel);

        // When & Then
        mockMvc.perform(post("/admin/hotels/{id}", hotelId)
                        .with(csrf())
                        .param("name", "Updated Hotel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels?updated=true"));

        verify(hotelService).updateHotel(anyLong(), any(Hotel.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteHotel_ShouldRedirectToHotels() throws Exception {
        // Given
        doNothing().when(hotelService).deleteHotel(hotelId);

        // When & Then
        mockMvc.perform(post("/admin/hotels/{id}/delete", hotelId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/hotels?deleted=true"));

        verify(hotelService).deleteHotel(hotelId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manageBookings_ShouldReturnBookingsView() throws Exception {
        // Given
        when(bookingService.getAllBookings()).thenReturn(Arrays.asList(testBooking));

        // When & Then
        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/bookings"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attributeExists("totalBookings"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelBooking_ShouldRedirectToBookings() throws Exception {
        // Given
        when(bookingService.cancelBooking(bookingId)).thenReturn(testBooking);

        // When & Then
        mockMvc.perform(post("/admin/bookings/{id}/cancel", bookingId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/bookings?cancelled=true"));

        verify(bookingService).cancelBooking(bookingId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmBooking_ShouldRedirectToBookings() throws Exception {
        // Given
        when(bookingService.getBookingById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingService.updateBooking(any(Booking.class))).thenReturn(testBooking);

        // When & Then
        mockMvc.perform(post("/admin/bookings/{id}/confirm", bookingId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/bookings?confirmed=true"));

        verify(bookingService).getBookingById(bookingId);
        verify(bookingService).updateBooking(any(Booking.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void managePayments_ShouldReturnPaymentsView() throws Exception {
        // Given
        when(paymentService.getAllPayments()).thenReturn(Arrays.asList(testPayment));
        when(paymentService.getTotalRevenue()).thenReturn(new BigDecimal("400.00"));

        // When & Then
        mockMvc.perform(get("/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/payments"))
                .andExpect(model().attributeExists("payments"))
                .andExpect(model().attributeExists("totalRevenue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void refundPayment_ShouldRedirectToPayments() throws Exception {
        // Given
        when(paymentService.refundPayment(paymentId)).thenReturn(testPayment);

        // When & Then
        mockMvc.perform(post("/admin/payments/{id}/refund", paymentId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/payments?refunded=true"));

        verify(paymentService).refundPayment(paymentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_ShouldReturnStats() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser));
        when(hotelService.getAllHotels()).thenReturn(Arrays.asList(testHotel));
        when(bookingService.getAllBookings()).thenReturn(Arrays.asList(testBooking));
        when(paymentService.getAllPayments()).thenReturn(Arrays.asList(testPayment));
        when(paymentService.getTotalRevenue()).thenReturn(new BigDecimal("400.00"));

        // When & Then
        mockMvc.perform(get("/admin/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalUsers").value(1))
                .andExpect(jsonPath("$.totalHotels").value(1))
                .andExpect(jsonPath("$.totalBookings").value(1));
    }
}