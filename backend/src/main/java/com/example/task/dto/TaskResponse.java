package com.example.task.dto;

import com.example.category.dto.CategoryResponse;
import com.example.common.enums.RepeatedFrequency;
import com.example.task.enums.Priority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 
 * <b>TaskResponse</b><br>
 * 
 * A DTO record used to send task data to the frontend.
 * 
 * @param idTask            database ID of the task
 * @param title             title of the task
 * @param description       task description
 * @param priority          task priority level
 * @param category          category DTO object with category data to which the task is assigned
 * @param startDate         date of the task
 * @param duration          boolean value that determines whether the task has specific time
 * @param timeStart         time when the task begins
 * @param timeEnd           time when the task ends
 * @param repeated          boolean value set to true if the task is repeated in time
 * @param repeatedFrequency enumeration representing the frequency with which the task is repeated in time
 * @param dates             all dates when a task is repeated until the end of year
 * @param progress          task progress (percentage value)
 * @param finished          boolean value indicating if the task has finished status or not
 * @param subtasks          a list of task subtasks' DTOs
 */
public record TaskResponse(
    Long idTask,
    String title,
    String description,
    Priority priority,
    CategoryResponse category,
    LocalDate startDate,
    boolean duration,
    LocalTime timeStart,
    LocalTime timeEnd,
    boolean repeated,
    RepeatedFrequency repeatedFrequency,
    List<LocalDate> dates,
    long progress,
    boolean finished,
    List<SubtaskResponse> subtasks
) {

}
