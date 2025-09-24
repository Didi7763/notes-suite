package com.notes.exception;

public class ShareNotFoundException extends RuntimeException {
    
    private final Long shareId;
    
    public ShareNotFoundException(Long shareId) {
        super("Partage non trouvé avec l'ID: " + shareId);
        this.shareId = shareId;
    }
    
    public ShareNotFoundException(Long shareId, String message) {
        super(message);
        this.shareId = shareId;
    }
    
    public ShareNotFoundException(Long shareId, String message, Throwable cause) {
        super(message, cause);
        this.shareId = shareId;
    }
    
    public Long getShareId() {
        return shareId;
    }
}


