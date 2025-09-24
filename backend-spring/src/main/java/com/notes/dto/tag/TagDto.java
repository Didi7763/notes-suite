package com.notes.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Tag")
public class TagDto {
    
    @Schema(description = "Identifiant unique du tag", example = "1")
    private Long id;
    
    @Schema(description = "Libellé du tag", example = "travail")
    private String label;
    
    @Schema(description = "Date de création", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    // Constructors
    public TagDto() {}
    
    public TagDto(Long id, String label, LocalDateTime createdAt) {
        this.id = id;
        this.label = label;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}



