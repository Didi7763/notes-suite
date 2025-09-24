package com.notes.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "note_tags")
@IdClass(NoteTagId.class)
public class NoteTag implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // Constructors
    public NoteTag() {}

    public NoteTag(Note note, Tag tag) {
        this.note = note;
        this.tag = tag;
    }

    // Getters and Setters
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteTag noteTag = (NoteTag) o;
        return Objects.equals(note, noteTag.note) &&
               Objects.equals(tag, noteTag.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, tag);
    }
}



