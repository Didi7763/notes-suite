package com.notes.repository;

import com.notes.model.Note;
import com.notes.model.PublicLink;
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
public interface PublicLinkRepository extends JpaRepository<PublicLink, Long> {

    // Recherche par token URL
    Optional<PublicLink> findByUrlToken(String urlToken);
    Boolean existsByUrlToken(String urlToken);

    // Recherche par note
    List<PublicLink> findByNote(Note note);
    Page<PublicLink> findByNote(Note note, Pageable pageable);

    // Liens actifs
    List<PublicLink> findByIsActiveTrue();
    Page<PublicLink> findByIsActiveTrue(Pageable pageable);

    // Liens actifs par note
    List<PublicLink> findByNoteAndIsActiveTrue(Note note);
    Page<PublicLink> findByNoteAndIsActiveTrue(Note note, Pageable pageable);

    // Liens expirés
    @Query("SELECT pl FROM PublicLink pl WHERE pl.expiresAt IS NOT NULL AND pl.expiresAt < :now")
    List<PublicLink> findExpiredLinks(@Param("now") LocalDateTime now);

    @Query("SELECT pl FROM PublicLink pl WHERE pl.expiresAt IS NOT NULL AND pl.expiresAt < :now")
    Page<PublicLink> findExpiredLinks(@Param("now") LocalDateTime now, Pageable pageable);

    // Liens valides (actifs et non expirés)
    @Query("SELECT pl FROM PublicLink pl WHERE pl.isActive = true AND (pl.expiresAt IS NULL OR pl.expiresAt > :now)")
    List<PublicLink> findValidLinks(@Param("now") LocalDateTime now);

    @Query("SELECT pl FROM PublicLink pl WHERE pl.isActive = true AND (pl.expiresAt IS NULL OR pl.expiresAt > :now)")
    Page<PublicLink> findValidLinks(@Param("now") LocalDateTime now, Pageable pageable);

    // Liens valides par note
    @Query("SELECT pl FROM PublicLink pl WHERE pl.note = :note AND pl.isActive = true AND (pl.expiresAt IS NULL OR pl.expiresAt > :now)")
    List<PublicLink> findValidLinksByNote(@Param("note") Note note, @Param("now") LocalDateTime now);

    @Query("SELECT pl FROM PublicLink pl WHERE pl.note = :note AND pl.isActive = true AND (pl.expiresAt IS NULL OR pl.expiresAt > :now)")
    Page<PublicLink> findValidLinksByNote(@Param("note") Note note, @Param("now") LocalDateTime now, Pageable pageable);

    // Liens avec limite d'accès atteinte
    @Query("SELECT pl FROM PublicLink pl WHERE pl.maxAccessCount IS NOT NULL AND pl.accessCount >= pl.maxAccessCount")
    List<PublicLink> findLinksWithAccessLimitReached();

    @Query("SELECT pl FROM PublicLink pl WHERE pl.maxAccessCount IS NOT NULL AND pl.accessCount >= pl.maxAccessCount")
    Page<PublicLink> findLinksWithAccessLimitReached(Pageable pageable);

    // Liens protégés par mot de passe
    @Query("SELECT pl FROM PublicLink pl WHERE pl.passwordHash IS NOT NULL AND pl.passwordHash != ''")
    List<PublicLink> findPasswordProtectedLinks();

    @Query("SELECT pl FROM PublicLink pl WHERE pl.passwordHash IS NOT NULL AND pl.passwordHash != ''")
    Page<PublicLink> findPasswordProtectedLinks(Pageable pageable);

    // Liens sans mot de passe
    @Query("SELECT pl FROM PublicLink pl WHERE pl.passwordHash IS NULL OR pl.passwordHash = ''")
    List<PublicLink> findUnprotectedLinks();

    @Query("SELECT pl FROM PublicLink pl WHERE pl.passwordHash IS NULL OR pl.passwordHash = ''")
    Page<PublicLink> findUnprotectedLinks(Pageable pageable);

    // Liens créés récemment
    @Query("SELECT pl FROM PublicLink pl WHERE pl.createdAt >= :date")
    List<PublicLink> findLinksCreatedSince(@Param("date") LocalDateTime date);

    @Query("SELECT pl FROM PublicLink pl WHERE pl.createdAt >= :date")
    Page<PublicLink> findLinksCreatedSince(@Param("date") LocalDateTime date, Pageable pageable);

    // Liens expirant bientôt
    @Query("SELECT pl FROM PublicLink pl WHERE pl.expiresAt IS NOT NULL AND pl.expiresAt BETWEEN :now AND :future")
    List<PublicLink> findLinksExpiringSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future);

    @Query("SELECT pl FROM PublicLink pl WHERE pl.expiresAt IS NOT NULL AND pl.expiresAt BETWEEN :now AND :future")
    Page<PublicLink> findLinksExpiringSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future, Pageable pageable);

    // Liens les plus consultés
    @Query("SELECT pl FROM PublicLink pl ORDER BY pl.accessCount DESC")
    List<PublicLink> findMostAccessedLinks();

    @Query("SELECT pl FROM PublicLink pl ORDER BY pl.accessCount DESC")
    Page<PublicLink> findMostAccessedLinks(Pageable pageable);

    // Liens les plus consultés par note
    @Query("SELECT pl FROM PublicLink pl WHERE pl.note = :note ORDER BY pl.accessCount DESC")
    List<PublicLink> findMostAccessedLinksByNote(@Param("note") Note note);

    @Query("SELECT pl FROM PublicLink pl WHERE pl.note = :note ORDER BY pl.accessCount DESC")
    Page<PublicLink> findMostAccessedLinksByNote(@Param("note") Note note, Pageable pageable);

    // Recherche par description
    @Query("SELECT pl FROM PublicLink pl WHERE LOWER(pl.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PublicLink> findByDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT pl FROM PublicLink pl WHERE LOWER(pl.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<PublicLink> findByDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Recherche avancée avec filtres multiples
    @Query("SELECT pl FROM PublicLink pl WHERE " +
           "(:note IS NULL OR pl.note = :note) AND " +
           "(:isActive IS NULL OR pl.isActive = :isActive) AND " +
           "(:hasPassword IS NULL OR (pl.passwordHash IS NOT NULL AND pl.passwordHash != '') = :hasPassword) AND " +
           "(:hasAccessLimit IS NULL OR (pl.maxAccessCount IS NOT NULL) = :hasAccessLimit) AND " +
           "(:createdAfter IS NULL OR pl.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR pl.createdAt <= :createdBefore) AND " +
           "(:expiresAfter IS NULL OR pl.expiresAt >= :expiresAfter) AND " +
           "(:expiresBefore IS NULL OR pl.expiresAt <= :expiresBefore)")
    Page<PublicLink> findLinksWithFilters(
            @Param("note") Note note,
            @Param("isActive") Boolean isActive,
            @Param("hasPassword") Boolean hasPassword,
            @Param("hasAccessLimit") Boolean hasAccessLimit,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            @Param("expiresAfter") LocalDateTime expiresAfter,
            @Param("expiresBefore") LocalDateTime expiresBefore,
            Pageable pageable);

    // Tri par différents critères
    Page<PublicLink> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<PublicLink> findAllByOrderByCreatedAtAsc(Pageable pageable);
    Page<PublicLink> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<PublicLink> findAllByOrderByUpdatedAtAsc(Pageable pageable);
    Page<PublicLink> findAllByOrderByExpiresAtAsc(Pageable pageable);
    Page<PublicLink> findAllByOrderByExpiresAtDesc(Pageable pageable);
    Page<PublicLink> findAllByOrderByAccessCountDesc(Pageable pageable);
    Page<PublicLink> findAllByOrderByAccessCountAsc(Pageable pageable);

    // Statistiques
    @Query("SELECT COUNT(pl) FROM PublicLink pl WHERE pl.isActive = true")
    Long countActiveLinks();

    @Query("SELECT COUNT(pl) FROM PublicLink pl WHERE pl.note = :note AND pl.isActive = true")
    Long countActiveLinksByNote(@Param("note") Note note);

    @Query("SELECT COUNT(pl) FROM PublicLink pl WHERE pl.expiresAt IS NOT NULL AND pl.expiresAt < :now")
    Long countExpiredLinks(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(pl) FROM PublicLink pl WHERE pl.passwordHash IS NOT NULL AND pl.passwordHash != ''")
    Long countPasswordProtectedLinks();

    @Query("SELECT COUNT(pl) FROM PublicLink pl WHERE pl.maxAccessCount IS NOT NULL")
    Long countLinksWithAccessLimit();

    @Query("SELECT SUM(pl.accessCount) FROM PublicLink pl WHERE pl.isActive = true")
    Long getTotalAccessCount();

    @Query("SELECT AVG(pl.accessCount) FROM PublicLink pl WHERE pl.isActive = true")
    Double getAverageAccessCount();

    @Query("SELECT MAX(pl.accessCount) FROM PublicLink pl")
    Long getMaxAccessCount();

    // Liens valides par token (pour l'accès public)
    @Query("SELECT pl FROM PublicLink pl WHERE pl.urlToken = :token AND pl.isActive = true AND (pl.expiresAt IS NULL OR pl.expiresAt > :now) AND (pl.maxAccessCount IS NULL OR pl.accessCount < pl.maxAccessCount)")
    Optional<PublicLink> findValidLinkByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Suppression des liens expirés
    @Query("DELETE FROM PublicLink pl WHERE pl.expiresAt IS NOT NULL AND pl.expiresAt < :now")
    int deleteExpiredLinks(@Param("now") LocalDateTime now);

    // Suppression des liens inactifs
    @Query("DELETE FROM PublicLink pl WHERE pl.isActive = false")
    int deleteInactiveLinks();

    // Suppression des liens avec limite d'accès atteinte
    @Query("DELETE FROM PublicLink pl WHERE pl.maxAccessCount IS NOT NULL AND pl.accessCount >= pl.maxAccessCount")
    int deleteLinksWithAccessLimitReached();

    // Méthodes manquantes pour les services
    @Query("SELECT pl FROM PublicLink pl WHERE pl.note.id = :noteId")
    List<PublicLink> findByNoteId(@Param("noteId") Long noteId);

    @Query("SELECT pl FROM PublicLink pl WHERE (pl.expiresAt IS NULL OR pl.expiresAt > CURRENT_TIMESTAMP) AND (pl.maxAccessCount IS NULL OR pl.accessCount < pl.maxAccessCount)")
    List<PublicLink> findActivePublicLinks();

    @Query("SELECT pl FROM PublicLink pl WHERE pl.passwordHash IS NOT NULL AND pl.passwordHash != ''")
    List<PublicLink> findPasswordProtectedPublicLinks();

    @Query("SELECT pl FROM PublicLink pl WHERE pl.maxAccessCount IS NOT NULL AND pl.accessCount < pl.maxAccessCount")
    List<PublicLink> findPublicLinksWithRemainingAccess();

}