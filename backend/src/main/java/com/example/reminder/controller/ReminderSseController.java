package com.example.reminder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.reminder.service.ReminderSseService;
import com.example.user.service.CurrentUserService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 
 * <b>ReminderSseController</b>
 * <br>
 * REST controller responsible for sending reminder SSE
 */
@RestController 
@RequestMapping("/api/reminders")
public class ReminderSseController {


    private final ReminderSseService reminderSseService;
    private final CurrentUserService userService;

    public ReminderSseController(ReminderSseService reminderSseService, CurrentUserService userService) {
        this.reminderSseService = reminderSseService;
        this.userService = userService;
    }

    /**
     *  Sends data stream to the frontend and keeps the connection open
     * @return an instance of a Server-Side-Events emitter.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        Long idUser = userService.getCurrentUserId();
        return reminderSseService.connect(idUser);
    }
    
}
