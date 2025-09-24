package com.notes.dto.share;

import com.notes.model.SharePermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ShareCreateDto {
    
    @Email(message = "Format d'email invalide")
    @NotNull(message = "L'email de l'utilisateur est obligatoire")
    private String userEmail;

    @NotNull(message = "La permission est obligatoire")
    private SharePermission permission;

    private LocalDateTime expiresAt;

    // Constructors
    public ShareCreateDto() {}

    public ShareCreateDto(String userEmail, SharePermission permission) {
        this.userEmail = userEmail;
        this.permission = permission;
    }

    public ShareCreateDto(String userEmail, SharePermission permission, LocalDateTime expiresAt) {
        this.userEmail = userEmail;
        this.permission = permission;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public SharePermission getPermission() {
        return permission;
    }

    public void setPermission(SharePermission permission) {
        this.permission = permission;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "ShareCreateDto{" +
                "userEmail='" + userEmail + '\'' +
                ", permission=" + permission +
                ", expiresAt=" + expiresAt +
                '}';
    }
}


