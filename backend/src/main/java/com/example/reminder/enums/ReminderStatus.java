package com.example.reminder.enums;

/**
 * 
 * <b>ReminderStatus</b><br>
 * 
 * An enumeration representing reminder current status.
 */
public enum ReminderStatus {
    PENDING,    // Reminder was created (default)
    TRIGGERED,  // Reminder was triggered to be sent (by scheduler)
    SENT,       // Remider was successfully sent to the user
    OPENED,     // Notification corresponding to the reminder was opened
    CANCELLED   // Reminder was cancelled by the user
}
