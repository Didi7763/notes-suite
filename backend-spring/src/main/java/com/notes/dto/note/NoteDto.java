package com.notes.dto.note;

import com.notes.model.NoteVisibility;
import com.notes.dto.tag.TagDto;
import com.notes.dto.share.ShareDto;
import com.notes.dto.publiclink.PublicLinkDto;

import java.time.LocalDateTime;
import java.util.List;

public class NoteDto {
    
    private Long id;
    private Long ownerId;
    private String ownerEmail;
    private String title;
    private String contentMd;
    private NoteVisibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long viewCount;
    private Boolean isFavorite;
    private List<TagDto> tags;
    private List<ShareDto> shares;
    private List<PublicLinkDto> publicLinks;

    // Constructors
    public NoteDto() {}

    public NoteDto(Long id, Long ownerId, String ownerEmail, String title, String contentMd, 
                   NoteVisibility visibility, LocalDateTime createdAt, LocalDateTime updatedAt, 
                   Long viewCount, Boolean isFavorite) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.title = title;
        this.contentMd = contentMd;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.viewCount = viewCount;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public List<ShareDto> getShares() {
        return shares;
    }

    public void setShares(List<ShareDto> shares) {
        this.shares = shares;
    }

    public List<PublicLinkDto> getPublicLinks() {
        return publicLinks;
    }

    public void setPublicLinks(List<PublicLinkDto> publicLinks) {
        this.publicLinks = publicLinks;
    }

    @Override
    public String toString() {
        return "NoteDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", visibility=" + visibility +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", viewCount=" + viewCount +
                '}';
    }
}