package com.notes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes",
       indexes = {
           @Index(name = "idx_note_owner", columnList = "owner_id"),
           @Index(name = "idx_note_visibility", columnList = "visibility"),
           @Index(name = "idx_note_created_at", columnList = "created_at"),
           @Index(name = "idx_note_updated_at", columnList = "updated_at"),
           @Index(name = "idx_note_title", columnList = "title"),
           @Index(name = "idx_note_owner_visibility", columnList = "owner_id, visibility")
       })
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le propriétaire est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_note_owner"))
    private User owner;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "content_md", columnDefinition = "TEXT")
    private String contentMd;

    @NotNull(message = "La visibilité est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteVisibility visibility = NoteVisibility.PRIVATE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    // Relations
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoteTag> noteTags = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Share> shares = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PublicLink> publicLinks = new ArrayList<>();

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
    public Note() {}

    public Note(String title, String contentMd, User owner) {
        this.title = title;
        this.contentMd = contentMd;
        this.owner = owner;
    }

    public Note(String title, String contentMd, User owner, NoteVisibility visibility) {
        this.title = title;
        this.contentMd = contentMd;
        this.owner = owner;
        this.visibility = visibility;
    }

    // Business methods
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }

    public void addTag(Tag tag) {
        NoteTag noteTag = new NoteTag(this, tag);
        this.noteTags.add(noteTag);
    }

    public void removeTag(Tag tag) {
        this.noteTags.removeIf(nt -> nt.getTag().equals(tag));
    }

    public boolean hasTag(Tag tag) {
        return this.noteTags.stream()
                .anyMatch(nt -> nt.getTag().equals(tag));
    }

    public List<Tag> getTags() {
        return this.noteTags.stream()
                .map(NoteTag::getTag)
                .toList();
    }

    public boolean isAccessibleBy(User user) {
        if (this.owner.equals(user)) {
            return true;
        }
        if (this.visibility == NoteVisibility.PUBLIC) {
            return true;
        }
        if (this.visibility == NoteVisibility.SHARED) {
            return this.shares.stream()
                    .anyMatch(share -> share.getSharedWithUser().equals(user));
        }
        return false;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public User getOwner() { 
        return owner; 
    }
    
    public void setOwner(User owner) { 
        this.owner = owner; 
    }

    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getContentMd() { 
        return contentMd; 
    }
    
    public void setContentMd(String contentMd) { 
        this.contentMd = contentMd; 
    }

    public NoteVisibility getVisibility() { 
        return visibility; 
    }
    
    public void setVisibility(NoteVisibility visibility) { 
        this.visibility = visibility; 
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

    public Long getViewCount() { 
        return viewCount; 
    }
    
    public void setViewCount(Long viewCount) { 
        this.viewCount = viewCount; 
    }

    public Boolean getIsFavorite() { 
        return isFavorite; 
    }
    
    public void setIsFavorite(Boolean isFavorite) { 
        this.isFavorite = isFavorite; 
    }

    public List<NoteTag> getNoteTags() { 
        return noteTags; 
    }
    
    public void setNoteTags(List<NoteTag> noteTags) { 
        this.noteTags = noteTags; 
    }

    public List<Share> getShares() { 
        return shares; 
    }
    
    public void setShares(List<Share> shares) { 
        this.shares = shares; 
    }

    public List<PublicLink> getPublicLinks() { 
        return publicLinks; 
    }
    
    public void setPublicLinks(List<PublicLink> publicLinks) { 
        this.publicLinks = publicLinks; 
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", visibility=" + visibility +
                ", createdAt=" + createdAt +
                ", viewCount=" + viewCount +
                '}';
    }
}