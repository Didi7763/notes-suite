package com.notes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "shares",
       indexes = {
           @Index(name = "idx_share_note", columnList = "note_id"),
           @Index(name = "idx_share_user", columnList = "shared_with_user_id"),
           @Index(name = "idx_share_permission", columnList = "permission"),
           @Index(name = "idx_share_created_at", columnList = "created_at"),
           @Index(name = "idx_share_note_user", columnList = "note_id, shared_with_user_id", unique = true)
       })
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La note est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false, foreignKey = @ForeignKey(name = "fk_share_note"))
    private Note note;

    @NotNull(message = "L'utilisateur avec qui partager est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_share_user"))
    private User sharedWithUser;

    @NotNull(message = "La permission est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SharePermission permission;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "shared_by_user_id", nullable = false)
    private Long sharedByUserId;

    // Callbacks JPA
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Share() {}

    public Share(Note note, User sharedWithUser, SharePermission permission) {
        this.note = note;
        this.sharedWithUser = sharedWithUser;
        this.permission = permission;
        this.sharedByUserId = note.getOwner().getId();
    }

    public Share(Note note, User sharedWithUser, SharePermission permission, LocalDateTime expiresAt) {
        this.note = note;
        this.sharedWithUser = sharedWithUser;
        this.permission = permission;
        this.expiresAt = expiresAt;
        this.sharedByUserId = note.getOwner().getId();
    }

    // Business methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isValid() {
        return isActive && !isExpired();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean canRead() {
        return isValid() && (permission == SharePermission.READ || 
                           permission == SharePermission.WRITE || 
                           permission == SharePermission.ADMIN);
    }

    public boolean canWrite() {
        return isValid() && (permission == SharePermission.WRITE || 
                           permission == SharePermission.ADMIN);
    }

    public boolean canAdmin() {
        return isValid() && permission == SharePermission.ADMIN;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public Note getNote() { 
        return note; 
    }
    
    public void setNote(Note note) { 
        this.note = note; 
    }

    public User getSharedWithUser() { 
        return sharedWithUser; 
    }
    
    public void setSharedWithUser(User sharedWithUser) { 
        this.sharedWithUser = sharedWithUser; 
    }

    public SharePermission getPermission() { 
        return permission; 
    }
    
    public void setPermission(SharePermission permission) { 
        this.permission = permission; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    public LocalDateTime getExpiresAt() { 
        return expiresAt; 
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) { 
        this.expiresAt = expiresAt; 
    }

    public Boolean getIsActive() { 
        return isActive; 
    }
    
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
    }

    public Long getSharedByUserId() { 
        return sharedByUserId; 
    }
    
    public void setSharedByUserId(Long sharedByUserId) { 
        this.sharedByUserId = sharedByUserId; 
    }

    @Override
    public String toString() {
        return "Share{" +
                "id=" + id +
                ", permission=" + permission +
                ", sharedWithUser=" + (sharedWithUser != null ? sharedWithUser.getEmail() : null) +
                ", isActive=" + isActive +
                ", expiresAt=" + expiresAt +
                '}';
    }
}