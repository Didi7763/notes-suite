package com.notes.dto.note;

import com.notes.model.NoteVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class NoteUpdateDto {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;

    private String contentMd;

    @NotNull(message = "La visibilité est obligatoire")
    private NoteVisibility visibility;

    private List<String> tags;

    // Constructors
    public NoteUpdateDto() {}

    public NoteUpdateDto(String title, String contentMd, NoteVisibility visibility) {
        this.title = title;
        this.contentMd = contentMd;
        this.visibility = visibility;
    }

    public NoteUpdateDto(String title, String contentMd, NoteVisibility visibility, List<String> tags) {
        this.title = title;
        this.contentMd = contentMd;
        this.visibility = visibility;
        this.tags = tags;
    }

    // Getters and Setters
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "NoteUpdateDto{" +
                "title='" + title + '\'' +
                ", visibility=" + visibility +
                ", tags=" + tags +
                '}';
    }
}