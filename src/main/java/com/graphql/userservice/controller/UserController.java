package com.graphql.userservice.controller;

import com.graphql.userservice.domain.CreateUserRequest;
import com.graphql.userservice.domain.User;
import com.graphql.userservice.service.UserResponse;
import com.graphql.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }

    /*@GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #userId.toString()")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(UserResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }*/

    /** Alternate implementation  **/
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId, Authentication authentication) {

        // Manual authorization check
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = authentication.getName().equals(userId.toString());

        if (!hasAdminRole && !isOwner) {
            throw new AccessDeniedException("Access denied: insufficient privileges");
        }

        /*return userService.findById(userId);*/
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(UserResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}