package com.example.reminder.dto;

import com.example.reminder.enums.ReminderStatus;

/**
 * <b>ReminderStatusChangeRequest</b>
 * <br>
 * A DTO record used to transfer status data when user changes reminder status 
 * 
 * @param idReminder    an ID of the reminder whose status was changed
 * @param status        a status to which reminder should be changed
 */
public record ReminderStatusChangeRequest(
    Long idReminder,
    ReminderStatus status
) {

}
