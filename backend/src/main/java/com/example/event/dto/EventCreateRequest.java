package com.example.event.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.common.enums.RepeatedFrequency;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 
 * <b>EventCreateRequest</b><br>
 * A DTO record that transfers event data provided by the user during event creation and update.
 * 
 * @param title             title of the event
 * @param details           details about the event
 * @param idCategory        ID of the category to which the event is assigned
 * @param dateStart         date when the event starts
 * @param dateEnd           date when the event ends
 * @param duration          boolean value that determines if the event has specific time
 * @param timeStart         time when the event starts
 * @param timeEnd           time when the event ends
 * @param repeated          boolean value set to true if the event is repeated in time
 * @param repeatedFrequency enumeration representing the frequency with which the event is repeated in time
 */
public record EventCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @Size(max = 1200, message = "Details exceed the maximum length")
    String details,

    Long idCategory,

    @FutureOrPresent(message = "Start date must be in the presence or future")
    @NotNull(message = "Start date must be provided")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateStart,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateEnd,

    boolean duration, 

    @JsonFormat(pattern = "HH:mm")
    LocalTime timeStart,

    @JsonFormat(pattern = "HH:mm")
    LocalTime timeEnd,
    boolean repeated,
    RepeatedFrequency repeatedFrequency
) {

    @AssertTrue(message = "End date of event must be the same or after the start date")
    public boolean isEndDateAfterStart() {
        return dateStart() == null || dateEnd() == null || !dateStart().isAfter(dateEnd());
    }

    @AssertTrue(message = "End time of event must be after the start time")
    public boolean isEndTimeAfterStart() {
        if (timeStart() == null || timeEnd() == null) {
            return true;
        }

        if (dateStart() == null || dateEnd() == null) {
            return true;
        }

        if (dateStart().isEqual(dateEnd())) {
            return timeEnd().isAfter(timeStart());
        }

        return true;
    }
}
