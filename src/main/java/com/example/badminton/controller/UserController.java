package com.example.badminton.controller;

import com.example.badminton.dto.request.UserPostRequest;
import com.example.badminton.dto.request.UserUpdateRequest;
import com.example.badminton.dto.response.BookingResponse;
import com.example.badminton.dto.response.UserResponse;
import com.example.badminton.entity.Role;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            Authentication authentication,
            @Valid @RequestBody UserPostRequest request) {
        if (request.getRole() != null && request.getRole() != Role.ROLE_CUSTOMER) {
            if (!isAdmin(authentication)) {
                throw new BadRequestException("Only admin can assign roles");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userService.createUser(userService.toCreateRequest(request)));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(userService.toRegisterRequest(request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(keyword, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/bookings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('CUSTOMER') and @userSecurity.isOwner(#userId, authentication))")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
