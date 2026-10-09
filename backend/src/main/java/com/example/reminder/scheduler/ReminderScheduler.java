package com.example.reminder.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.reminder.service.ReminderService;

/**
 * 
 * <b>ReminderScheduler</b><br>
 * 
 * A component that automatically calls ReminderService method that fetches due reminders every 60 seconds.
 */
@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // Scheduler checks reminders every minute (60,000 milliseconds)
    @Scheduled(fixedRate = 60_000)
    public void checkReminders() {
        reminderService.processDueReminders();
    }
}
