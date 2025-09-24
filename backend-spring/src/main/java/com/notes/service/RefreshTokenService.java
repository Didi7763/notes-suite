package com.notes.service;

import com.notes.entity.RefreshToken;
import com.notes.exception.TokenRefreshException;
import com.notes.model.User;
import com.notes.repository.RefreshTokenRepository;
import com.notes.repository.UserRepository;
import com.notes.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, 
                              UserRepository userRepository, 
                              JwtUtils jwtUtils) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    // Création d'un nouveau refresh token
    @Transactional
    public RefreshToken createRefreshToken(Long userId, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Révocation des anciens tokens de l'utilisateur (rotation)
        revokeAllUserTokens(user, "Token rotation");

        // Génération d'un nouveau token
        String token = generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshTokenDurationMs * 1_000_000);

        RefreshToken refreshToken = new RefreshToken(user, token, expiresAt, ipAddress, userAgent);
        refreshToken = refreshTokenRepository.save(refreshToken);

        logger.info("Created new refresh token for user: {}", user.getEmail());
        return refreshToken;
    }

    // Création d'un refresh token sans informations de device
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        return createRefreshToken(userId, null, null);
    }

    // Vérification et rotation du refresh token
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        if (token.isRevoked()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token was revoked. Please make a new signin request");
        }

        return token;
    }

    // Rotation du refresh token
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken, String ipAddress, String userAgent) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new TokenRefreshException(oldToken, "Refresh token not found"));

        // Vérification de l'expiration
        verifyExpiration(oldRefreshToken);

        // Révocation de l'ancien token avec rotation
        oldRefreshToken.revoke("Token rotation", "replaced_by_rotation");
        refreshTokenRepository.save(oldRefreshToken);

        // Création d'un nouveau token
        RefreshToken newRefreshToken = createRefreshToken(
                oldRefreshToken.getUser().getId(), 
                ipAddress, 
                userAgent
        );

        logger.info("Rotated refresh token for user: {}", oldRefreshToken.getUser().getEmail());
        return newRefreshToken;
    }

    // Recherche d'un token par valeur
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Recherche d'un token valide
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidToken(token, LocalDateTime.now());
    }

    // Révocation d'un token spécifique
    @Transactional
    public void revokeToken(String token, String reason) {
        refreshTokenRepository.revokeToken(token, LocalDateTime.now(), reason);
        logger.info("Revoked refresh token: {}", token);
    }

    // Révocation de tous les tokens d'un utilisateur
    @Transactional
    public void revokeAllUserTokens(User user, String reason) {
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now(), reason);
        logger.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    // Révocation de tous les tokens d'un utilisateur par ID
    @Transactional
    public void revokeAllUserTokens(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        revokeAllUserTokens(user, reason);
    }

    // Suppression des tokens expirés
    @Transactional
    public int deleteExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deletedCount > 0) {
            logger.info("Deleted {} expired refresh tokens", deletedCount);
        }
        return deletedCount;
    }

    // Suppression des tokens révoqués
    @Transactional
    public int deleteRevokedTokens() {
        int deletedCount = refreshTokenRepository.deleteRevokedTokens();
        if (deletedCount > 0) {
            logger.info("Deleted {} revoked refresh tokens", deletedCount);
        }
        return deletedCount;
    }

    // Nettoyage des anciens tokens
    @Transactional
    public int deleteOldTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = refreshTokenRepository.deleteOldTokens(cutoffDate);
        if (deletedCount > 0) {
            logger.info("Deleted {} old refresh tokens (older than {} days)", deletedCount, daysOld);
        }
        return deletedCount;
    }

    // Recherche des tokens valides d'un utilisateur
    public List<RefreshToken> findValidTokensByUser(User user) {
        return refreshTokenRepository.findValidTokensByUser(user, LocalDateTime.now());
    }

    // Recherche des tokens valides d'un utilisateur par ID
    public List<RefreshToken> findValidTokensByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return findValidTokensByUser(user);
    }

    // Recherche des tokens par device
    public List<RefreshToken> findActiveTokensByDevice(User user, String ipAddress, String userAgent) {
        return refreshTokenRepository.findActiveTokensByUserAndDevice(user, ipAddress, userAgent);
    }

    // Statistiques
    public Long countValidTokens() {
        return refreshTokenRepository.countValidTokens(LocalDateTime.now());
    }

    public Long countValidTokensByUser(User user) {
        return refreshTokenRepository.countValidTokensByUser(user, LocalDateTime.now());
    }

    public Long countRevokedTokens() {
        return refreshTokenRepository.countRevokedTokens();
    }

    public Long countExpiredTokens() {
        return refreshTokenRepository.countExpiredTokens(LocalDateTime.now());
    }

    // Génération d'un token aléatoire
    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // Vérification de la sécurité du token
    public boolean isTokenSecure(RefreshToken token, String currentIpAddress, String currentUserAgent) {
        // Vérification de l'IP (optionnel, peut être désactivé pour les utilisateurs mobiles)
        if (token.getIpAddress() != null && currentIpAddress != null) {
            if (!token.getIpAddress().equals(currentIpAddress)) {
                logger.warn("IP address mismatch for refresh token: {} vs {}", 
                           token.getIpAddress(), currentIpAddress);
                // Pour l'instant, on ne bloque pas sur l'IP différente
            }
        }

        // Vérification du User Agent
        if (token.getUserAgent() != null && currentUserAgent != null) {
            if (!token.getUserAgent().equals(currentUserAgent)) {
                logger.warn("User agent mismatch for refresh token: {} vs {}", 
                           token.getUserAgent(), currentUserAgent);
                // Pour l'instant, on ne bloque pas sur le User Agent différent
            }
        }

        return true;
    }

    // Nettoyage périodique (à appeler via un scheduler)
    @Transactional
    public void performCleanup() {
        logger.info("Starting refresh token cleanup...");
        
        int expiredDeleted = deleteExpiredTokens();
        int revokedDeleted = deleteRevokedTokens();
        int oldDeleted = deleteOldTokens(30); // Supprimer les tokens de plus de 30 jours
        
        logger.info("Refresh token cleanup completed: {} expired, {} revoked, {} old tokens deleted", 
                   expiredDeleted, revokedDeleted, oldDeleted);
    }
}