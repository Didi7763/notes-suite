package com.notes.repository;

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
public interface UserRepository extends JpaRepository<User, Long> {

    // Recherche par email
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    // Recherche par email avec LIKE insensible à la casse
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // Recherche paginée par email
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findByEmailContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Utilisateurs actifs
    List<User> findByIsActiveTrue();
    Page<User> findByIsActiveTrue(Pageable pageable);

    // Utilisateurs créés après une date
    List<User> findByCreatedAtAfter(LocalDateTime date);
    Page<User> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    // Utilisateurs avec dernière connexion
    List<User> findByLastLoginAtIsNotNull();
    List<User> findByLastLoginAtAfter(LocalDateTime date);

    // Utilisateurs inactifs depuis longtemps
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);

    // Utilisateurs avec accès à une note spécifique
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT s.sharedWithUser.id FROM Share s WHERE s.note.id = :noteId AND s.isActive = true)")
    List<User> findUsersWithAccessToNote(@Param("noteId") Long noteId);

    // Statistiques
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    Long countUsersCreatedSince(@Param("date") LocalDateTime date);

    // Recherche avancée
    @Query("SELECT u FROM User u WHERE " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:createdAfter IS NULL OR u.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR u.createdAt <= :createdBefore)")
    Page<User> findUsersWithFilters(
            @Param("email") String email,
            @Param("isActive") Boolean isActive,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);

    // Tri par différents critères
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<User> findAllByOrderByLastLoginAtDesc(Pageable pageable);
    Page<User> findAllByOrderByEmailAsc(Pageable pageable);
}