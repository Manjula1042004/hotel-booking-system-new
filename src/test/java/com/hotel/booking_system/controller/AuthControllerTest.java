package com.hotel.booking_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking_system.model.User;
import com.hotel.booking_system.service.UserService;
import com.hotel.booking_system.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserService userService;

    private User testUser;
    private UserDetails userDetails;
    private AuthController.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());

        loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("encodedPassword")
                .authorities("USER")
                .build();
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).loadUserByUsername("test@example.com");
        verify(jwtTokenUtil).generateToken(userDetails);
        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    void login_ShouldReturnError_WhenInvalidCredentials() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void login_ShouldReturnError_WhenUserNotFound() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Error generating token: User not found"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).loadUserByUsername("test@example.com");
        verify(jwtTokenUtil).generateToken(userDetails);
        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    void validateToken_ShouldReturnValid_WhenTokenValid() throws Exception {
        // Given
        String token = "Bearer valid.jwt.token";
        String jwtToken = "valid.jwt.token";

        when(jwtTokenUtil.extractUsername(jwtToken)).thenReturn("test@example.com");
        when(userService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(jwtToken, userDetails)).thenReturn(true);
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userService).loadUserByUsername("test@example.com");
        verify(jwtTokenUtil).validateToken(jwtToken, userDetails);
        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    void validateToken_ShouldReturnError_WhenInvalidTokenFormat() throws Exception {
        // Given
        String token = "InvalidFormat";

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid token format"));

        verify(jwtTokenUtil, never()).extractUsername(anyString());
    }

    @Test
    void validateToken_ShouldReturnError_WhenTokenInvalid() throws Exception {
        // Given
        String token = "Bearer invalid.jwt.token";
        String jwtToken = "invalid.jwt.token";

        when(jwtTokenUtil.extractUsername(jwtToken)).thenReturn("test@example.com");
        when(userService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(jwtToken, userDetails)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid token"));

        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userService).loadUserByUsername("test@example.com");
        verify(jwtTokenUtil).validateToken(jwtToken, userDetails);
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void refreshToken_ShouldReturnNewToken_WhenValid() throws Exception {
        // Given
        String token = "Bearer valid.jwt.token";
        String jwtToken = "valid.jwt.token";
        String newToken = "new.jwt.token";

        when(jwtTokenUtil.extractUsername(jwtToken)).thenReturn("test@example.com");
        when(userService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(jwtToken, userDetails)).thenReturn(true);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn(newToken);
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value(newToken))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userService).loadUserByUsername("test@example.com");
        verify(jwtTokenUtil).validateToken(jwtToken, userDetails);
        verify(jwtTokenUtil).generateToken(userDetails);
        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    void refreshToken_ShouldReturnError_WhenTokenInvalid() throws Exception {
        // Given
        String token = "Bearer invalid.jwt.token";
        String jwtToken = "invalid.jwt.token";

        when(jwtTokenUtil.extractUsername(jwtToken)).thenReturn("test@example.com");
        when(userService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(jwtToken, userDetails)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Cannot refresh invalid token"));

        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userService).loadUserByUsername("test@example.com");
        verify(jwtTokenUtil).validateToken(jwtToken, userDetails);
        verify(jwtTokenUtil, never()).generateToken(any());
    }
}