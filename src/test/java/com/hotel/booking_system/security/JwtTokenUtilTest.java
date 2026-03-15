package com.hotel.booking_system.security;

import com.hotel.booking_system.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private UserDetails userDetails;
    private final String secret = "myTestSecretKeyForJwtTokenGenerationThatIsLongEnoughForHS256";
    private final Long expiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", expiration);

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // When
        String token = jwtTokenUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        String username = jwtTokenUtil.extractUsername(token);

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenValid() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameMismatch() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        // When
        boolean isValid = jwtTokenUtil.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractExpiration_ShouldReturnExpirationDate() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        var expirationDate = jwtTokenUtil.extractExpiration(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.getTime() > System.currentTimeMillis());
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenNotExpired() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        boolean isExpired = jwtTokenUtil.extractExpiration(token).before(new java.util.Date());

        // Then
        assertFalse(isExpired);
    }

    // Note: Testing expired token would require manipulating time or using a fixed expiration
}
