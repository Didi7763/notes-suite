package com.notes.controller;

import com.notes.dto.publiclink.PublicLinkCreateDto;
import com.notes.dto.publiclink.PublicLinkDto;
import com.notes.model.User;
import com.notes.service.PublicLinkService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PublicLinkController {

    private static final Logger logger = LoggerFactory.getLogger(PublicLinkController.class);

    private final PublicLinkService publicLinkService;

    public PublicLinkController(PublicLinkService publicLinkService) {
        this.publicLinkService = publicLinkService;
    }

    // POST /api/v1/notes/{id}/share/public
    @PostMapping("/notes/{id}/share/public")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPublicLink(@PathVariable Long id,
                                            @Valid @RequestBody PublicLinkCreateDto publicLinkCreateDto,
                                            @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Creating public link for note {} by user {}", id, currentUser.getEmail());

            PublicLinkDto publicLink = publicLinkService.createPublicLink(id, publicLinkCreateDto, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(publicLink);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized public link creation attempt for note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating public link for note {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors de la création du lien public", e.getMessage()));
        }
    }

    // GET /api/v1/notes/{id}/public-links
    @GetMapping("/notes/{id}/public-links")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNotePublicLinks(@PathVariable Long id,
                                               @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Getting public links for note {} by user {}", id, currentUser.getEmail());

            List<PublicLinkDto> publicLinks = publicLinkService.getNotePublicLinks(id, currentUser);

            return ResponseEntity.ok(publicLinks);

        } catch (com.notes.exception.NoteNotFoundException e) {
            logger.warn("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Note non trouvée", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized access to public links for note {} by user {}", id, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting public links for note {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des liens publics", e.getMessage()));
        }
    }

    // PUT /api/v1/public-links/{publicLinkId}
    @PutMapping("/public-links/{publicLinkId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePublicLink(@PathVariable Long publicLinkId,
                                            @Valid @RequestBody PublicLinkCreateDto publicLinkUpdateDto,
                                            @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Updating public link {} by user {}", publicLinkId, currentUser.getEmail());

            PublicLinkDto updatedPublicLink = publicLinkService.updatePublicLink(publicLinkId, publicLinkUpdateDto, currentUser);

            return ResponseEntity.ok(updatedPublicLink);

        } catch (com.notes.exception.PublicLinkNotFoundException e) {
            logger.warn("Public link not found with ID: {}", publicLinkId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Lien public non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized update attempt to public link {} by user {}", publicLinkId, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating public link {}: {}", publicLinkId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur lors de la mise à jour du lien public", e.getMessage()));
        }
    }

    // DELETE /api/v1/public-links/{publicLinkId}
    @DeleteMapping("/public-links/{publicLinkId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePublicLink(@PathVariable Long publicLinkId,
                                            @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Deleting public link {} by user {}", publicLinkId, currentUser.getEmail());

            publicLinkService.deletePublicLink(publicLinkId, currentUser);

            return ResponseEntity.noContent().build();

        } catch (com.notes.exception.PublicLinkNotFoundException e) {
            logger.warn("Public link not found with ID: {}", publicLinkId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Lien public non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized delete attempt to public link {} by user {}", publicLinkId, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting public link {}: {}", publicLinkId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression du lien public", e.getMessage()));
        }
    }

    // DELETE /api/v1/public-links/token/{urlToken}
    @DeleteMapping("/public-links/token/{urlToken}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePublicLinkByToken(@PathVariable String urlToken,
                                                    @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Deleting public link with token {} by user {}", urlToken, currentUser.getEmail());

            publicLinkService.deletePublicLinkByToken(urlToken, currentUser);

            return ResponseEntity.noContent().build();

        } catch (com.notes.exception.PublicLinkNotFoundException e) {
            logger.warn("Public link not found with token: {}", urlToken);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Lien public non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized delete attempt to public link with token {} by user {}", urlToken, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting public link with token {}: {}", urlToken, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression du lien public", e.getMessage()));
        }
    }

    // POST /api/v1/public-links/{publicLinkId}/deactivate
    @PostMapping("/public-links/{publicLinkId}/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deactivatePublicLink(@PathVariable Long publicLinkId,
                                                 @AuthenticationPrincipal User currentUser) {
        try {
            logger.info("Deactivating public link {} by user {}", publicLinkId, currentUser.getEmail());

            PublicLinkDto deactivatedPublicLink = publicLinkService.deactivatePublicLink(publicLinkId, currentUser);

            return ResponseEntity.ok(deactivatedPublicLink);

        } catch (com.notes.exception.PublicLinkNotFoundException e) {
            logger.warn("Public link not found with ID: {}", publicLinkId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Lien public non trouvé", e.getMessage()));
        } catch (com.notes.exception.UnauthorizedAccessException e) {
            logger.warn("Unauthorized deactivate attempt to public link {} by user {}", publicLinkId, currentUser.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès non autorisé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deactivating public link {}: {}", publicLinkId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la désactivation du lien public", e.getMessage()));
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
