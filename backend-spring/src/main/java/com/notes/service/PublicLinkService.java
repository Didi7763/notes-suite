package com.notes.service;

import com.notes.dto.publiclink.PublicLinkCreateDto;
import com.notes.dto.publiclink.PublicLinkDto;
import com.notes.exception.NoteNotFoundException;
import com.notes.exception.PublicLinkNotFoundException;
import com.notes.exception.UnauthorizedAccessException;
import com.notes.mapper.PublicLinkMapper;
import com.notes.model.Note;
import com.notes.model.PublicLink;
import com.notes.model.User;
import com.notes.repository.NoteRepository;
import com.notes.repository.PublicLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PublicLinkService {

    private static final Logger logger = LoggerFactory.getLogger(PublicLinkService.class);

    private final PublicLinkRepository publicLinkRepository;
    private final NoteRepository noteRepository;
    private final PublicLinkMapper publicLinkMapper;
    private final PasswordEncoder passwordEncoder;

    public PublicLinkService(PublicLinkRepository publicLinkRepository, NoteRepository noteRepository,
                            PublicLinkMapper publicLinkMapper, PasswordEncoder passwordEncoder) {
        this.publicLinkRepository = publicLinkRepository;
        this.noteRepository = noteRepository;
        this.publicLinkMapper = publicLinkMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // Création d'un lien public
    @Transactional
    public PublicLinkDto createPublicLink(Long noteId, PublicLinkCreateDto publicLinkCreateDto, User currentUser) {
        logger.info("Creating public link for note {} by user {}", noteId, currentUser.getEmail());

        // Vérification de l'existence de la note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        // Vérification que l'utilisateur est le propriétaire
        if (!note.getOwner().equals(currentUser)) {
            logger.warn("Unauthorized public link creation attempt for note {} by user {}", noteId, currentUser.getEmail());
            throw new UnauthorizedAccessException(noteId, "note", "Vous ne pouvez créer des liens publics que pour vos propres notes");
        }

        // Génération d'un token unique
        String urlToken = generateUniqueUrlToken();

        // Création du lien public
        PublicLink publicLink = new PublicLink();
        publicLink.setNote(note);
        publicLink.setUrlToken(urlToken);
        publicLink.setExpiresAt(publicLinkCreateDto.getExpiresAt());
        publicLink.setMaxAccessCount(publicLinkCreateDto.getMaxAccessCount());
        publicLink.setAccessCount(0L);
        publicLink.setIsActive(true);

        // Gestion du mot de passe optionnel
        if (publicLinkCreateDto.getPassword() != null && !publicLinkCreateDto.getPassword().trim().isEmpty()) {
            publicLink.setPasswordHash(passwordEncoder.encode(publicLinkCreateDto.getPassword()));
        }

        PublicLink savedPublicLink = publicLinkRepository.save(publicLink);
        logger.info("Public link created successfully with token: {}", urlToken);

        return publicLinkMapper.toDto(savedPublicLink);
    }

    // Accès à une note via un lien public
    @Transactional
    public PublicLinkDto accessNoteByToken(String urlToken, String password) {
        logger.info("Accessing note with public token: {}", urlToken);

        PublicLink publicLink = publicLinkRepository.findByUrlToken(urlToken)
                .orElseThrow(() -> new PublicLinkNotFoundException(urlToken));

        // Vérification que le lien est valide
        if (!publicLink.isValid()) {
            if (publicLink.isExpired()) {
                throw new PublicLinkNotFoundException(urlToken, "Ce lien public a expiré");
            }
            if (publicLink.getMaxAccessCount() != null && publicLink.getAccessCount() >= publicLink.getMaxAccessCount()) {
                throw new PublicLinkNotFoundException(urlToken, "Ce lien public a atteint le nombre maximum d'accès");
            }
            throw new PublicLinkNotFoundException(urlToken, "Ce lien public n'est plus actif");
        }

        // Vérification du mot de passe si nécessaire
        if (publicLink.isPasswordProtected()) {
            if (password == null || password.trim().isEmpty()) {
                throw new RuntimeException("Ce lien public est protégé par un mot de passe");
            }
            if (!passwordEncoder.matches(password, publicLink.getPasswordHash())) {
                throw new RuntimeException("Mot de passe incorrect");
            }
        }

        // Incrémentation du compteur d'accès
        publicLink.incrementAccessCount();
        publicLinkRepository.save(publicLink);

        // Incrémentation du compteur de vues de la note
        Note note = publicLink.getNote();
        note.incrementViewCount();
        noteRepository.save(note);

        logger.info("Note accessed successfully via public link: {}", urlToken);
        return publicLinkMapper.toDto(publicLink);
    }

    // Récupération des liens publics d'une note
    public List<PublicLinkDto> getNotePublicLinks(Long noteId, User currentUser) {
        logger.info("Getting public links for note {} by user {}", noteId, currentUser.getEmail());

        // Vérification de l'existence de la note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        // Vérification que l'utilisateur est le propriétaire
        if (!note.getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(noteId, "note", "Vous ne pouvez voir les liens publics que de vos propres notes");
        }

        List<PublicLink> publicLinks = publicLinkRepository.findByNoteId(noteId);
        return publicLinks.stream()
                .map(publicLinkMapper::toDto)
                .collect(Collectors.toList());
    }

    // Suppression d'un lien public
    @Transactional
    public void deletePublicLink(Long publicLinkId, User currentUser) {
        logger.info("Deleting public link {} by user {}", publicLinkId, currentUser.getEmail());

        PublicLink publicLink = publicLinkRepository.findById(publicLinkId)
                .orElseThrow(() -> new PublicLinkNotFoundException("Lien public non trouvé"));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!publicLink.getNote().getOwner().equals(currentUser)) {
            logger.warn("Unauthorized delete attempt to public link {} by user {}", publicLinkId, currentUser.getEmail());
            throw new UnauthorizedAccessException(publicLinkId, "publicLink", "Vous ne pouvez supprimer que les liens publics de vos propres notes");
        }

        publicLinkRepository.delete(publicLink);
        logger.info("Public link deleted successfully with ID: {}", publicLinkId);
    }

    // Suppression d'un lien public par token
    @Transactional
    public void deletePublicLinkByToken(String urlToken, User currentUser) {
        logger.info("Deleting public link with token {} by user {}", urlToken, currentUser.getEmail());

        PublicLink publicLink = publicLinkRepository.findByUrlToken(urlToken)
                .orElseThrow(() -> new PublicLinkNotFoundException(urlToken));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!publicLink.getNote().getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(publicLink.getId(), "publicLink", "Vous ne pouvez supprimer que les liens publics de vos propres notes");
        }

        publicLinkRepository.delete(publicLink);
        logger.info("Public link deleted successfully with token: {}", urlToken);
    }

    // Désactivation d'un lien public
    @Transactional
    public PublicLinkDto deactivatePublicLink(Long publicLinkId, User currentUser) {
        logger.info("Deactivating public link {} by user {}", publicLinkId, currentUser.getEmail());

        PublicLink publicLink = publicLinkRepository.findById(publicLinkId)
                .orElseThrow(() -> new PublicLinkNotFoundException("Lien public non trouvé"));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!publicLink.getNote().getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(publicLinkId, "publicLink", "Vous ne pouvez désactiver que les liens publics de vos propres notes");
        }

        publicLink.setIsActive(false);
        PublicLink updatedPublicLink = publicLinkRepository.save(publicLink);

        logger.info("Public link deactivated successfully with ID: {}", publicLinkId);
        return publicLinkMapper.toDto(updatedPublicLink);
    }

    // Mise à jour d'un lien public
    @Transactional
    public PublicLinkDto updatePublicLink(Long publicLinkId, PublicLinkCreateDto publicLinkUpdateDto, User currentUser) {
        logger.info("Updating public link {} by user {}", publicLinkId, currentUser.getEmail());

        PublicLink publicLink = publicLinkRepository.findById(publicLinkId)
                .orElseThrow(() -> new PublicLinkNotFoundException("Lien public non trouvé"));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!publicLink.getNote().getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(publicLinkId, "publicLink", "Vous ne pouvez modifier que les liens publics de vos propres notes");
        }

        // Mise à jour des champs
        publicLink.setExpiresAt(publicLinkUpdateDto.getExpiresAt());
        publicLink.setMaxAccessCount(publicLinkUpdateDto.getMaxAccessCount());

        // Mise à jour du mot de passe si fourni
        if (publicLinkUpdateDto.getPassword() != null) {
            if (publicLinkUpdateDto.getPassword().trim().isEmpty()) {
                publicLink.setPasswordHash(null); // Suppression du mot de passe
            } else {
                publicLink.setPasswordHash(passwordEncoder.encode(publicLinkUpdateDto.getPassword()));
            }
        }

        PublicLink updatedPublicLink = publicLinkRepository.save(publicLink);
        logger.info("Public link updated successfully with ID: {}", publicLinkId);

        return publicLinkMapper.toDto(updatedPublicLink);
    }

    // Nettoyage des liens publics expirés
    @Transactional
    public int cleanupExpiredPublicLinks() {
        logger.info("Cleaning up expired public links...");
        
        int deletedCount = publicLinkRepository.deleteExpiredLinks(LocalDateTime.now());
        if (deletedCount > 0) {
            logger.info("Deleted {} expired public links", deletedCount);
        }
        
        return deletedCount;
    }

    // Statistiques des liens publics
    public long countActivePublicLinks() {
        return publicLinkRepository.findActivePublicLinks().size();
    }

    public long countPasswordProtectedPublicLinks() {
        return publicLinkRepository.findPasswordProtectedPublicLinks().size();
    }

    public long countPublicLinksWithRemainingAccess() {
        return publicLinkRepository.findPublicLinksWithRemainingAccess().size();
    }

    // Méthodes utilitaires
    private String generateUniqueUrlToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (publicLinkRepository.existsByUrlToken(token));
        return token;
    }

    // Vérification de l'existence d'un lien public
    public boolean publicLinkExists(String urlToken) {
        return publicLinkRepository.findByUrlToken(urlToken).isPresent();
    }

    // Récupération d'un lien public par token (pour usage interne)
    public Optional<PublicLink> findPublicLinkByToken(String urlToken) {
        return publicLinkRepository.findByUrlToken(urlToken);
    }
}
