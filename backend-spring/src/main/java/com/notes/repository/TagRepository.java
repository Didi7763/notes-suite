package com.notes.repository;

import com.notes.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // Recherche par libellé exact
    Optional<Tag> findByLabel(String label);
    Boolean existsByLabel(String label);

    // Recherche par libellé avec LIKE insensible à la casse
    @Query("SELECT t FROM Tag t WHERE LOWER(t.label) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tag> findByLabelContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.label) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tag> findByLabelContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Recherche par description
    @Query("SELECT t FROM Tag t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tag> findByDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tag> findByDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Recherche globale (libellé ou description)
    @Query("SELECT t FROM Tag t WHERE " +
           "LOWER(t.label) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tag> findByLabelOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Tag t WHERE " +
           "LOWER(t.label) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tag> findByLabelOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Tags les plus utilisés
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findMostUsedTags();

    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    Page<Tag> findMostUsedTags(Pageable pageable);

    // Tags utilisés (usageCount > 0)
    @Query("SELECT t FROM Tag t WHERE t.usageCount > 0 ORDER BY t.usageCount DESC")
    List<Tag> findUsedTags();

    @Query("SELECT t FROM Tag t WHERE t.usageCount > 0 ORDER BY t.usageCount DESC")
    Page<Tag> findUsedTags(Pageable pageable);

    // Tags non utilisés (usageCount = 0)
    @Query("SELECT t FROM Tag t WHERE t.usageCount = 0")
    List<Tag> findUnusedTags();

    @Query("SELECT t FROM Tag t WHERE t.usageCount = 0")
    Page<Tag> findUnusedTags(Pageable pageable);

    // Tags avec un nombre minimum d'utilisations
    @Query("SELECT t FROM Tag t WHERE t.usageCount >= :minUsage ORDER BY t.usageCount DESC")
    List<Tag> findTagsWithMinUsage(@Param("minUsage") Long minUsage);

    @Query("SELECT t FROM Tag t WHERE t.usageCount >= :minUsage ORDER BY t.usageCount DESC")
    Page<Tag> findTagsWithMinUsage(@Param("minUsage") Long minUsage, Pageable pageable);

    // Tri par différents critères
    Page<Tag> findAllByOrderByLabelAsc(Pageable pageable);
    Page<Tag> findAllByOrderByLabelDesc(Pageable pageable);
    Page<Tag> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Tag> findAllByOrderByCreatedAtAsc(Pageable pageable);
    Page<Tag> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<Tag> findAllByOrderByUpdatedAtAsc(Pageable pageable);

    // Recherche par couleur
    @Query("SELECT t FROM Tag t WHERE t.color = :color")
    List<Tag> findByColor(@Param("color") String color);

    @Query("SELECT t FROM Tag t WHERE t.color = :color")
    Page<Tag> findByColor(@Param("color") String color, Pageable pageable);

    // Tags avec couleur
    @Query("SELECT t FROM Tag t WHERE t.color IS NOT NULL")
    List<Tag> findTagsWithColor();

    @Query("SELECT t FROM Tag t WHERE t.color IS NOT NULL")
    Page<Tag> findTagsWithColor(Pageable pageable);

    // Tags sans couleur
    @Query("SELECT t FROM Tag t WHERE t.color IS NULL")
    List<Tag> findTagsWithoutColor();

    @Query("SELECT t FROM Tag t WHERE t.color IS NULL")
    Page<Tag> findTagsWithoutColor(Pageable pageable);

    // Recherche avancée avec filtres multiples
    @Query("SELECT t FROM Tag t WHERE " +
           "(:label IS NULL OR LOWER(t.label) LIKE LOWER(CONCAT('%', :label, '%'))) AND " +
           "(:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:color IS NULL OR t.color = :color) AND " +
           "(:minUsage IS NULL OR t.usageCount >= :minUsage) AND " +
           "(:maxUsage IS NULL OR t.usageCount <= :maxUsage)")
    Page<Tag> findTagsWithFilters(
            @Param("label") String label,
            @Param("description") String description,
            @Param("color") String color,
            @Param("minUsage") Long minUsage,
            @Param("maxUsage") Long maxUsage,
            Pageable pageable);

    // Statistiques
    @Query("SELECT COUNT(t) FROM Tag t WHERE t.usageCount > 0")
    Long countUsedTags();

    @Query("SELECT COUNT(t) FROM Tag t WHERE t.usageCount = 0")
    Long countUnusedTags();

    @Query("SELECT AVG(t.usageCount) FROM Tag t WHERE t.usageCount > 0")
    Double getAverageUsageCount();

    @Query("SELECT MAX(t.usageCount) FROM Tag t")
    Long getMaxUsageCount();

    @Query("SELECT MIN(t.usageCount) FROM Tag t")
    Long getMinUsageCount();

    // Tags populaires (top N)
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTopNTags(@Param("limit") int limit);

    // Tags suggérés basés sur un terme de recherche
    @Query("SELECT t FROM Tag t WHERE LOWER(t.label) LIKE LOWER(CONCAT(:searchTerm, '%')) ORDER BY t.usageCount DESC")
    List<Tag> findSuggestedTags(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Tags similaires (même préfixe)
    @Query("SELECT t FROM Tag t WHERE t.label LIKE CONCAT(:prefix, '%') AND t.id != :excludeId ORDER BY t.usageCount DESC")
    List<Tag> findSimilarTags(@Param("prefix") String prefix, @Param("excludeId") Long excludeId, Pageable pageable);
}