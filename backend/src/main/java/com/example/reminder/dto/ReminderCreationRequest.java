package com.example.reminder.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.reminder.enums.ReminderType;
import com.example.reminder.enums.Timing;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 
 * <b>ReminderCreationRequest</b>
 * <br>
 * 
 * A DTO record that transfers reminder data provided by the user during reminder creation and update.
 * 
 * @param content       reminder content (a title of the reminder)
 * @param type          type of the reminder
 * @param idEvent       ID of the event (if the reminder is assigned to the event)
 * @param idTask        ID of the task (if the reminder is assigned to the task)
 * @param repeatForAll  true if the reminder is to be assigned to all event or task dates
 * @param remindAt      timing option indicating the relative time of the reminder to the venet/task
 * @param remindDate    date of the reminder
 * @param remindTime    time of the reminder
 */
public record ReminderCreationRequest(

    @NotBlank(message = "Reminder content section is required")
    String content,

    @NotNull(message = "Reminder type is required")
    ReminderType type,

    Long idEvent,
    Long idTask,
    boolean repeatForAll,
    Timing remindAt,

    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate remindDate,

    @JsonFormat(pattern = "HH:mm")
    LocalTime remindTime
) {
    @AssertTrue(message = "Please choose event")
    public boolean isEventChosen() {
        return type != ReminderType.EVENT || idEvent() != null;
    }

    @AssertTrue(message = "Please choose task")
    public boolean isTaskChosen() {
        return type != ReminderType.TASK || idTask() != null;
    }

    @AssertTrue(message = "Please choose date and time")
    public boolean isDateAndTimeProvided() {
        return remindAt != Timing.SET_MANUALLY || (remindDate != null && remindTime != null);
    }

    @AssertTrue(message = "Please choose time in the future")
    public boolean isChosenTimeInFutureToday() {
        return remindDate == null
        || remindTime == null
        || remindDate.isAfter(LocalDate.now())
        || (remindDate.isEqual(LocalDate.now()) && remindTime.isAfter(LocalTime.now()));
    }

}
