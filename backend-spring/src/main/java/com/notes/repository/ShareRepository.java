package com.notes.repository;

import com.notes.model.Note;
import com.notes.model.Share;
import com.notes.model.SharePermission;
import com.notes.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {

    // Recherche par note
    List<Share> findByNote(Note note);
    Page<Share> findByNote(Note note, Pageable pageable);

    // Recherche par utilisateur avec qui partager
    List<Share> findBySharedWithUser(User sharedWithUser);
    Page<Share> findBySharedWithUser(User sharedWithUser, Pageable pageable);

    // Recherche par permission
    List<Share> findByPermission(SharePermission permission);
    Page<Share> findByPermission(SharePermission permission, Pageable pageable);

    // Recherche par note et utilisateur
    Optional<Share> findByNoteAndSharedWithUser(Note note, User sharedWithUser);

    // Recherche par note et utilisateur avec permission
    Optional<Share> findByNoteAndSharedWithUserAndPermission(Note note, User sharedWithUser, SharePermission permission);

    // Partages actifs
    List<Share> findByIsActiveTrue();
    Page<Share> findByIsActiveTrue(Pageable pageable);

    // Partages actifs par note
    List<Share> findByNoteAndIsActiveTrue(Note note);
    Page<Share> findByNoteAndIsActiveTrue(Note note, Pageable pageable);

    // Partages actifs par utilisateur
    List<Share> findBySharedWithUserAndIsActiveTrue(User sharedWithUser);
    Page<Share> findBySharedWithUserAndIsActiveTrue(User sharedWithUser, Pageable pageable);

    // Partages expirés
    @Query("SELECT s FROM Share s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    List<Share> findExpiredShares(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Share s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    Page<Share> findExpiredShares(@Param("now") LocalDateTime now, Pageable pageable);

    // Partages valides (actifs et non expirés)
    @Query("SELECT s FROM Share s WHERE s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<Share> findValidShares(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Share s WHERE s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Page<Share> findValidShares(@Param("now") LocalDateTime now, Pageable pageable);

    // Partages valides par note
    @Query("SELECT s FROM Share s WHERE s.note = :note AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<Share> findValidSharesByNote(@Param("note") Note note, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Share s WHERE s.note = :note AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Page<Share> findValidSharesByNote(@Param("note") Note note, @Param("now") LocalDateTime now, Pageable pageable);

    // Partages valides par utilisateur
    @Query("SELECT s FROM Share s WHERE s.sharedWithUser = :user AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<Share> findValidSharesByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Share s WHERE s.sharedWithUser = :user AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Page<Share> findValidSharesByUser(@Param("user") User user, @Param("now") LocalDateTime now, Pageable pageable);

    // Partages par permission et statut
    @Query("SELECT s FROM Share s WHERE s.permission = :permission AND s.isActive = :isActive")
    List<Share> findByPermissionAndIsActive(@Param("permission") SharePermission permission, @Param("isActive") Boolean isActive);

    @Query("SELECT s FROM Share s WHERE s.permission = :permission AND s.isActive = :isActive")
    Page<Share> findByPermissionAndIsActive(@Param("permission") SharePermission permission, @Param("isActive") Boolean isActive, Pageable pageable);

    // Partages créés par un utilisateur
    List<Share> findBySharedByUserId(Long sharedByUserId);
    Page<Share> findBySharedByUserId(Long sharedByUserId, Pageable pageable);

    // Partages créés récemment
    @Query("SELECT s FROM Share s WHERE s.createdAt >= :date")
    List<Share> findSharesCreatedSince(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Share s WHERE s.createdAt >= :date")
    Page<Share> findSharesCreatedSince(@Param("date") LocalDateTime date, Pageable pageable);

    // Partages expirant bientôt
    @Query("SELECT s FROM Share s WHERE s.expiresAt IS NOT NULL AND s.expiresAt BETWEEN :now AND :future")
    List<Share> findSharesExpiringSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future);

    @Query("SELECT s FROM Share s WHERE s.expiresAt IS NOT NULL AND s.expiresAt BETWEEN :now AND :future")
    Page<Share> findSharesExpiringSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future, Pageable pageable);

    // Recherche avancée avec filtres multiples
    @Query("SELECT s FROM Share s WHERE " +
           "(:note IS NULL OR s.note = :note) AND " +
           "(:sharedWithUser IS NULL OR s.sharedWithUser = :sharedWithUser) AND " +
           "(:permission IS NULL OR s.permission = :permission) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:sharedByUserId IS NULL OR s.sharedByUserId = :sharedByUserId) AND " +
           "(:createdAfter IS NULL OR s.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR s.createdAt <= :createdBefore)")
    Page<Share> findSharesWithFilters(
            @Param("note") Note note,
            @Param("sharedWithUser") User sharedWithUser,
            @Param("permission") SharePermission permission,
            @Param("isActive") Boolean isActive,
            @Param("sharedByUserId") Long sharedByUserId,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);

    // Tri par différents critères
    Page<Share> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Share> findAllByOrderByCreatedAtAsc(Pageable pageable);
    Page<Share> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<Share> findAllByOrderByUpdatedAtAsc(Pageable pageable);
    Page<Share> findAllByOrderByExpiresAtAsc(Pageable pageable);
    Page<Share> findAllByOrderByExpiresAtDesc(Pageable pageable);

    // Statistiques
    @Query("SELECT COUNT(s) FROM Share s WHERE s.isActive = true")
    Long countActiveShares();

    @Query("SELECT COUNT(s) FROM Share s WHERE s.note = :note AND s.isActive = true")
    Long countActiveSharesByNote(@Param("note") Note note);

    @Query("SELECT COUNT(s) FROM Share s WHERE s.sharedWithUser = :user AND s.isActive = true")
    Long countActiveSharesByUser(@Param("user") User user);

    @Query("SELECT COUNT(s) FROM Share s WHERE s.permission = :permission AND s.isActive = true")
    Long countActiveSharesByPermission(@Param("permission") SharePermission permission);

    @Query("SELECT COUNT(s) FROM Share s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    Long countExpiredShares(@Param("now") LocalDateTime now);

    // Utilisateurs avec accès à une note
    @Query("SELECT s.sharedWithUser FROM Share s WHERE s.note = :note AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<User> findUsersWithAccessToNote(@Param("note") Note note, @Param("now") LocalDateTime now);

    // Notes partagées avec un utilisateur
    @Query("SELECT s.note FROM Share s WHERE s.sharedWithUser = :user AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<Note> findNotesSharedWithUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT s.note FROM Share s WHERE s.sharedWithUser = :user AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Page<Note> findNotesSharedWithUser(@Param("user") User user, @Param("now") LocalDateTime now, Pageable pageable);

    // Suppression des partages expirés
    @Query("DELETE FROM Share s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    int deleteExpiredShares(@Param("now") LocalDateTime now);

    // Suppression des partages inactifs
    @Query("DELETE FROM Share s WHERE s.isActive = false")
    int deleteInactiveShares();

    // Méthodes pour les services
    @Query("SELECT s FROM Share s WHERE s.note.id = :noteId")
    List<Share> findByNoteId(@Param("noteId") Long noteId);

    @Query("SELECT s FROM Share s WHERE s.sharedWithUser.id = :userId")
    Page<Share> findBySharedWithUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Share s WHERE s.note.id = :noteId AND s.sharedWithUser.id = :userId")
    Optional<Share> findByNoteIdAndSharedWithUserId(@Param("noteId") Long noteId, @Param("userId") Long userId);

    @Query("SELECT s FROM Share s WHERE s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP)")
    List<Share> findActiveShares();

    @Query("SELECT s FROM Share s WHERE s.expiresAt < CURRENT_TIMESTAMP")
    List<Share> findExpiredShares();

    @Query("SELECT s FROM Share s WHERE s.permission = :permission AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP)")
    List<Share> findActiveSharesByPermission(@Param("permission") SharePermission permission);

}