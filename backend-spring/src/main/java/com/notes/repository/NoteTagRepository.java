package com.notes.repository;

import com.notes.model.NoteTag;
import com.notes.model.NoteTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteTagRepository extends JpaRepository<NoteTag, NoteTagId> {
    
    List<NoteTag> findByNoteId(Long noteId);
    
    List<NoteTag> findByTagId(Long tagId);
    
    void deleteByNoteId(Long noteId);
    
    void deleteByTagId(Long tagId);
    
    @Query("SELECT nt FROM NoteTag nt WHERE nt.note.id = :noteId AND nt.tag.id = :tagId")
    Optional<NoteTag> findByNoteIdAndTagId(@Param("noteId") Long noteId, @Param("tagId") Long tagId);
}

