package com.example.task.dto;

import java.time.LocalDate;

import com.example.task.enums.Priority;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 
 * <b>QuickTaskCreateRequest</b><br>
 * 
 * A DTO record that transfers task data provided by the user through the quick addition form.
 * 
 * @param title     title of the task
 * @param priority  priority level of the task
 * @param startDate date of the task
 */
public record QuickTaskCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,
    
    Priority priority,

    @FutureOrPresent(message = "Date should be in the future or presence")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate
) {

}
