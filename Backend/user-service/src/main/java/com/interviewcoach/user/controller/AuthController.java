package com.interviewcoach.user.controller;

import com.interviewcoach.user.domain.User;
import com.interviewcoach.user.queue.QueueService;
import com.interviewcoach.user.repository.UserRepository;
import com.interviewcoach.user.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@SuppressWarnings("null")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,128}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final QueueService queueService;

    @Value("${APP_FRONTEND_URL:http://localhost:4200}")
    private String frontendUrl;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            QueueService queueService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.queueService = queueService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (!isStrongPassword(request.password())) {
            return badRequest("Password must be at least 8 characters and include uppercase, lowercase, number, and special character.");
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already in use"));
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProvider("local");
        user.setRole(User.Role.USER);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        userRepository.save(user);

        sendVerificationEmail(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Verification email sent"));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody TokenRequest request) {
        if (request.token() == null || request.token().isBlank()) {
            return badRequest("Verification token is required");
        }

        return userRepository.findByVerificationToken(request.token())
                .map(user -> {
                    if (isExpired(user.getVerificationTokenExpiresAt())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.<String, Object>of("error", "Verification token expired"));
                    }
                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    user.setVerificationTokenExpiresAt(null);
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.<String, Object>of("message", "Email verified"));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.<String, Object>of("error", "Invalid verification token")));
    }

    @PostMapping("/resend")
    public ResponseEntity<Map<String, Object>> resendVerification(@Valid @RequestBody EmailRequest request) {
        String email = normalizeEmail(request.email());
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.isEmailVerified()) {
                        return ResponseEntity.ok(Map.<String, Object>of("message", "Email already verified"));
                    }
                    user.setVerificationToken(UUID.randomUUID().toString());
                    user.setVerificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
                    userRepository.save(user);
                    sendVerificationEmail(user);
                    return ResponseEntity.ok(Map.<String, Object>of("message", "Verification email sent"));
                })
                .orElseGet(() -> ResponseEntity.ok(Map.<String, Object>of("message", "If an account exists, a verification email will be sent")));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String email = normalizeEmail(request.email());
        return userRepository.findByEmail(email)
                .filter(user -> user.getPasswordHash() != null)
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(user -> {
                    if (!user.isEmailVerified()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.<String, Object>of("error", "Email not verified"));
                    }
                    return ResponseEntity.ok(Map.<String, Object>of("token", jwtUtil.generateToken(user.getEmail())));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.<String, Object>of("error", "Invalid email or password")));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody EmailRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email)
                .filter(user -> user.getPasswordHash() != null)
                .ifPresent(user -> {
                    user.setPasswordResetToken(UUID.randomUUID().toString());
                    user.setPasswordResetTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
                    userRepository.save(user);
                    sendPasswordResetEmail(user);
                });

        return ResponseEntity.ok(Map.of("message", "If an account exists, a password reset email will be sent"));
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestBody TokenRequest request) {
        if (request.token() == null || request.token().isBlank()) {
            return badRequest("Reset token is required");
        }

        return userRepository.findByPasswordResetToken(request.token())
                .filter(user -> !isExpired(user.getPasswordResetTokenExpiresAt()))
                .map(user -> ResponseEntity.ok(Map.<String, Object>of("valid", true)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.<String, Object>of("error", "Invalid or expired reset token")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!isStrongPassword(request.password())) {
            return badRequest("Password must be at least 8 characters and include uppercase, lowercase, number, and special character.");
        }

        return userRepository.findByPasswordResetToken(request.token())
                .map(user -> {
                    if (isExpired(user.getPasswordResetTokenExpiresAt())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.<String, Object>of("error", "Reset token expired"));
                    }
                    user.setPasswordHash(passwordEncoder.encode(request.password()));
                    user.setPasswordResetToken(null);
                    user.setPasswordResetTokenExpiresAt(null);
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.<String, Object>of("message", "Password reset successful"));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.<String, Object>of("error", "Invalid reset token")));
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser() {
        Map<String, Object> response = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }

        // Extract email from principal - could be String (JWT) or OAuth2User (session)
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            email = (String) principal;
        } else if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        } else {
            email = principal.toString();
        }

        if (email == null) {
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }

        User dbUser = userRepository.findByEmail(email).orElse(null);

        if (dbUser == null) {
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", true);
        response.put("name", dbUser.getName());
        response.put("email", dbUser.getEmail());
        response.put("picture", dbUser.getPicture());

        // Return dbUser profile data so frontend can check profile completeness
        // Return dbUser profile data so frontend can check profile and roles
        Map<String, Object> dbUserMap = new HashMap<>();
        dbUserMap.put("id", dbUser.getId());
        dbUserMap.put("role", dbUser.getRole());
        dbUserMap.put("isActive", dbUser.isActive());
        response.put("role", dbUser.getRole());
        response.put("dbUser", dbUserMap);

        return ResponseEntity.ok(response);
    }

    private void sendVerificationEmail(User user) {
        String verificationUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                .path("/verify-email")
                .queryParam("token", user.getVerificationToken())
                .build()
                .toUriString();

        sendEmail(user.getEmail(), "Verify your account email",
                "Welcome! Please verify your email address to complete your registration.",
                verificationUrl);
    }

    private void sendPasswordResetEmail(User user) {
        String resetUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                .path("/reset-password")
                .queryParam("token", user.getPasswordResetToken())
                .build()
                .toUriString();

        sendEmail(user.getEmail(), "Reset your account password",
                "Use the secure link below to reset your password.",
                resetUrl);
    }

private void sendEmail(String to, String subject, String text, String ctaUrl) {
        try {
            queueService.publishEmailNotification(to, subject, text, ctaUrl);
        } catch (Exception ex) {
            logger.error("Failed to queue email for {}: {}", to, ex.getMessage(), ex);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }

    private static boolean isExpired(Instant expiresAt) {
        return expiresAt == null || expiresAt.isBefore(Instant.now());
    }

    private static ResponseEntity<Map<String, Object>> badRequest(String error) {
        return ResponseEntity.badRequest().body(Map.of("error", error));
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 128) String password) {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 128) String password) {
    }

    public record EmailRequest(@NotBlank @Email @Size(max = 255) String email) {
    }

    public record TokenRequest(String token) {
    }

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(max = 128) String password) {
    }
}
