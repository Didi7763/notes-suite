package com.notes.mapper;

import com.notes.dto.tag.TagDto;
import com.notes.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TagMapper {
    
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);
    
    TagDto toDto(Tag tag);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "noteTags", ignore = true)
    Tag toEntity(TagDto tagDto);
}
