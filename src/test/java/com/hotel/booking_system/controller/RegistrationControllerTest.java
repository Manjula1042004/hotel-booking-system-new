package com.hotel.booking_system.controller;

import com.hotel.booking_system.config.TestSecurityConfig;
import com.hotel.booking_system.model.User;
import com.hotel.booking_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
    }

    @Test
    void showRegistrationForm_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerUser_ShouldRegisterUser_WhenValidUserAndUserRole() throws Exception {
        // Given
        when(userService.registerUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setRole(User.Role.USER);
            return user;
        });

        // When & Then
        mockMvc.perform(post("/register")
                        .param("name", "Test User")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?user_registered"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldRegisterAdmin_WhenValidUserAndAdminRole() throws Exception {
        // Given
        when(userService.registerUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setRole(User.Role.ADMIN);
            return user;
        });

        // When & Then
        mockMvc.perform(post("/register")
                        .param("name", "Admin User")
                        .param("email", "admin@example.com")
                        .param("password", "password123")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?admin_registered"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnError_WhenEmailExists() throws Exception {
        // Given
        when(userService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/register")
                        .param("name", "Test User")
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("user"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    void registerUser_ShouldSetUserRole_WhenRoleNotSpecified() throws Exception {
        // Given
        when(userService.registerUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When & Then - No role parameter (should default to USER in controller)
        mockMvc.perform(post("/register")
                        .param("name", "Test User")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?user_registered"));

        verify(userService).registerUser(any(User.class));
    }
}