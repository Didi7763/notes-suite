package com.notes.service;

import com.notes.dto.auth.AuthResponse;
import com.notes.dto.auth.LoginRequest;
import com.notes.dto.auth.RegisterRequest;
import com.notes.dto.auth.RefreshTokenRequest;
import com.notes.entity.RefreshToken;
import com.notes.exception.TokenRefreshException;
import com.notes.model.User;
import com.notes.repository.UserRepository;
import com.notes.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, String ipAddress, String userAgent) {
        logger.info("Attempting to register user with email: {}", registerRequest.getEmail());

        // Vérification si l'utilisateur existe déjà
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
            throw new RuntimeException("Erreur: L'email est déjà utilisé!");
        }

        // Création du nouvel utilisateur
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setIsActive(true);

        user = userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        // Génération des tokens
        return generateAuthResponse(user, ipAddress, userAgent);
    }

    public AuthResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        logger.info("Attempting to login user with email: {}", loginRequest.getEmail());

        try {
            // Authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Récupération de l'utilisateur
            User user = (User) authentication.getPrincipal();
            user.updateLastLogin();
            userRepository.save(user);

            logger.info("User logged in successfully: {}", user.getEmail());

            // Génération des tokens
            return generateAuthResponse(user, ipAddress, userAgent);

        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for email - {}", loginRequest.getEmail());
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (Exception e) {
            logger.error("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest, String ipAddress, String userAgent) {
        logger.info("Attempting to refresh token");

        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        try {
            // Vérification et rotation du refresh token
            RefreshToken refreshToken = refreshTokenService.rotateRefreshToken(requestRefreshToken, ipAddress, userAgent);
            User user = refreshToken.getUser();

            // Génération d'un nouveau access token
            String newAccessToken = jwtUtils.generateAccessTokenFromEmail(user.getEmail());

            logger.info("Token refreshed successfully for user: {}", user.getEmail());

            return new AuthResponse(
                    newAccessToken,
                    refreshToken.getToken(),
                    (long) jwtUtils.getAccessTokenExpirationSeconds(),
                    user.getId(),
                    user.getEmail(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusSeconds(jwtUtils.getAccessTokenExpirationSeconds())
            );

        } catch (TokenRefreshException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Refresh token invalide: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du rafraîchissement du token: " + e.getMessage());
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        logger.info("Attempting to logout user");

        try {
            if (refreshToken != null && !refreshToken.isEmpty()) {
                refreshTokenService.revokeToken(refreshToken, "User logout");
                logger.info("User logged out successfully");
            }
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    @Transactional
    public void logoutAllDevices(Long userId) {
        logger.info("Attempting to logout all devices for user: {}", userId);

        try {
            refreshTokenService.revokeAllUserTokens(userId, "Logout all devices");
            logger.info("All devices logged out successfully for user: {}", userId);
        } catch (Exception e) {
            logger.error("Logout all devices failed for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la déconnexion de tous les appareils: " + e.getMessage());
        }
    }

    private AuthResponse generateAuthResponse(User user, String ipAddress, String userAgent) {
        // Génération des tokens
        String accessToken = jwtUtils.generateAccessTokenFromEmail(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), ipAddress, userAgent);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                (long) jwtUtils.getAccessTokenExpirationSeconds(),
                user.getId(),
                user.getEmail(),
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(jwtUtils.getAccessTokenExpirationSeconds())
        );
    }

    // Méthodes utilitaires
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }
}