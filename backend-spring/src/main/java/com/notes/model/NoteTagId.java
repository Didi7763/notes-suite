package com.notes.model;

import java.io.Serializable;
import java.util.Objects;

public class NoteTagId implements Serializable {

    private Long note;
    private Long tag;

    // Constructors
    public NoteTagId() {}

    public NoteTagId(Long noteId, Long tagId) {
        this.note = noteId;
        this.tag = tagId;
    }

    // Getters and Setters
    public Long getNote() { return note; }
    public void setNote(Long note) { this.note = note; }

    public Long getTag() { return tag; }
    public void setTag(Long tag) { this.tag = tag; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteTagId noteTagId = (NoteTagId) o;
        return Objects.equals(note, noteTagId.note) &&
               Objects.equals(tag, noteTagId.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, tag);
    }
}