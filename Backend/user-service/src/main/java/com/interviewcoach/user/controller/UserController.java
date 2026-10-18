package com.interviewcoach.user.controller;

import com.interviewcoach.user.domain.User;
import com.interviewcoach.user.queue.QueueService;
import com.interviewcoach.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@SuppressWarnings("null")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final QueueService queueService;

    public UserController(UserRepository userRepository, QueueService queueService) {
        this.userRepository = userRepository;
        this.queueService = queueService;
    }

    private String getEmailFromPrincipal(OAuth2User principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String email = principal.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No email in principal");
        }
        return email.trim().toLowerCase();
    }

    private Map<String, Object> mapUserResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId() != null ? user.getId() : 0L);
        map.put("name", user.getName() != null ? user.getName() : "");
        map.put("email", user.getEmail() != null ? user.getEmail() : "");
        map.put("picture", user.getPicture() != null ? user.getPicture() : "");
        map.put("role", user.getRole() != null ? user.getRole().name() : "USER");
        map.put("active", user.isActive());
        map.put("provider", user.getProvider() != null ? user.getProvider() : "local");
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        return map;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(principal.getAttribute("name") != null ? principal.getAttribute("name") : "User");
                    newUser.setPicture(principal.getAttribute("picture"));
                    newUser.setRole(User.Role.USER);
                    newUser.setActive(true);
                    return userRepository.save(newUser);
                });

        return ResponseEntity.ok(mapUserResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, String> updates) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (updates.containsKey("name")) {
            String name = updates.get("name");
            if (name != null && !name.isBlank()) {
                user.setName(name.trim());
            }
        }
        if (updates.containsKey("picture")) {
            user.setPicture(updates.get("picture"));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(mapUserResponse(saved));
    }

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<User> userPage;

        if (search != null && !search.isBlank()) {
            userPage = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        Page<Map<String, Object>> responsePage = userPage.map(this::mapUserResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(mapUserResponse(user)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (payload.containsKey("active") && payload.get("active") != null) {
            user.setActive(payload.get("active"));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(mapUserResponse(saved));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String roleStr = payload.get("role");
        if (roleStr != null) {
            try {
                user.setRole(User.Role.valueOf(roleStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role. Allowed: USER, ADMIN");
            }
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(mapUserResponse(saved));
    }

    @PostMapping("/register")
    public Map<String, Object> registerOrLogin(@AuthenticationPrincipal OAuth2User principal, @Valid @RequestBody User user) {
        String principalEmail = getEmailFromPrincipal(principal);

        User saved = userRepository.findByEmail(principalEmail)
                .map(existing -> {
                    existing.setName(user.getName());
                    existing.setPicture(user.getPicture());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    user.setId(null);
                    user.setProvider(null);
                    user.setProviderId(null);
                    user.setEmail(principalEmail);
                    user.setRole(User.Role.USER);
                    user.setActive(true);
                    return userRepository.save(user);
                });
        return mapUserResponse(saved);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestHeader(value = "X-Confirm", required = false) String confirm) {
        if (!"confirm-delete".equals(confirm)) {
            return ResponseEntity.badRequest().body("Confirmation header missing");
        }

        String email = getEmailFromPrincipal(principal);
        return userRepository.findByEmail(email).map(user -> {
            try {
                queueService.publishUserDeleted(user.getId(), user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to publish user deleted event for user {}: {}", user.getId(), e.getMessage(), e);
            }

            user.setActive(false);
            userRepository.save(user);
            return ResponseEntity.ok("Account deactivated successfully.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/internal/by-email")
    public Map<String, Object> getUserByEmail(@RequestParam("email") String email) {
        return userRepository.findByEmail(email)
                .map(this::mapUserResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PutMapping("/internal/update")
    public Map<String, Object> updateUserInfo(@RequestParam("email") String email, @RequestBody Map<String, String> updates) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (updates.containsKey("name") && updates.get("name") != null) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("picture")) {
            user.setPicture(updates.get("picture"));
        }
        User saved = userRepository.save(user);
        return mapUserResponse(saved);
    }
}
