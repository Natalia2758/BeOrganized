package com.example.note.dto;

import java.time.LocalDate;

import com.example.category.dto.CategoryResponse;

/**
 * 
 * <b>NoteResponse</b><br>
 * A DTO record used to send note data to frontend.
 * 
 * @param idNote note ID from the database
 * @param title title of the note
 * @param content content of the note
 * @param date date of the note
 * @param category category object to which the note is assigned
 */
public record NoteResponse(
    Long idNote,
    String title,
    String content,
    LocalDate date,
    CategoryResponse category
) {

}
