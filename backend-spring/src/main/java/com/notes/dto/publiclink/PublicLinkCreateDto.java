package com.notes.dto.publiclink;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PublicLinkCreateDto {
    
    private LocalDateTime expiresAt;

    @Min(value = 1, message = "Le nombre maximum d'accès doit être au moins 1")
    @Max(value = 10000, message = "Le nombre maximum d'accès ne peut pas dépasser 10000")
    private Long maxAccessCount;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;

    private String password;

    // Constructors
    public PublicLinkCreateDto() {}

    public PublicLinkCreateDto(LocalDateTime expiresAt, Long maxAccessCount, String description) {
        this.expiresAt = expiresAt;
        this.maxAccessCount = maxAccessCount;
        this.description = description;
    }

    public PublicLinkCreateDto(LocalDateTime expiresAt, Long maxAccessCount, String description, String password) {
        this.expiresAt = expiresAt;
        this.maxAccessCount = maxAccessCount;
        this.description = description;
        this.password = password;
    }

    // Getters and Setters
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getMaxAccessCount() {
        return maxAccessCount;
    }

    public void setMaxAccessCount(Long maxAccessCount) {
        this.maxAccessCount = maxAccessCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "PublicLinkCreateDto{" +
                "expiresAt=" + expiresAt +
                ", maxAccessCount=" + maxAccessCount +
                ", description='" + description + '\'' +
                '}';
    }
}
