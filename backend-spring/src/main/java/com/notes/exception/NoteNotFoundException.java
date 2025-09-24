package com.notes.exception;

public class NoteNotFoundException extends RuntimeException {
    
    private final Long noteId;
    
    public NoteNotFoundException(Long noteId) {
        super("Note non trouvée avec l'ID: " + noteId);
        this.noteId = noteId;
    }
    
    public NoteNotFoundException(Long noteId, String message) {
        super(message);
        this.noteId = noteId;
    }
    
    public NoteNotFoundException(Long noteId, String message, Throwable cause) {
        super(message, cause);
        this.noteId = noteId;
    }
    
    public Long getNoteId() {
        return noteId;
    }
}


