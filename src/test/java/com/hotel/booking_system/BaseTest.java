package com.hotel.booking_system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseTest {

    // Common test data
    protected static final Long USER_ID = 1L;
    protected static final Long HOTEL_ID = 1L;
    protected static final Long ROOM_ID = 1L;
    protected static final Long BOOKING_ID = 1L;
    protected static final Long PAYMENT_ID = 1L;
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_PASSWORD = "password123";

    protected LocalDate today = LocalDate.now();
    protected LocalDate tomorrow = today.plusDays(1);
    protected LocalDate dayAfterTomorrow = today.plusDays(2);
    protected LocalDate nextWeek = today.plusDays(7);

    @BeforeEach
    public void setUp() {
        // Common setup
    }
}