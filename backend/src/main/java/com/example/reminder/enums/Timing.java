package com.example.reminder.enums;

/**
 * 
 * <b>Timing</b><br>
 * 
 * An enumeration representing option of reminder timing relative to the event/task. For reminders not assigned to task/event, SET_MANUALLY option is chosen.
 */
public enum Timing {
    AT_TIME,
    QUARTER_BEFORE,
    HOUR_BEFORE,
    DAY_BEFORE,
    SET_MANUALLY
}
