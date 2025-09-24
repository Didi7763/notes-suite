package com.notes.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informations utilisateur")
public class UserDto {
    
    @Schema(description = "Identifiant unique de l'utilisateur", example = "1")
    private Long id;
    
    @Schema(description = "Adresse email de l'utilisateur", example = "user@example.com")
    private String email;
    
    @Schema(description = "Date de création du compte", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    // Constructors
    public UserDto() {}
    
    public UserDto(Long id, String email, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}



