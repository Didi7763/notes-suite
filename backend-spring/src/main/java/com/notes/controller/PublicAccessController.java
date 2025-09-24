package com.notes.controller;

import com.notes.dto.note.NoteDto;
import com.notes.dto.publiclink.PublicLinkDto;
import com.notes.mapper.NoteMapper;
import com.notes.model.Note;
import com.notes.service.NoteService;
import com.notes.service.PublicLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/p")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicAccessController {

    private static final Logger logger = LoggerFactory.getLogger(PublicAccessController.class);

    private final PublicLinkService publicLinkService;
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    public PublicAccessController(PublicLinkService publicLinkService, NoteService noteService, NoteMapper noteMapper) {
        this.publicLinkService = publicLinkService;
        this.noteService = noteService;
        this.noteMapper = noteMapper;
    }

    // GET /api/v1/p/{url_token} (accès public)
    @GetMapping("/{urlToken}")
    public ResponseEntity<?> accessNoteByPublicLink(@PathVariable String urlToken,
                                                   @RequestParam(required = false) String password) {
        try {
            logger.info("Public access attempt to note with token: {}", urlToken);

            // Accès via le lien public
            PublicLinkDto publicLink = publicLinkService.accessNoteByToken(urlToken, password);

            // Récupération de la note
            Note note = noteService.findNoteById(publicLink.getNoteId())
                    .orElseThrow(() -> new RuntimeException("Note non trouvée"));

            // Vérification que la note est accessible publiquement
            if (note.getVisibility() != com.notes.model.NoteVisibility.PUBLIC) {
                logger.warn("Attempt to access non-public note via public link: {}", urlToken);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Cette note n'est pas accessible publiquement", "La note n'est pas publique"));
            }

            NoteDto noteDto = noteMapper.toDto(note);

            // Création de la réponse avec les informations du lien public
            Map<String, Object> response = new HashMap<>();
            response.put("note", noteDto);
            response.put("publicLink", publicLink);
            response.put("accessInfo", createAccessInfo(publicLink));

            logger.info("Public access successful to note {} via token {}", note.getId(), urlToken);
            return ResponseEntity.ok(response);

        } catch (com.notes.exception.PublicLinkNotFoundException e) {
            logger.warn("Public link not found or invalid: {}", urlToken);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Lien public non trouvé ou invalide", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error accessing note via public link {}: {}", urlToken, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur d'accès", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error accessing note via public link {}: {}", urlToken, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    // GET /api/v1/p/{url_token}/info (informations sur le lien public sans accès à la note)
    @GetMapping("/{urlToken}/info")
    public ResponseEntity<?> getPublicLinkInfo(@PathVariable String urlToken) {
        try {
            logger.info("Getting public link info for token: {}", urlToken);

            // Vérification de l'existence du lien public
            if (!publicLinkService.publicLinkExists(urlToken)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Lien public non trouvé", "Le lien public n'existe pas"));
            }

            // Récupération des informations du lien public
            var publicLinkOpt = publicLinkService.findPublicLinkByToken(urlToken);
            if (publicLinkOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Lien public non trouvé", "Le lien public n'existe pas"));
            }

            var publicLink = publicLinkOpt.get();

            // Vérification de la validité
            if (!publicLink.isValid()) {
                String reason = publicLink.isExpired() ? "Le lien a expiré" : 
                               (publicLink.getMaxAccessCount() != null && publicLink.getAccessCount() >= publicLink.getMaxAccessCount()) ? "Le lien a atteint le nombre maximum d'accès" :
                               "Le lien n'est plus actif";
                
                return ResponseEntity.status(HttpStatus.GONE)
                        .body(createErrorResponse("Lien public invalide", reason));
            }

            // Création de la réponse avec les informations du lien
            Map<String, Object> response = new HashMap<>();
            response.put("urlToken", publicLink.getUrlToken());
            response.put("isPasswordProtected", publicLink.isPasswordProtected());
            response.put("expiresAt", publicLink.getExpiresAt());
            response.put("maxAccessCount", publicLink.getMaxAccessCount());
            response.put("accessCount", publicLink.getAccessCount());
            response.put("remainingAccess", publicLink.getMaxAccessCount() != null ? 
                        publicLink.getMaxAccessCount() - publicLink.getAccessCount() : null);
            response.put("isValid", publicLink.isValid());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting public link info for token {}: {}", urlToken, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
        }
    }

    // POST /api/v1/p/{url_token}/verify-password (vérification du mot de passe)
    @PostMapping("/{urlToken}/verify-password")
    public ResponseEntity<?> verifyPassword(@PathVariable String urlToken,
                                           @RequestBody Map<String, String> passwordRequest) {
        try {
            logger.info("Password verification attempt for token: {}", urlToken);

            String password = passwordRequest.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Mot de passe requis", "Le mot de passe est obligatoire"));
            }

            // Tentative d'accès avec le mot de passe
            try {
                PublicLinkDto publicLink = publicLinkService.accessNoteByToken(urlToken, password);
                
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("message", "Mot de passe correct");
                response.put("publicLink", publicLink);
                
                return ResponseEntity.ok(response);
                
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Mot de passe incorrect")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(createErrorResponse("Mot de passe incorrect", "Le mot de passe fourni est incorrect"));
                }
                throw e;
            }

        } catch (com.notes.exception.PublicLinkNotFoundException e) {
            logger.warn("Public link not found for password verification: {}", urlToken);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Lien public non trouvé", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error verifying password for token {}: {}", urlToken, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne du serveur", "Une erreur inattendue s'est produite"));
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

    private Map<String, Object> createAccessInfo(PublicLinkDto publicLink) {
        Map<String, Object> accessInfo = new HashMap<>();
        accessInfo.put("isPasswordProtected", publicLink.getIsPasswordProtected());
        accessInfo.put("expiresAt", publicLink.getExpiresAt());
        accessInfo.put("maxAccessCount", publicLink.getMaxAccessCount());
        accessInfo.put("accessCount", publicLink.getAccessCount());
        
        if (publicLink.getMaxAccessCount() != null) {
            accessInfo.put("remainingAccess", publicLink.getMaxAccessCount() - publicLink.getAccessCount());
        }
        
        return accessInfo;
    }
}
