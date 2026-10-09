package com.example.note.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.config.MappingConfig;
import com.example.note.dto.NoteResponse;
import com.example.note.model.Note;

/**
 * 
 * <b>NoteMapper</b>
 * <br>
 * Mapper interface responsible for mapping Note objects to DTOs
 */
@Mapper(
    config = MappingConfig.class
)
public interface NoteMapper {
    NoteResponse toResponse(Note note);
    List<NoteResponse> toResponseList(List<Note> notes);
}
