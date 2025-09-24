package com.notes.mapper;

import com.notes.dto.note.NoteCreateDto;
import com.notes.dto.note.NoteDto;
import com.notes.dto.note.NoteUpdateDto;
import com.notes.model.Note;
import com.notes.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, TagMapper.class, PublicLinkMapper.class})
public interface NoteMapper {
    NoteMapper INSTANCE = Mappers.getMapper(NoteMapper.class);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerEmail", source = "owner.email")
    NoteDto toDto(Note note);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "isFavorite", ignore = true)
    @Mapping(target = "noteTags", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(target = "publicLinks", ignore = true)
    Note toEntity(NoteCreateDto noteCreateDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "isFavorite", ignore = true)
    @Mapping(target = "noteTags", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(target = "publicLinks", ignore = true)
    void updateEntity(NoteUpdateDto noteUpdateDto, @MappingTarget Note note);

    // Méthodes de mapping pour les tags
    default List<Tag> mapTags(List<String> tagLabels) {
        if (tagLabels == null) {
            return null;
        }
        return tagLabels.stream()
                .map(label -> {
                    Tag tag = new Tag();
                    tag.setLabel(label);
                    return tag;
                })
                .toList();
    }
}