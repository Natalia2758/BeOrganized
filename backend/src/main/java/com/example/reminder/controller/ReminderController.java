package com.example.reminder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.reminder.dto.ReminderCreationRequest;
import com.example.reminder.dto.ReminderResponse;
import com.example.reminder.dto.ReminderStatusChangeRequest;
import com.example.reminder.service.ReminderService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * <b>ReminderController</b>
 * <br>
 * REST controller responsible for communication and reminder data exchange with the frontend side of application
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * Sends response with all user reminders to the frontend
     * @return a list of reminder response DTOs
     */
    @GetMapping
    public List<ReminderResponse> getReminders() {
        return reminderService.getAllReminders();
    }

    /**
     * Sends response with a reminder data by the provided ID
     * @param idReminder   an ID of the requested reminder
     * @return          a response DTO with a reminder data
     */
    @GetMapping("/{idReminder}")
    public ReminderResponse getReminder(@PathVariable("idReminder") Long idReminder) {
        return reminderService.getReminder(idReminder);
    }

    /**
     * Receives a request DTO with data of a reminder to be created
     * @param request   a request DTO with reminder data provided by the user
     * @return          a response DTO with a created reminder
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ReminderResponse> createReminder(@Valid @RequestBody ReminderCreationRequest request) {
        List<ReminderResponse> reminders = reminderService.createRemindersList(request);
        return reminders;
    }

    /**
     * Receives a request DTO with modified data of an existing reminder
     * @param idReminder   an ID of the reminder to be modified
     * @param request   a request DTO with the data to be changed
     * @return          a response DTO with modified reminder data
     */
    @PutMapping("/{idReminder}")
    public ReminderResponse updateReminder(@PathVariable("idReminder") Long idReminder, @Valid @RequestBody ReminderCreationRequest request) {
        return reminderService.updateReminder(idReminder, request);
    }
    
    /**
     * Receives a request to delete a reminder
     * @param idReminder an ID of the reminder to be deleted
     */
    @DeleteMapping("/{idReminder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReminder(@PathVariable("idReminder") Long idReminder) {
        reminderService.deleteReminder(idReminder);
    }

    /**
     * Receives a request to change a reminder status
     * @param idReminder    an ID of the reminder
     * @param request       a request with changed status
     * @return              a response DTO of reminder with changed status
     */
    @PatchMapping("/{idReminder}/status")
    public ReminderResponse changeStatus(@PathVariable("idReminder") Long idReminder, @RequestBody ReminderStatusChangeRequest request) {
        return reminderService.changeReminderStatus(idReminder, request.status());
    }

    /**
     * Sends all reminders filtered according to the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return      a list of response DTOs representing filtered reminders
     */
    @GetMapping("/by-date")
    public List<ReminderResponse> getRemindersByDate(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("day") int day) {
        return reminderService.getRemindersByDate(year, month, day);
    }

    /**
     * Sends all reminders that have a sent status
     * @return a list of sent reminders
     */
    @GetMapping("/sent")
    public List<ReminderResponse> getSentReminders() {
        return reminderService.getSentReminders();
    }
    
}
