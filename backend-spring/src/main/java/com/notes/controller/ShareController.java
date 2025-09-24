package com.notes.controller;

import com.notes.dto.share.ShareCreateDto;
import com.notes.dto.share.ShareDto;
import com.notes.model.User;
import com.notes.service.ShareService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShareController {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    // POST /api/v1/notes/{id}/share/user
    @PostMapping("/notes/{id}/share/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> shareNoteWithUser(@PathVariable Long id,
                                              @Valid @RequestBody ShareCreateDto shareCreateDto,
                                              @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Sharing note {} with user {} by {}", id, shareCreateDto.getUserEmail(), currentUser.getEmail());

            ShareDto share = shareService.shareNoteWithUser(id, shareCreateDto, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(share);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized share attempt to note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error sharing note {} with user {}: {}", id, shareCreateDto.getUserEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors du partage", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error sharing note {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    // GET /api/v1/notes/{id}/shares
    @GetMapping("/notes/{id}/shares")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNoteShares(@PathVariable Long id,
                                          @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Getting shares for note {} by user {}", id, currentUser.getEmail());

            List<ShareDto> shares = shareService.getNoteShares(id, currentUser);

            return ResponseEntity.ok(shares);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized access to note shares {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting shares for note {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des partages", e.getMessage()));
        }
    }

    // GET /api/v1/shares/received
    @GetMapping("/shares/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReceivedShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            logger.info("Getting received shares for user: {}", currentUser.getEmail());

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ShareDto> shares = shareService.getReceivedShares(currentUser, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", shares.getNumber());
            response.put("size", shares.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting received shares for user {}: {}", currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des partages reçus", e.getMessage()));
        }
    }

    // DELETE /api/v1/shares/{shareId}
    @DeleteMapping("/shares/{shareId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteShare(@PathVariable Long shareId,
                                        @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Deleting share {} by user {}", shareId, currentUser.getEmail());

            shareService.deleteShare(shareId, currentUser);

            return ResponseEntity.noContent().build();

        } catch (com.notes.exception.ShareNotFoundException e) {
            logger.warn("Share not found with ID: {}", shareId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Partage non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized delete attempt to share {} by user {}", shareId, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting share {}: {}", shareId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression du partage", e.getMessage()));
        }
    }

    // PUT /api/v1/shares/{shareId}
    @PutMapping("/shares/{shareId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateShare(@PathVariable Long shareId,
                                        @Valid @RequestBody ShareCreateDto shareUpdateDto,
                                        @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Updating share {} by user {}", shareId, currentUser.getEmail());

            ShareDto updatedShare = shareService.updateShare(shareId, shareUpdateDto, currentUser);

            return ResponseEntity.ok(updatedShare);

        } catch (com.notes.exception.ShareNotFoundException e) {
            logger.warn("Share not found with ID: {}", shareId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Partage non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized update attempt to share {} by user {}", shareId, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating share {}: {}", shareId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors de la mise à jour du partage", e.getMessage()));
        }
    }

    // POST /api/v1/shares/{shareId}/revoke
    @PostMapping("/shares/{shareId}/revoke")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> revokeShare(@PathVariable Long shareId,
                                        @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Revoking share {} by user {}", shareId, currentUser.getEmail());

            shareService.revokeShare(shareId, currentUser);

            return ResponseEntity.ok(createSuccessResponse("Partage révoqué avec succès"));

        } catch (com.notes.exception.ShareNotFoundException e) {
            logger.warn("Share not found with ID: {}", shareId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Partage non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized revoke attempt to share {} by user {}", shareId, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error revoking share {}: {}", shareId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la révocation du partage", e.getMessage()));
        }
    }

    // POST /api/v1/notes/{id}/shares/revoke-all
    @PostMapping("/notes/{id}/shares/revoke-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> revokeAllNoteShares(@PathVariable Long id,
                                                @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Revoking all shares for note {} by user {}", id, currentUser.getEmail());

            shareService.revokeAllNoteShares(id, currentUser);

            return ResponseEntity.ok(createSuccessResponse("Tous les partages de la note ont été révoqués"));

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized revoke all attempt for note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error revoking all shares for note {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la révocation des partages", e.getMessage()));
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

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}


