package com.example.note.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 
 * <b>NoteCreateRequest</b><br>
 * A DTO record that transfers note data provided by the user during creation and update.
 * 
 * @param title title of the note
 * @param content actual note content
 * @param date date of the note
 * @param idCategory ID of the category to which the note is assigned
 */
public record NoteCreateRequest(
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,
    @NotBlank(message = "Note content is required")
    String content,
    @FutureOrPresent(message = "Date should be in the future or presence")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    Long idCategory

) {

}
