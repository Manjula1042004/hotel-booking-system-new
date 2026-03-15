package com.hotel.booking_system.service;

import com.hotel.booking_system.model.User;
import com.hotel.booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail(testEmail);
        testUser.setPassword(testPassword);
        testUser.setRole(User.Role.USER);
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(userDetails);
        assertEquals(testEmail, userDetails.getUsername());
        assertEquals(testPassword, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(testEmail);
        });
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void registerUser_ShouldSaveUser_WhenEmailNotExists() {
        // Given
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User registeredUser = userService.registerUser(testUser);

        // Then
        assertNotNull(registeredUser);
        assertEquals(testUser.getName(), registeredUser.getName());
        assertEquals(testUser.getEmail(), registeredUser.getEmail());
        verify(userRepository).existsByEmail(testEmail);
        verify(passwordEncoder).encode(testPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(testUser);
        });
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(testEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.getUserByEmail(testEmail);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.getUserById(userId);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenUserExists() {
        // Given
        Long userId = 1L;
        User updateDetails = new User();
        updateDetails.setName("Updated Name");
        updateDetails.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User updatedUser = userService.updateUser(userId, updateDetails);

        // Then
        assertNotNull(updatedUser);
        assertEquals("Updated Name", testUser.getName());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        // Given
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    void recordLoginSuccess_ShouldUpdateUser() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLoginSuccess(testEmail);

        // Then
        assertNotNull(testUser.getLastLoginAt());
        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNull(testUser.getLockoutTime());
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository).save(testUser);
    }

    @Test
    void recordLoginFailure_ShouldIncrementFailedAttempts() {
        // Given
        testUser.setFailedLoginAttempts(0);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLoginFailure(testEmail);

        // Then
        assertEquals(1, testUser.getFailedLoginAttempts());
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository).save(testUser);
    }

    @Test
    void registerUser_ShouldSetAllRequiredFields() {
        // Given
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");
        newUser.setRole(User.Role.USER);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        // When
        User registeredUser = userService.registerUser(newUser);

        // Then
        assertNotNull(registeredUser);
        assertTrue(registeredUser.isEnabled());
        assertTrue(registeredUser.isAccountNonExpired());
        assertTrue(registeredUser.isAccountNonLocked());
        assertTrue(registeredUser.isCredentialsNonExpired());
        assertFalse(registeredUser.getEmailVerified());
        assertEquals(0, registeredUser.getFailedLoginAttempts());
        assertEquals("LOCAL", registeredUser.getProvider());
        assertFalse(registeredUser.getTwoFactorEnabled());
        assertNotNull(registeredUser.getCreatedAt());
        assertNotNull(registeredUser.getUpdatedAt());
        verify(userRepository).save(any(User.class));
    }
}