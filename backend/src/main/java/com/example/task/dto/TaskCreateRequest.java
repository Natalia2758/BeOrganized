package com.example.task.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.common.enums.RepeatedFrequency;
import com.example.task.enums.Priority;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 
 * <b>TaskCreateRequest</b><br>
 * 
 * A DTO record that transfers task data provided by the user during task creation and update.
 * 
 * @param title             title of the task
 * @param description       task description
 * @param priority          task priority level
 * @param idCategory        ID of the category to which the task is assigned
 * @param startDate         date of the task
 * @param duration          boolean value that determines if the task has specific time
 * @param timeStart         time when the event starts
 * @param timeEnd           time when the event ends
 * @param repeated          boolean value set to true if the event is repeated in time
 * @param repeatedFrequency enumeration representing the frequency with which the event is repeated in time
 * @param subtasks          a list of subtask titles
 */
public record TaskCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @Size(max = 1200, message = "Description exceeds the maximum length")
    String description,
    Priority priority,
    Long idCategory,

    @FutureOrPresent(message = "Date should be in the future or presence")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    boolean duration,

    @JsonFormat(pattern = "HH:mm")
    LocalTime timeStart,

    @JsonFormat(pattern = "HH:mm")
    LocalTime timeEnd,
    
    boolean repeated,
    RepeatedFrequency repeatedFrequency,

    List<String> subtasks
) {

    @AssertTrue(message = "Time of task end must be after the start time")
    public boolean isEndAfterStart() {
        return timeStart() == null || timeEnd() == null || timeStart().isBefore(timeEnd());
    }

}
