package com.notes.dto.publiclink;

import java.time.LocalDateTime;

public class PublicLinkDto {
    
    private Long id;
    private Long noteId;
    private String noteTitle;
    private String urlToken;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long accessCount;
    private Long maxAccessCount;
    private Boolean isActive;
    private String description;
    private Boolean isPasswordProtected;

    // Constructors
    public PublicLinkDto() {}

    public PublicLinkDto(Long id, Long noteId, String noteTitle, String urlToken, 
                        LocalDateTime expiresAt, LocalDateTime createdAt, 
                        LocalDateTime updatedAt, Long accessCount, 
                        Long maxAccessCount, Boolean isActive, String description, 
                        Boolean isPasswordProtected) {
        this.id = id;
        this.noteId = noteId;
        this.noteTitle = noteTitle;
        this.urlToken = urlToken;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accessCount = accessCount;
        this.maxAccessCount = maxAccessCount;
        this.isActive = isActive;
        this.description = description;
        this.isPasswordProtected = isPasswordProtected;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPasswordProtected() {
        return isPasswordProtected;
    }

    public void setIsPasswordProtected(Boolean isPasswordProtected) {
        this.isPasswordProtected = isPasswordProtected;
    }

    @Override
    public String toString() {
        return "PublicLinkDto{" +
                "id=" + id +
                ", noteId=" + noteId +
                ", urlToken='" + urlToken + '\'' +
                ", expiresAt=" + expiresAt +
                ", accessCount=" + accessCount +
                ", isActive=" + isActive +
                '}';
    }
}
