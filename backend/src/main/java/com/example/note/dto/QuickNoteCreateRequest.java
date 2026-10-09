package com.example.note.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 
 * <b>QuickNoteCreateRequest</b><br>
 * 
 * A DTO record that transfers note data provided by the user through the quick addition form.
 * @param title title of the note
 * @param content actual note content
 */
public record QuickNoteCreateRequest(
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,
    @NotBlank(message = "Note content is required")
    String content
) {}
