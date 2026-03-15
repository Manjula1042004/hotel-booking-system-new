package com.hotel.booking_system.config;

import com.hotel.booking_system.util.JwtTokenUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
@ComponentScan(basePackages = "com.hotel.booking_system") // Add this to scan all components
public class TestSecurityConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(User.withUsername("admin@test.com")
                .password(passwordEncoder().encode("admin123"))
                .authorities("ADMIN")
                .build());

        manager.createUser(User.withUsername("user@test.com")
                .password(passwordEncoder().encode("user123"))
                .authorities("USER")
                .build());

        manager.createUser(User.withUsername("test@example.com")
                .password(passwordEncoder().encode("password123"))
                .authorities("USER")
                .build());

        return manager;
    }

    @Bean
    @Primary
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }
}