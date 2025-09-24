package com.notes.repository;

import com.notes.model.Note;
import com.notes.model.NoteVisibility;
import com.notes.model.Tag;
import com.notes.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Recherche par propriétaire
    Page<Note> findByOwner(User owner, Pageable pageable);
    List<Note> findByOwner(User owner);
    List<Note> findByOwnerAndVisibility(User owner, NoteVisibility visibility);

    // Recherche par visibilité
    List<Note> findByVisibility(NoteVisibility visibility);
    Page<Note> findByVisibility(NoteVisibility visibility, Pageable pageable);

    // Recherche par titre (LIKE insensible à la casse)
    @Query("SELECT n FROM Note n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Note> findByTitleContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT n FROM Note n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Note> findByTitleContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Recherche par propriétaire et titre
    @Query("SELECT n FROM Note n WHERE n.owner = :owner AND LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Note> findByOwnerAndTitleContainingIgnoreCase(@Param("owner") User owner, @Param("searchTerm") String searchTerm);

    @Query("SELECT n FROM Note n WHERE n.owner = :owner AND LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Note> findByOwnerAndTitleContainingIgnoreCase(@Param("owner") User owner, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Recherche dans le contenu
    @Query("SELECT n FROM Note n WHERE n.owner = :owner AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.contentMd) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Note> findByOwnerAndSearchTerm(@Param("owner") User owner, @Param("searchTerm") String searchTerm);

    @Query("SELECT n FROM Note n WHERE n.owner = :owner AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.contentMd) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Note> findByOwnerAndSearchTerm(@Param("owner") User owner, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Recherche dans les notes publiques
    @Query("SELECT n FROM Note n WHERE n.visibility = 'PUBLIC' AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.contentMd) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Note> findPublicNotesBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query("SELECT n FROM Note n WHERE n.visibility = 'PUBLIC' AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.contentMd) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Note> findPublicNotesBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Filtrage par tags
    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE nt.tag = :tag")
    List<Note> findByTag(@Param("tag") Tag tag);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE nt.tag = :tag")
    Page<Note> findByTag(@Param("tag") Tag tag, Pageable pageable);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE nt.tag.label = :tagLabel")
    List<Note> findByTagLabel(@Param("tagLabel") String tagLabel);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE nt.tag.label = :tagLabel")
    Page<Note> findByTagLabel(@Param("tagLabel") String tagLabel, Pageable pageable);

    // Filtrage par plusieurs tags
    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE nt.tag IN :tags")
    List<Note> findByTags(@Param("tags") List<Tag> tags);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE nt.tag IN :tags")
    Page<Note> findByTags(@Param("tags") List<Tag> tags, Pageable pageable);

    // Filtrage par propriétaire, tags et visibilité
    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE " +
           "n.owner = :owner AND nt.tag IN :tags AND n.visibility = :visibility")
    List<Note> findByOwnerAndTagsAndVisibility(@Param("owner") User owner, 
                                               @Param("tags") List<Tag> tags, 
                                               @Param("visibility") NoteVisibility visibility);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.noteTags nt WHERE " +
           "n.owner = :owner AND nt.tag IN :tags AND n.visibility = :visibility")
    Page<Note> findByOwnerAndTagsAndVisibility(@Param("owner") User owner, 
                                               @Param("tags") List<Tag> tags, 
                                               @Param("visibility") NoteVisibility visibility, 
                                               Pageable pageable);

    // Notes partagées avec un utilisateur
    @Query("SELECT n FROM Note n WHERE n.id IN " +
           "(SELECT s.note.id FROM Share s WHERE s.sharedWithUser.id = :userId AND s.isActive = true)")
    List<Note> findSharedWithUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Note n WHERE n.id IN " +
           "(SELECT s.note.id FROM Share s WHERE s.sharedWithUser.id = :userId AND s.isActive = true)")
    Page<Note> findSharedWithUser(@Param("userId") Long userId, Pageable pageable);

    // Notes accessibles par un utilisateur (propriétaire + partagées + publiques)
    @Query("SELECT n FROM Note n WHERE n.owner.id = :userId OR n.id IN " +
           "(SELECT s.note.id FROM Share s WHERE s.sharedWithUser.id = :userId AND s.isActive = true) OR " +
           "n.visibility = 'PUBLIC'")
    List<Note> findAccessibleByUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Note n WHERE n.owner.id = :userId OR n.id IN " +
           "(SELECT s.note.id FROM Share s WHERE s.sharedWithUser.id = :userId AND s.isActive = true) OR " +
           "n.visibility = 'PUBLIC'")
    Page<Note> findAccessibleByUser(@Param("userId") Long userId, Pageable pageable);

    // Tri par updated_at
    Page<Note> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<Note> findAllByOrderByUpdatedAtAsc(Pageable pageable);

    // Tri par created_at
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Note> findAllByOrderByCreatedAtAsc(Pageable pageable);

    // Tri par titre
    Page<Note> findAllByOrderByTitleAsc(Pageable pageable);
    Page<Note> findAllByOrderByTitleDesc(Pageable pageable);

    // Tri par nombre de vues
    Page<Note> findAllByOrderByViewCountDesc(Pageable pageable);

    // Notes favorites
    List<Note> findByOwnerAndIsFavoriteTrue(User owner);
    Page<Note> findByOwnerAndIsFavoriteTrue(User owner, Pageable pageable);

    // Notes récentes
    @Query("SELECT n FROM Note n WHERE n.owner = :owner AND n.updatedAt >= :date")
    List<Note> findRecentNotesByOwner(@Param("owner") User owner, @Param("date") LocalDateTime date);

    @Query("SELECT n FROM Note n WHERE n.owner = :owner AND n.updatedAt >= :date")
    Page<Note> findRecentNotesByOwner(@Param("owner") User owner, @Param("date") LocalDateTime date, Pageable pageable);

    // Recherche avancée avec filtres multiples
    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.noteTags nt WHERE " +
           "(:owner IS NULL OR n.owner = :owner) AND " +
           "(:visibility IS NULL OR n.visibility = :visibility) AND " +
           "(:tag IS NULL OR nt.tag = :tag) AND " +
           "(:searchTerm IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.contentMd) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:isFavorite IS NULL OR n.isFavorite = :isFavorite) AND " +
           "(:createdAfter IS NULL OR n.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR n.createdAt <= :createdBefore)")
    Page<Note> findNotesWithFilters(
            @Param("owner") User owner,
            @Param("visibility") NoteVisibility visibility,
            @Param("tag") Tag tag,
            @Param("searchTerm") String searchTerm,
            @Param("isFavorite") Boolean isFavorite,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);

    // Statistiques
    @Query("SELECT COUNT(n) FROM Note n WHERE n.owner = :owner")
    Long countByOwner(@Param("owner") User owner);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.visibility = :visibility")
    Long countByVisibility(@Param("visibility") NoteVisibility visibility);

    @Query("SELECT COUNT(DISTINCT n) FROM Note n JOIN n.noteTags nt WHERE nt.tag = :tag")
    Long countByTag(@Param("tag") Tag tag);

    // Notes les plus vues
    @Query("SELECT n FROM Note n WHERE n.visibility = 'PUBLIC' ORDER BY n.viewCount DESC")
    Page<Note> findMostViewedPublicNotes(Pageable pageable);

    // Notes récemment mises à jour
    @Query("SELECT n FROM Note n WHERE n.visibility = 'PUBLIC' AND n.updatedAt >= :date ORDER BY n.updatedAt DESC")
    Page<Note> findRecentlyUpdatedPublicNotes(@Param("date") LocalDateTime date, Pageable pageable);

    // Méthodes manquantes pour les services
    @Query("SELECT n FROM Note n JOIN n.noteTags nt JOIN nt.tag t WHERE n.owner.id = :ownerId AND t.label = :tagLabel")
    Page<Note> findByOwnerIdAndTagLabel(@Param("ownerId") Long ownerId, @Param("tagLabel") String tagLabel, Pageable pageable);

    Page<Note> findByOwnerAndVisibility(User owner, NoteVisibility visibility, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.owner.id = :ownerId AND n.isFavorite = true")
    Page<Note> findFavoriteNotesByOwner(@Param("ownerId") Long ownerId, Pageable pageable);
}