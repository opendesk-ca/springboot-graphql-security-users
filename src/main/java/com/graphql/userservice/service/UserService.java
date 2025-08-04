package com.graphql.userservice.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserService {

    // Hardcoded users for testing
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeUsers() {
        log.info("Initializing hardcoded users...");

        // Create some test users
        User admin = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of("ADMIN", "USER"))
                .createdAt(Instant.now())
                .build();

        User regularUser = User.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("USER"))
                .createdAt(Instant.now())
                .build();

        User manager = User.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .email("manager@example.com")
                .firstName("Jane")
                .lastName("Manager")
                .roles(Set.of("MANAGER", "USER"))
                .createdAt(Instant.now())
                .build();

        users.put(admin.getId(), admin);
        users.put(regularUser.getId(), regularUser);
        users.put(manager.getId(), manager);

        log.info("Initialized {} hardcoded users", users.size());
    }

    public User createUser(CreateUserRequest request) {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .roles(Set.of("USER")) // Default role
                .createdAt(Instant.now())
                .build();

        users.put(userId, user);
        log.info("Created new user with ID: {}", userId);
        return user;
    }

    public Optional<User> getUserById(UUID userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public Optional<User> getUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}