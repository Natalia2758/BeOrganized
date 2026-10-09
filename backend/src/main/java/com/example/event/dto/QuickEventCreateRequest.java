package com.example.event.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

/**
 * 
 * <b>QuickEventCreateRequest</b><br>
 * A DTO record that transfers event data provided by the user through the quick addition form.
 * 
 * @param title     title of the event
 * @param dateStart date when the event starts
 * @param dateEnd   date when the event ends
 */
public record QuickEventCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @FutureOrPresent(message = "Start date must be in the presence or future")
    @NotNull(message = "Start date must be provided")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateStart,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateEnd
) {
    @AssertTrue(message = "End date of event must be the same or after the start date")
    public boolean isEndDateAfterStart() {
        return dateStart() == null || dateEnd() == null || !dateStart().isAfter(dateEnd());
    }

}
