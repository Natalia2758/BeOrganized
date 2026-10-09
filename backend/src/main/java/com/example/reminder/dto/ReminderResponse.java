package com.example.reminder.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.reminder.enums.ReminderStatus;
import com.example.reminder.enums.ReminderType;
import com.example.reminder.enums.Timing;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 
 * <b>ReminderResponse</b>
 * <br>
 * 
 * A DTO record used to send reminder data to the frontend.
 * 
 * @param idReminder    database ID of the reminder
 * @param content       reminder content (a title of the reminder)
 * @param type          type of the reminder
 * @param idEvent       ID of the event (if the reminder is assigned to the event)
 * @param idTask        ID of the task (if the reminder is assigned to the task)
 * @param repeatForAll  true if the reminder is to be assigned to all event or task dates
 * @param remindAt      timing option indicating the relative time of the reminder to the venet/task
 * @param remindDate    date of the reminder
 * @param remindTime    time of the reminder
 * @param sentTime      time when the reminder was sent
 * @param status        status of the reminder
 */
public record ReminderResponse(
    Long idReminder,
    String content,
    ReminderType type,
    Long idEvent,
    Long idTask,
    boolean repeatForAll,
    Timing remindAt,
    LocalDate remindDate,
    LocalTime remindTime,
    @JsonFormat(pattern = "hh:mm")
    LocalDateTime sentTime,
    ReminderStatus status
) {

}
