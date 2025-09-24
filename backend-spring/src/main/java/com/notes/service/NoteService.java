package com.notes.service;

import com.notes.dto.note.NoteCreateDto;
import com.notes.dto.note.NoteDto;
import com.notes.dto.note.NoteUpdateDto;
import com.notes.exception.NoteNotFoundException;
import com.notes.exception.UnauthorizedAccessException;
import com.notes.mapper.NoteMapper;
import com.notes.model.Note;
import com.notes.model.NoteVisibility;
import com.notes.model.Tag;
import com.notes.model.User;
import com.notes.repository.NoteRepository;
import com.notes.repository.TagRepository;
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
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final NoteMapper noteMapper;

    public NoteService(NoteRepository noteRepository, TagRepository tagRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.tagRepository = tagRepository;
        this.noteMapper = noteMapper;
    }

    // Création d'une nouvelle note
    @Transactional
    public NoteDto createNote(NoteCreateDto noteCreateDto, User currentUser) {
        logger.info("Creating note: {} for user: {}", noteCreateDto.getTitle(), currentUser.getEmail());

        Note note = noteMapper.toEntity(noteCreateDto);
        note.setOwner(currentUser);
        note.setViewCount(0L);
        note.setIsFavorite(false);

        // Gestion des tags
        if (noteCreateDto.getTags() != null && !noteCreateDto.getTags().isEmpty()) {
            List<Tag> tags = noteCreateDto.getTags().stream()
                    .map(this::getOrCreateTag)
                    .collect(Collectors.toList());
            
            tags.forEach(tag -> note.addTag(tag));
        }

        Note savedNote = noteRepository.save(note);
        logger.info("Note created successfully with ID: {}", savedNote.getId());

        return noteMapper.toDto(savedNote);
    }

    // Récupération d'une note par ID avec vérification d'accès
    public NoteDto getNoteById(Long id, User currentUser) {
        logger.info("Getting note with ID: {} for user: {}", id, currentUser.getEmail());

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        // Vérification des permissions d'accès
        if (!note.isAccessibleBy(currentUser)) {
            logger.warn("Unauthorized access attempt to note {} by user {}", id, currentUser.getEmail());
            throw new UnauthorizedAccessException(id, "note", "Vous n'avez pas l'autorisation d'accéder à cette note");
        }

        // Incrémentation du compteur de vues
        note.incrementViewCount();
        noteRepository.save(note);

        return noteMapper.toDto(note);
    }

    // Mise à jour d'une note
    @Transactional
    public NoteDto updateNote(Long id, NoteUpdateDto noteUpdateDto, User currentUser) {
        logger.info("Updating note with ID: {} by user: {}", id, currentUser.getEmail());

        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        // Vérification que l'utilisateur est le propriétaire
        if (!existingNote.getOwner().equals(currentUser)) {
            logger.warn("Unauthorized update attempt to note {} by user {}", id, currentUser.getEmail());
            throw new UnauthorizedAccessException(id, "note", "Vous ne pouvez modifier que vos propres notes");
        }

        // Mise à jour des champs
        noteMapper.updateEntity(noteUpdateDto, existingNote);
        existingNote.setUpdatedAt(LocalDateTime.now());

        // Gestion des tags
        if (noteUpdateDto.getTags() != null) {
            // Suppression des anciens tags
            existingNote.getNoteTags().clear();
            
            // Ajout des nouveaux tags
            List<Tag> tags = noteUpdateDto.getTags().stream()
                    .map(this::getOrCreateTag)
                    .collect(Collectors.toList());
            
            tags.forEach(tag -> existingNote.addTag(tag));
        }

        Note updatedNote = noteRepository.save(existingNote);
        logger.info("Note updated successfully with ID: {}", updatedNote.getId());

        return noteMapper.toDto(updatedNote);
    }

    // Suppression d'une note
    @Transactional
    public void deleteNote(Long id, User currentUser) {
        logger.info("Deleting note with ID: {} by user: {}", id, currentUser.getEmail());

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        // Vérification que l'utilisateur est le propriétaire
        if (!note.getOwner().equals(currentUser)) {
            logger.warn("Unauthorized delete attempt to note {} by user {}", id, currentUser.getEmail());
            throw new UnauthorizedAccessException(id, "note", "Vous ne pouvez supprimer que vos propres notes");
        }

        noteRepository.delete(note);
        logger.info("Note deleted successfully with ID: {}", id);
    }

    // Recherche et filtrage des notes
    public Page<NoteDto> searchNotes(String query, String tag, NoteVisibility visibility, 
                                   User currentUser, Pageable pageable) {
        logger.info("Searching notes with query: {}, tag: {}, visibility: {} for user: {}", 
                   query, tag, visibility, currentUser.getEmail());

        Page<Note> notes;

        if (query != null && !query.trim().isEmpty()) {
            // Recherche par texte
            if (visibility == NoteVisibility.PUBLIC) {
                notes = noteRepository.findPublicNotesBySearchTerm(query, pageable);
            } else {
                notes = noteRepository.findByOwnerAndSearchTerm(currentUser, query, pageable);
            }
        } else if (tag != null && !tag.trim().isEmpty()) {
            // Recherche par tag
            notes = noteRepository.findByOwnerIdAndTagLabel(currentUser.getId(), tag, pageable);
        } else if (visibility != null) {
            // Filtrage par visibilité
            if (visibility == NoteVisibility.PUBLIC) {
                notes = noteRepository.findByVisibility(visibility, pageable);
            } else {
                notes = noteRepository.findByOwnerAndVisibility(currentUser, visibility, pageable);
            }
        } else {
            // Récupération de toutes les notes accessibles
            notes = noteRepository.findAccessibleByUser(currentUser.getId(), pageable);
        }

        return notes.map(noteMapper::toDto);
    }

    // Récupération des notes favorites
    public Page<NoteDto> getFavoriteNotes(User currentUser, Pageable pageable) {
        logger.info("Getting favorite notes for user: {}", currentUser.getEmail());

        Page<Note> notes = noteRepository.findFavoriteNotesByOwner(currentUser.getId(), pageable);
        return notes.map(noteMapper::toDto);
    }

    // Récupération des notes partagées avec l'utilisateur
    public Page<NoteDto> getSharedNotes(User currentUser, Pageable pageable) {
        logger.info("Getting shared notes for user: {}", currentUser.getEmail());

        Page<Note> notes = noteRepository.findSharedWithUser(currentUser.getId(), pageable);
        return notes.map(noteMapper::toDto);
    }

    // Récupération des notes publiques
    public Page<NoteDto> getPublicNotes(Pageable pageable) {
        logger.info("Getting public notes");

        Page<Note> notes = noteRepository.findByVisibility(NoteVisibility.PUBLIC, pageable);
        return notes.map(noteMapper::toDto);
    }

    // Toggle du statut favori
    @Transactional
    public NoteDto toggleFavorite(Long id, User currentUser) {
        logger.info("Toggling favorite status for note {} by user {}", id, currentUser.getEmail());

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        // Vérification des permissions d'accès
        if (!note.isAccessibleBy(currentUser)) {
            throw new UnauthorizedAccessException(id, "note", "Vous n'avez pas l'autorisation d'accéder à cette note");
        }

        note.toggleFavorite();
        Note updatedNote = noteRepository.save(note);

        logger.info("Favorite status toggled for note {}", id);
        return noteMapper.toDto(updatedNote);
    }

    // Récupération des notes par propriétaire
    public Page<NoteDto> getNotesByOwner(User owner, Pageable pageable) {
        logger.info("Getting notes by owner: {}", owner.getEmail());

        Page<Note> notes = noteRepository.findByOwner(owner, pageable);
        return notes.map(noteMapper::toDto);
    }

    // Méthodes utilitaires
    private Tag getOrCreateTag(String tagLabel) {
        return tagRepository.findByLabel(tagLabel)
                .orElseGet(() -> {
                    Tag newTag = new Tag(tagLabel);
                    return tagRepository.save(newTag);
                });
    }

    // Vérification de l'existence d'une note
    public boolean noteExists(Long id) {
        return noteRepository.existsById(id);
    }

    // Vérification des permissions d'accès
    public boolean hasAccess(Long noteId, User user) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return false;
        }
        return noteOpt.get().isAccessibleBy(user);
    }

    // Récupération d'une note sans vérification d'accès (pour usage interne)
    public Optional<Note> findNoteById(Long id) {
        return noteRepository.findById(id);
    }
}