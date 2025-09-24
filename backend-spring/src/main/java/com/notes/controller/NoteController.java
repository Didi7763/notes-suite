package com.notes.controller;

import com.notes.dto.note.NoteCreateDto;
import com.notes.dto.note.NoteDto;
import com.notes.dto.note.NoteUpdateDto;
import com.notes.model.NoteVisibility;
import com.notes.model.User;
import com.notes.service.NoteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // GET /api/v1/notes?query=&tag=&visibility=&page=&size=
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) NoteVisibility visibility,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            logger.info("Getting notes for user: {} with query: {}, tag: {}, visibility: {}", 
                       currentUser.getEmail(), query, tag, visibility);

            // Configuration de la pagination et du tri
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<NoteDto> notes = noteService.searchNotes(query, tag, visibility, currentUser, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", notes.getContent());
            response.put("totalElements", notes.getTotalElements());
            response.put("totalPages", notes.getTotalPages());
            response.put("currentPage", notes.getNumber());
            response.put("size", notes.getSize());
            response.put("first", notes.isFirst());
            response.put("last", notes.isLast());
            response.put("numberOfElements", notes.getNumberOfElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting notes for user {}: {}", currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des notes", e.getMessage()));
        }
    }

    // POST /api/v1/notes
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createNote(@Valid @RequestBody NoteCreateDto noteCreateDto,
                                       @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Creating note: {} for user: {}", noteCreateDto.getTitle(), currentUser.getEmail());

            NoteDto createdNote = noteService.createNote(noteCreateDto, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);

        } catch (Exception e) {
            logger.error("Error creating note for user {}: {}", currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors de la création de la note", e.getMessage()));
        }
    }

    // GET /api/v1/notes/{id}
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNoteById(@PathVariable Long id,
                                        @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Getting note with ID: {} for user: {}", id, currentUser.getEmail());

            NoteDto note = noteService.getNoteById(id, currentUser);

            return ResponseEntity.ok(note);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized access to note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting note {} for user {}: {}", id, currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération de la note", e.getMessage()));
        }
    }

    // PUT /api/v1/notes/{id}
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateNote(@PathVariable Long id,
                                       @Valid @RequestBody NoteUpdateDto noteUpdateDto,
                                       @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Updating note with ID: {} by user: {}", id, currentUser.getEmail());

            NoteDto updatedNote = noteService.updateNote(id, noteUpdateDto, currentUser);

            return ResponseEntity.ok(updatedNote);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized update attempt to note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating note {} for user {}: {}", id, currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors de la mise à jour de la note", e.getMessage()));
        }
    }

    // DELETE /api/v1/notes/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteNote(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Deleting note with ID: {} by user: {}", id, currentUser.getEmail());

            noteService.deleteNote(id, currentUser);

            return ResponseEntity.noContent().build();

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized delete attempt to note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting note {} for user {}: {}", id, currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression de la note", e.getMessage()));
        }
    }

    // GET /api/v1/notes/favorites
    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFavoriteNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            logger.info("Getting favorite notes for user: {}", currentUser.getEmail());

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<NoteDto> notes = noteService.getFavoriteNotes(currentUser, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", notes.getContent());
            response.put("totalElements", notes.getTotalElements());
            response.put("totalPages", notes.getTotalPages());
            response.put("currentPage", notes.getNumber());
            response.put("size", notes.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting favorite notes for user {}: {}", currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des notes favorites", e.getMessage()));
        }
    }

    // GET /api/v1/notes/shared
    @GetMapping("/shared")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSharedNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            logger.info("Getting shared notes for user: {}", currentUser.getEmail());

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<NoteDto> notes = noteService.getSharedNotes(currentUser, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", notes.getContent());
            response.put("totalElements", notes.getTotalElements());
            response.put("totalPages", notes.getTotalPages());
            response.put("currentPage", notes.getNumber());
            response.put("size", notes.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting shared notes for user {}: {}", currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des notes partagées", e.getMessage()));
        }
    }

    // POST /api/v1/notes/{id}/favorite
    @PostMapping("/{id}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long id,
                                           @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Toggling favorite status for note {} by user {}", id, currentUser.getEmail());

            NoteDto updatedNote = noteService.toggleFavorite(id, currentUser);

            return ResponseEntity.ok(updatedNote);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized access to note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error toggling favorite for note {} by user {}: {}", id, currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la modification du statut favori", e.getMessage()));
        }
    }

    // Méthodes utilitaires
    private Map<String, Object> createErrorResponse(String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("details", details);
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}