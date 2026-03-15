package com.hotel.booking_system.security;

import com.hotel.booking_system.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtRequestFilterTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    private UserDetails userDetails;
    private final String validToken = "Bearer valid.jwt.token";
    private final String jwtToken = "valid.jwt.token";
    private final String username = "test@example.com";

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenTokenValid() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenUtil.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(jwtToken, userDetails)).thenReturn(true);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtTokenUtil).validateToken(jwtToken, userDetails);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenNoToken() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidTokenFormat() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenTokenExpired() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenUtil.extractUsername(jwtToken)).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenTokenInvalid() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenUtil.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(jwtToken, userDetails)).thenReturn(false);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).extractUsername(jwtToken);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtTokenUtil).validateToken(jwtToken, userDetails);
    }
}