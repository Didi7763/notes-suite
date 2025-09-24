package com.notes.exception;

public class UnauthorizedAccessException extends RuntimeException {
    
    private final Long resourceId;
    private final String resourceType;
    
    public UnauthorizedAccessException(String message) {
        super(message);
        this.resourceId = null;
        this.resourceType = null;
    }
    
    public UnauthorizedAccessException(Long resourceId, String resourceType) {
        super("Accès non autorisé à la ressource " + resourceType + " avec l'ID: " + resourceId);
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
    
    public UnauthorizedAccessException(Long resourceId, String resourceType, String message) {
        super(message);
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
    
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
        this.resourceId = null;
        this.resourceType = null;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
}


