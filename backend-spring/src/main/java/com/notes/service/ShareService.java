package com.notes.service;

import com.notes.dto.share.ShareCreateDto;
import com.notes.dto.share.ShareDto;
import com.notes.exception.NoteNotFoundException;
import com.notes.exception.ShareNotFoundException;
import com.notes.exception.UnauthorizedAccessException;
import com.notes.mapper.ShareMapper;
import com.notes.model.Note;
import com.notes.model.Share;
import com.notes.model.SharePermission;
import com.notes.model.User;
import com.notes.repository.NoteRepository;
import com.notes.repository.ShareRepository;
import com.notes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShareService {

    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);

    private final ShareRepository shareRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ShareMapper shareMapper;

    public ShareService(ShareRepository shareRepository, NoteRepository noteRepository, 
                       UserRepository userRepository, ShareMapper shareMapper) {
        this.shareRepository = shareRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.shareMapper = shareMapper;
    }

    // Création d'un partage avec un utilisateur
    @Transactional
    public ShareDto shareNoteWithUser(Long noteId, ShareCreateDto shareCreateDto, User currentUser) {
        logger.info("Sharing note {} with user {} by {}", noteId, shareCreateDto.getUserEmail(), currentUser.getEmail());

        // Vérification de l'existence de la note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        // Vérification que l'utilisateur est le propriétaire
        if (!note.getOwner().equals(currentUser)) {
            logger.warn("Unauthorized share attempt to note {} by user {}", noteId, currentUser.getEmail());
            throw new UnauthorizedAccessException(noteId, "note", "Vous ne pouvez partager que vos propres notes");
        }

        // Vérification que l'utilisateur ne partage pas avec lui-même
        if (shareCreateDto.getUserEmail().equals(currentUser.getEmail())) {
            throw new RuntimeException("Vous ne pouvez pas partager une note avec vous-même");
        }

        // Recherche de l'utilisateur avec qui partager
        User sharedWithUser = userRepository.findByEmail(shareCreateDto.getUserEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + shareCreateDto.getUserEmail()));

        // Vérification qu'il n'y a pas déjà un partage actif
        Optional<Share> existingShare = shareRepository.findByNoteIdAndSharedWithUserId(noteId, sharedWithUser.getId());
        if (existingShare.isPresent() && existingShare.get().isValid()) {
            throw new RuntimeException("Cette note est déjà partagée avec cet utilisateur");
        }

        // Création du partage
        Share share = new Share();
        share.setNote(note);
        share.setSharedWithUser(sharedWithUser);
        share.setPermission(shareCreateDto.getPermission());
        share.setExpiresAt(shareCreateDto.getExpiresAt());
        share.setIsActive(true);

        Share savedShare = shareRepository.save(share);
        logger.info("Note shared successfully with ID: {}", savedShare.getId());

        return shareMapper.toDto(savedShare);
    }

    // Récupération des partages d'une note
    public List<ShareDto> getNoteShares(Long noteId, User currentUser) {
        logger.info("Getting shares for note {} by user {}", noteId, currentUser.getEmail());

        // Vérification de l'existence de la note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        // Vérification que l'utilisateur est le propriétaire
        if (!note.getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(noteId, "note", "Vous ne pouvez voir les partages que de vos propres notes");
        }

        List<Share> shares = shareRepository.findByNoteId(noteId);
        return shares.stream()
                .map(shareMapper::toDto)
                .collect(Collectors.toList());
    }

    // Récupération des partages reçus par l'utilisateur
    public Page<ShareDto> getReceivedShares(User currentUser, Pageable pageable) {
        logger.info("Getting received shares for user: {}", currentUser.getEmail());

        Page<Share> shares = shareRepository.findBySharedWithUserId(currentUser.getId(), pageable);
        return shares.map(shareMapper::toDto);
    }

    // Suppression d'un partage
    @Transactional
    public void deleteShare(Long shareId, User currentUser) {
        logger.info("Deleting share {} by user {}", shareId, currentUser.getEmail());

        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ShareNotFoundException(shareId));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!share.getNote().getOwner().equals(currentUser)) {
            logger.warn("Unauthorized delete attempt to share {} by user {}", shareId, currentUser.getEmail());
            throw new UnauthorizedAccessException(shareId, "share", "Vous ne pouvez supprimer que les partages de vos propres notes");
        }

        shareRepository.delete(share);
        logger.info("Share deleted successfully with ID: {}", shareId);
    }

    // Mise à jour d'un partage
    @Transactional
    public ShareDto updateShare(Long shareId, ShareCreateDto shareUpdateDto, User currentUser) {
        logger.info("Updating share {} by user {}", shareId, currentUser.getEmail());

        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ShareNotFoundException(shareId));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!share.getNote().getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(shareId, "share", "Vous ne pouvez modifier que les partages de vos propres notes");
        }

        // Mise à jour des champs
        share.setPermission(shareUpdateDto.getPermission());
        share.setExpiresAt(shareUpdateDto.getExpiresAt());

        Share updatedShare = shareRepository.save(share);
        logger.info("Share updated successfully with ID: {}", updatedShare.getId());

        return shareMapper.toDto(updatedShare);
    }

    // Révocation d'un partage
    @Transactional
    public void revokeShare(Long shareId, User currentUser) {
        logger.info("Revoking share {} by user {}", shareId, currentUser.getEmail());

        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ShareNotFoundException(shareId));

        // Vérification que l'utilisateur est le propriétaire de la note
        if (!share.getNote().getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(shareId, "share", "Vous ne pouvez révoquer que les partages de vos propres notes");
        }

        share.setIsActive(false);
        shareRepository.save(share);
        logger.info("Share revoked successfully with ID: {}", shareId);
    }

    // Révocation de tous les partages d'une note
    @Transactional
    public void revokeAllNoteShares(Long noteId, User currentUser) {
        logger.info("Revoking all shares for note {} by user {}", noteId, currentUser.getEmail());

        // Vérification de l'existence de la note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        // Vérification que l'utilisateur est le propriétaire
        if (!note.getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(noteId, "note", "Vous ne pouvez révoquer les partages que de vos propres notes");
        }

        List<Share> shares = shareRepository.findByNoteId(noteId);
        shares.forEach(share -> {
            share.setIsActive(false);
            shareRepository.save(share);
        });

        logger.info("All shares revoked for note {}", noteId);
    }

    // Vérification des permissions d'accès à une note
    public boolean hasAccessToNote(Long noteId, User user) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return false;
        }

        Note note = noteOpt.get();
        return note.isAccessibleBy(user);
    }

    // Récupération des permissions d'un utilisateur sur une note
    public SharePermission getUserPermissionOnNote(Long noteId, User user) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return null;
        }

        Note note = noteOpt.get();

        // Si c'est le propriétaire
        if (note.getOwner().equals(user)) {
            return SharePermission.ADMIN;
        }

        // Si c'est une note publique
        if (note.getVisibility() == com.notes.model.NoteVisibility.PUBLIC) {
            return SharePermission.READ;
        }

        // Recherche d'un partage actif
        Optional<Share> shareOpt = shareRepository.findByNoteIdAndSharedWithUserId(noteId, user.getId());
        if (shareOpt.isPresent() && shareOpt.get().isValid()) {
            return shareOpt.get().getPermission();
        }

        return null;
    }

    // Nettoyage des partages expirés
    @Transactional
    public int cleanupExpiredShares() {
        logger.info("Cleaning up expired shares...");
        
        int deletedCount = shareRepository.deleteExpiredShares(LocalDateTime.now());
        if (deletedCount > 0) {
            logger.info("Deleted {} expired shares", deletedCount);
        }
        
        return deletedCount;
    }

    // Statistiques des partages
    public long countActiveShares() {
        return shareRepository.findActiveShares().size();
    }

    public long countExpiredShares() {
        return shareRepository.findExpiredShares().size();
    }

    public long countSharesByPermission(SharePermission permission) {
        return shareRepository.findActiveSharesByPermission(permission).size();
    }
}
