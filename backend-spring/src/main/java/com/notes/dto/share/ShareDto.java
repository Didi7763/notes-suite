package com.notes.dto.share;

import com.notes.model.SharePermission;

import java.time.LocalDateTime;

public class ShareDto {
    
    private Long id;
    private Long noteId;
    private String noteTitle;
    private Long sharedWithUserId;
    private String sharedWithUserEmail;
    private SharePermission permission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;

    // Constructors
    public ShareDto() {}

    public ShareDto(Long id, Long noteId, String noteTitle, Long sharedWithUserId, 
                   String sharedWithUserEmail, SharePermission permission, 
                   LocalDateTime createdAt, LocalDateTime updatedAt, 
                   LocalDateTime expiresAt, Boolean isActive) {
        this.id = id;
        this.noteId = noteId;
        this.noteTitle = noteTitle;
        this.sharedWithUserId = sharedWithUserId;
        this.sharedWithUserEmail = sharedWithUserEmail;
        this.permission = permission;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
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

    public Long getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(Long sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getSharedWithUserEmail() {
        return sharedWithUserEmail;
    }

    public void setSharedWithUserEmail(String sharedWithUserEmail) {
        this.sharedWithUserEmail = sharedWithUserEmail;
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

    @Override
    public String toString() {
        return "ShareDto{" +
                "id=" + id +
                ", noteId=" + noteId +
                ", sharedWithUserEmail='" + sharedWithUserEmail + '\'' +
                ", permission=" + permission +
                ", isActive=" + isActive +
                '}';
    }
}


