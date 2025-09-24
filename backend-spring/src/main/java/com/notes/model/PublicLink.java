package com.notes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_links",
       indexes = {
           @Index(name = "idx_public_link_note", columnList = "note_id"),
           @Index(name = "idx_public_link_token", columnList = "url_token", unique = true),
           @Index(name = "idx_public_link_expires", columnList = "expires_at"),
           @Index(name = "idx_public_link_created", columnList = "created_at")
       })
public class PublicLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La note est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false, foreignKey = @ForeignKey(name = "fk_public_link_note"))
    private Note note;

    @NotBlank(message = "Le token URL est obligatoire")
    @Size(max = 255, message = "Le token URL ne peut pas dépasser 255 caractères")
    @Column(name = "url_token", unique = true, nullable = false, length = 255)
    private String urlToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;

    @Column(name = "max_access_count")
    private Long maxAccessCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "description", length = 500)
    private String description;

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
    public PublicLink() {}

    public PublicLink(Note note, String urlToken) {
        this.note = note;
        this.urlToken = urlToken;
    }

    public PublicLink(Note note, String urlToken, LocalDateTime expiresAt) {
        this.note = note;
        this.urlToken = urlToken;
        this.expiresAt = expiresAt;
    }

    public PublicLink(Note note, String urlToken, LocalDateTime expiresAt, Long maxAccessCount) {
        this.note = note;
        this.urlToken = urlToken;
        this.expiresAt = expiresAt;
        this.maxAccessCount = maxAccessCount;
    }

    // Business methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isAccessLimitReached() {
        return maxAccessCount != null && accessCount >= maxAccessCount;
    }

    public boolean isValid() {
        return isActive && !isExpired() && !isAccessLimitReached();
    }

    public void incrementAccessCount() {
        this.accessCount++;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isEmpty();
    }

    public void setPassword(String password) {
        // Note: In a real application, you would hash the password here
        this.passwordHash = password;
    }

    public boolean isPasswordProtected() {
        return hasPassword();
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

    public String getUrlToken() { 
        return urlToken; 
    }
    
    public void setUrlToken(String urlToken) { 
        this.urlToken = urlToken; 
    }

    public LocalDateTime getExpiresAt() { 
        return expiresAt; 
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) { 
        this.expiresAt = expiresAt; 
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

    public Long getAccessCount() { 
        return accessCount; 
    }
    
    public void setAccessCount(Long accessCount) { 
        this.accessCount = accessCount; 
    }

    public Long getMaxAccessCount() { 
        return maxAccessCount; 
    }
    
    public void setMaxAccessCount(Long maxAccessCount) { 
        this.maxAccessCount = maxAccessCount; 
    }

    public Boolean getIsActive() { 
        return isActive; 
    }
    
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
    }

    public String getPasswordHash() { 
        return passwordHash; 
    }
    
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }

    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    @Override
    public String toString() {
        return "PublicLink{" +
                "id=" + id +
                ", urlToken='" + urlToken + '\'' +
                ", expiresAt=" + expiresAt +
                ", accessCount=" + accessCount +
                ", isActive=" + isActive +
                '}';
    }
}