package com.notes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags",
       indexes = {
           @Index(name = "idx_tag_label", columnList = "label", unique = true),
           @Index(name = "idx_tag_created_at", columnList = "created_at")
       })
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libellé du tag est obligatoire")
    @Size(max = 50, message = "Le libellé ne peut pas dépasser 50 caractères")
    @Column(unique = true, nullable = false, length = 50)
    private String label;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    @Column(length = 255)
    private String description;

    @Column(name = "color", length = 7)
    private String color; // Format hexadécimal #RRGGBB

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    // Relations
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoteTag> noteTags = new ArrayList<>();

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
    public Tag() {}

    public Tag(String label) {
        this.label = label;
    }

    public Tag(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public Tag(String label, String description, String color) {
        this.label = label;
        this.description = description;
        this.color = color;
    }

    // Business methods
    public void incrementUsageCount() {
        this.usageCount++;
    }

    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }

    public List<Note> getNotes() {
        return this.noteTags.stream()
                .map(NoteTag::getNote)
                .toList();
    }

    public boolean isUsed() {
        return this.usageCount > 0;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getLabel() { 
        return label; 
    }
    
    public void setLabel(String label) { 
        this.label = label; 
    }

    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getColor() { 
        return color; 
    }
    
    public void setColor(String color) { 
        this.color = color; 
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

    public Long getUsageCount() { 
        return usageCount; 
    }
    
    public void setUsageCount(Long usageCount) { 
        this.usageCount = usageCount; 
    }

    public List<NoteTag> getNoteTags() { 
        return noteTags; 
    }
    
    public void setNoteTags(List<NoteTag> noteTags) { 
        this.noteTags = noteTags; 
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", usageCount=" + usageCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return label != null ? label.equals(tag.label) : tag.label == null;
    }

    @Override
    public int hashCode() {
        return label != null ? label.hashCode() : 0;
    }
}