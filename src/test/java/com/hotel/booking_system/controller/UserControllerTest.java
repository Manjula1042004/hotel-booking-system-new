package com.hotel.booking_system.controller;

import com.hotel.booking_system.model.User;
import com.hotel.booking_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail(userEmail);
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void userDashboard_ShouldReturnDashboardView() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);
        when(userService.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/user/dashboard")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void userDashboard_ShouldRedirect_WhenUserNotFound() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);
        when(userService.getUserByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/user/dashboard")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?error"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void userProfile_ShouldReturnProfileView() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);
        when(userService.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/user/profile")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void userProfile_ShouldThrowException_WhenUserNotFound() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);
        when(userService.getUserByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then - Fixed exception assertion
        mockMvc.perform(get("/user/profile")
                        .principal(principal))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertTrue(exception instanceof RuntimeException);
                    assertEquals("User not found", exception.getMessage());
                });
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_ShouldUpdateAndRedirect() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);
        when(userService.getUserByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/user/profile/update")
                        .with(csrf())
                        .principal(principal)
                        .param("name", "Updated Name")
                        .param("email", "updated@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/profile?success"));

        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_ShouldThrowException_WhenUserNotFound() throws Exception {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);
        when(userService.getUserByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then - Fixed exception assertion
        mockMvc.perform(post("/user/profile/update")
                        .with(csrf())
                        .principal(principal)
                        .param("name", "Updated Name")
                        .param("email", "updated@example.com"))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertTrue(exception instanceof RuntimeException);
                    assertEquals("User not found", exception.getMessage());
                });

        verify(userService, never()).updateUser(anyLong(), any(User.class));
    }
}