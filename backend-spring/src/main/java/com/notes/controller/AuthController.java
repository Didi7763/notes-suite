package com.notes.controller;

import com.notes.dto.auth.AuthResponse;
import com.notes.dto.auth.LoginRequest;
import com.notes.dto.auth.RegisterRequest;
import com.notes.dto.auth.RefreshTokenRequest;
import com.notes.service.AuthService;
import com.notes.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, 
                                     HttpServletRequest request) {
        try {
            logger.info("Registration request for email: {}", registerRequest.getEmail());
            
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            AuthResponse authResponse = authService.register(registerRequest, ipAddress, userAgent);
            
            logger.info("Registration successful for email: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Registration failed for email {}: {}", registerRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors de l'inscription", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during registration for email {}: {}", registerRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, 
                                  HttpServletRequest request) {
        try {
            logger.info("Login request for email: {}", loginRequest.getEmail());
            
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            AuthResponse authResponse = authService.login(loginRequest, ipAddress, userAgent);
            
            logger.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Échec de la connexion", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during login for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
                                         HttpServletRequest request) {
        try {
            logger.info("Token refresh request");
            
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest, ipAddress, userAgent);
            
            logger.info("Token refresh successful");
            return ResponseEntity.ok(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Échec du rafraîchissement du token", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
                                   HttpServletRequest request) {
        try {
            logger.info("Logout request");
            
            String refreshToken = refreshTokenRequest != null ? refreshTokenRequest.getRefreshToken() : null;
            authService.logout(refreshToken);
            
            logger.info("Logout successful");
            return ResponseEntity.ok(createSuccessResponse("Déconnexion réussie"));
            
        } catch (RuntimeException e) {
            logger.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Échec de la déconnexion", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutAllDevices(HttpServletRequest request) {
        try {
            logger.info("Logout all devices request");
            
            Long userId = authService.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Non autorisé", "Utilisateur non authentifié"));
            }
            
            authService.logoutAllDevices(userId);
            
            logger.info("Logout all devices successful for user: {}", userId);
            return ResponseEntity.ok(createSuccessResponse("Déconnexion de tous les appareils réussie"));
            
        } catch (RuntimeException e) {
            logger.error("Logout all devices failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Échec de la déconnexion", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during logout all devices: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            logger.info("Get current user request");
            
            var currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Non autorisé", "Utilisateur non authentifié"));
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", currentUser.getId());
            userInfo.put("email", currentUser.getEmail());
            userInfo.put("createdAt", currentUser.getCreatedAt());
            userInfo.put("lastLoginAt", currentUser.getLastLoginAt());
            userInfo.put("isActive", currentUser.getIsActive());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            logger.error("Unexpected error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        try {
            logger.info("Check email availability request for: {}", email);
            
            boolean isAvailable = authService.isEmailAvailable(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("available", isAvailable);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error checking email availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    // Méthodes utilitaires
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private Map<String, Object> createErrorResponse(String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("details", details);
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}