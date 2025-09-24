package com.notes.mapper;

import com.notes.dto.share.ShareDto;
import com.notes.model.Share;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShareMapper {
    ShareMapper INSTANCE = Mappers.getMapper(ShareMapper.class);

    @Mapping(target = "noteId", source = "note.id")
    @Mapping(target = "noteTitle", source = "note.title")
    @Mapping(target = "sharedWithUserId", source = "sharedWithUser.id")
    @Mapping(target = "sharedWithUserEmail", source = "sharedWithUser.email")
    ShareDto toDto(Share share);
}


