package com.example.note.dto;

/**
 * 
 * <b>NoteFilter</b><br>
 * A DTO record used to transfer parameters to filter notes
 * 
 * @param idCategory ID of the selected category
 */
public record NoteFilter(
    Long idCategory
) {}
