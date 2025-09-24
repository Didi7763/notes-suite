package com.notes.mapper;

import com.notes.dto.publiclink.PublicLinkDto;
import com.notes.model.PublicLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PublicLinkMapper {
    PublicLinkMapper INSTANCE = Mappers.getMapper(PublicLinkMapper.class);

    @Mapping(target = "noteId", source = "note.id")
    @Mapping(target = "noteTitle", source = "note.title")
    @Mapping(target = "isPasswordProtected", expression = "java(publicLink.isPasswordProtected())")
    PublicLinkDto toDto(PublicLink publicLink);
}
