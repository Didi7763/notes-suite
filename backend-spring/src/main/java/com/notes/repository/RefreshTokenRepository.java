package com.notes.repository;

import com.notes.entity.RefreshToken;
import com.notes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Recherche par token
    Optional<RefreshToken> findByToken(String token);
    Boolean existsByToken(String token);

    // Recherche par utilisateur
    List<RefreshToken> findByUser(User user);
    List<RefreshToken> findByUserAndRevokedFalse(User user);

    // Tokens valides (non révoqués et non expirés)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Tokens valides par utilisateur
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // Tokens expirés
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    // Tokens révoqués
    List<RefreshToken> findByRevokedTrue();
    List<RefreshToken> findByUserAndRevokedTrue(User user);

    // Tokens remplacés
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.replacedByToken IS NOT NULL")
    List<RefreshToken> findReplacedTokens();

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.replacedByToken IS NOT NULL")
    List<RefreshToken> findReplacedTokensByUser(@Param("user") User user);

    // Tokens créés récemment
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.createdAt >= :date")
    List<RefreshToken> findTokensCreatedSince(@Param("date") LocalDateTime date);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.createdAt >= :date")
    List<RefreshToken> findTokensCreatedSinceByUser(@Param("user") User user, @Param("date") LocalDateTime date);

    // Tokens expirant bientôt
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt BETWEEN :now AND :future AND rt.revoked = false")
    List<RefreshToken> findTokensExpiringSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future);

    // Recherche par adresse IP et User Agent
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.ipAddress = :ipAddress AND rt.userAgent = :userAgent AND rt.revoked = false")
    List<RefreshToken> findActiveTokensByUserAndDevice(@Param("user") User user, @Param("ipAddress") String ipAddress, @Param("userAgent") String userAgent);

    // Suppression des tokens expirés
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    // Suppression des tokens révoqués
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
    int deleteRevokedTokens();

    // Suppression des tokens d'un utilisateur
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(@Param("user") User user);

    // Suppression des tokens révoqués d'un utilisateur
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = true")
    int deleteRevokedTokensByUser(@Param("user") User user);

    // Révocation de tous les tokens d'un utilisateur
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now, rt.reasonRevoked = :reason WHERE rt.user = :user AND rt.revoked = false")
    int revokeAllUserTokens(@Param("user") User user, @Param("now") LocalDateTime now, @Param("reason") String reason);

    // Révocation d'un token spécifique
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now, rt.reasonRevoked = :reason WHERE rt.token = :token")
    int revokeToken(@Param("token") String token, @Param("now") LocalDateTime now, @Param("reason") String reason);

    // Révocation d'un token avec remplacement
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now, rt.reasonRevoked = :reason, rt.replacedByToken = :replacedByToken WHERE rt.token = :token")
    int revokeTokenWithReplacement(@Param("token") String token, @Param("now") LocalDateTime now, @Param("reason") String reason, @Param("replacedByToken") String replacedByToken);

    // Statistiques
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.revoked = false AND rt.expiresAt > :now")
    Long countValidTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    Long countValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.revoked = true")
    Long countRevokedTokens();

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt < :now")
    Long countExpiredTokens(@Param("now") LocalDateTime now);

    // Tokens les plus récents par utilisateur
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user ORDER BY rt.createdAt DESC")
    List<RefreshToken> findRecentTokensByUser(@Param("user") User user);

    // Nettoyage des anciens tokens (plus de X jours)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.createdAt < :cutoffDate")
    int deleteOldTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
}