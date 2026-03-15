package com.hotel.booking_system.service;

import com.hotel.booking_system.model.User;
import com.hotel.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                getAuthorities(user)
        );
    }

    private List<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        return authorities;
    }

    @Transactional
    public User registerUser(User user) {
        System.out.println("🔹 Attempting to register user with email: " + user.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            System.err.println("❌ Email already exists: " + user.getEmail());
            throw new RuntimeException("Email already exists");
        }

        try {
            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Set ALL required fields with proper values
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(true);

            // Set the new fields
            user.setEmailVerified(false);
            user.setEmailVerifiedAt(null);
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(null);
            user.setLockoutTime(null);
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setProfileImageUrl(null);
            user.setProvider("LOCAL");
            user.setProviderId(null);
            user.setTwoFactorEnabled(false);

            // Set timestamps
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            System.out.println("🔹 User details being saved:");
            System.out.println("   - Name: " + user.getName());
            System.out.println("   - Email: " + user.getEmail());
            System.out.println("   - Role: " + user.getRole());
            System.out.println("   - Enabled: " + user.isEnabled());
            System.out.println("   - Provider: " + user.getProvider());

            // Save the user
            User savedUser = userRepository.save(user);
            System.out.println("✅ User saved successfully with ID: " + savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            System.err.println("❌ Error during user registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void recordLoginSuccess(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setLockoutTime(null);
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordLoginFailure(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
        });
    }
}