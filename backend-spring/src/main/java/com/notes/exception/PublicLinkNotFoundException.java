package com.notes.exception;

public class PublicLinkNotFoundException extends RuntimeException {
    
    private final String urlToken;
    
    public PublicLinkNotFoundException(String urlToken) {
        super("Lien public non trouvé avec le token: " + urlToken);
        this.urlToken = urlToken;
    }
    
    public PublicLinkNotFoundException(String urlToken, String message) {
        super(message);
        this.urlToken = urlToken;
    }
    
    public PublicLinkNotFoundException(String urlToken, String message, Throwable cause) {
        super(message, cause);
        this.urlToken = urlToken;
    }
    
    public String getUrlToken() {
        return urlToken;
    }
}


