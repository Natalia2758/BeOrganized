package com.example.reminder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.reminder.controller.ReminderSseController;
import com.example.reminder.service.ReminderSseService;
import com.example.user.service.CurrentUserService;

@WebMvcTest(ReminderSseController.class)
public class ReminderSseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderSseService reminderSseService;

    @MockBean
    private CurrentUserService userService;


    @Test
    void streamReturnsSseStreamWhenSuccessfulRequest() throws Exception {

        Long idUser = 1L;
        SseEmitter emitter = new SseEmitter();

        when(userService.getCurrentUserId()).thenReturn(idUser);
        when(reminderSseService.connect(idUser)).thenReturn(emitter);

        mockMvc.perform(get("/api/reminders/stream")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted());

        verify(userService).getCurrentUserId();
        verify(reminderSseService).connect(idUser);
    }


    @Test
    void streamConnectsUsingCurrentUserId() throws Exception {

        Long idUser = 10L;
        SseEmitter emitter = new SseEmitter();

        when(userService.getCurrentUserId()).thenReturn(idUser);
        when(reminderSseService.connect(idUser)).thenReturn(emitter);

        mockMvc.perform(get("/api/reminders/stream")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted());

        verify(userService).getCurrentUserId();
        verify(reminderSseService).connect(idUser);
    }
}

