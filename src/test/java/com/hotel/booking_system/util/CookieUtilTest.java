package com.hotel.booking_system.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CookieUtilTest {

    @InjectMocks
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void createAccessTokenCookie_ShouldCreateCookie() {
        // Given
        String token = "jwt.token.value";
        long maxAgeSeconds = 3600;

        // When
        ResponseCookie cookie = cookieUtil.createAccessTokenCookie(token, maxAgeSeconds);

        // Then
        assertNotNull(cookie);
        assertEquals("accessToken", cookie.getName());
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(maxAgeSeconds, cookie.getMaxAge().getSeconds());
        assertEquals("Strict", cookie.getSameSite());
    }

    @Test
    void createRefreshTokenCookie_ShouldCreateCookie() {
        // Given
        String token = "refresh.token.value";

        // When
        ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(token);

        // Then
        assertNotNull(cookie);
        assertEquals("refreshToken", cookie.getName());
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(7 * 24 * 60 * 60, cookie.getMaxAge().getSeconds());
        assertEquals("Strict", cookie.getSameSite());
    }

    @Test
    void deleteCookie_ShouldAddCookieWithMaxAgeZero() {
        // Given
        String cookieName = "testCookie";

        // When
        cookieUtil.deleteCookie(response, cookieName);

        // Then
        verify(response).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void getCookieValue_ShouldReturnValue_WhenCookieExists() {
        // Given
        Cookie[] cookies = {
                new Cookie("testCookie", "testValue"),
                new Cookie("otherCookie", "otherValue")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        String value = cookieUtil.getCookieValue(request, "testCookie");

        // Then
        assertEquals("testValue", value);
    }

    @Test
    void getCookieValue_ShouldReturnNull_WhenCookieNotExists() {
        // Given
        Cookie[] cookies = {
                new Cookie("otherCookie", "otherValue")
        };
        when(request.getCookies()).thenReturn(cookies);

        // When
        String value = cookieUtil.getCookieValue(request, "testCookie");

        // Then
        assertNull(value);
    }

    @Test
    void getCookieValue_ShouldReturnNull_WhenNoCookies() {
        // Given
        when(request.getCookies()).thenReturn(null);

        // When
        String value = cookieUtil.getCookieValue(request, "testCookie");

        // Then
        assertNull(value);
    }
}