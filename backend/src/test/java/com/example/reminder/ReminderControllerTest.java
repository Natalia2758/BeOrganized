package com.example.reminder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.reminder.controller.ReminderController;
import com.example.reminder.dto.ReminderCreationRequest;
import com.example.reminder.dto.ReminderResponse;
import com.example.reminder.enums.ReminderStatus;
import com.example.reminder.service.ReminderService;

import org.mockito.ArgumentCaptor;

@WebMvcTest(ReminderController.class)
public class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;

    @Test
    void getRemindersReturnsReminderList() throws Exception {

        ReminderResponse reminder = mock(ReminderResponse.class);

        when(reminderService.getAllReminders()).thenReturn(List.of(reminder));

        mockMvc.perform(get("/api/reminders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(reminderService).getAllReminders();
    }

    @Test
    void getReminderReturnsReminder() throws Exception {

        Long idReminder = 1L;
        ReminderResponse reminder = mock(ReminderResponse.class);

        when(reminderService.getReminder(idReminder)).thenReturn(reminder);

        mockMvc.perform(get("/api/reminders/{idReminder}", idReminder))
            .andExpect(status().isOk());

        verify(reminderService).getReminder(idReminder);
    }

    @Test
    void getReminderReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(get("/api/reminders/{idReminder}", "abc"))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).getReminder(anyLong());
    }

    @Test
    void createReminderReturnsCreatedStatusWhenValidRequest() throws Exception {

        when(reminderService.createRemindersList(any(ReminderCreationRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/reminders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "content": "Reminder",
                        "type": "NONE",
                        "remindDate": "2026-10-20",
                        "remindTime": "10:00"
                    }
                """))
            .andExpect(status().isCreated());

        verify(reminderService).createRemindersList(any(ReminderCreationRequest.class));
    }

    @Test
    void createReminderReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        mockMvc.perform(post("/api/reminders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).createRemindersList(any(ReminderCreationRequest.class));
    }


    @Test
    void createReminderPassesCorrectRequestToService() throws Exception {

        when(reminderService.createRemindersList(any(ReminderCreationRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/reminders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "content": "Reminder",
                        "type": "NONE",
                        "remindDate": "2026-10-20",
                        "remindTime": "10:00"
                    }
                """))
            .andExpect(status().isCreated());

        ArgumentCaptor<ReminderCreationRequest> captor = ArgumentCaptor.forClass(ReminderCreationRequest.class);

        verify(reminderService).createRemindersList(captor.capture());

        ReminderCreationRequest request = captor.getValue();

        assertEquals("Reminder", request.content());
        assertEquals("2026-10-20", request.remindDate());
        assertEquals("10:00", request.remindTime());
    }


    @Test
    void updateReminderReturnsReminderWhenValidRequest() throws Exception {

        Long idReminder = 1L;
        ReminderResponse response = mock(ReminderResponse.class);

        when(reminderService.updateReminder(eq(idReminder), any(ReminderCreationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/reminders/{idReminder}", idReminder)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "content": "Updated reminder",
                        "type": "NONE",
                        "remindDate": "2026-10-20",
                        "remindTime": "10:00"
                    }
                """))
            .andExpect(status().isOk());

        verify(reminderService).updateReminder(eq(idReminder), any(ReminderCreationRequest.class));
    }


    @Test
    void updateReminderPassesCorrectRequestToService() throws Exception {

        Long idReminder = 1L;

        when(reminderService.updateReminder(eq(idReminder), any(ReminderCreationRequest.class))).thenReturn(mock(ReminderResponse.class));

        mockMvc.perform(put("/api/reminders/{idReminder}", idReminder)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "content": "Updated reminder",
                        "type": "NONE",
                        "remindDate": "2026-10-20",
                        "remindTime": "10:00"
                    }
                """))
            .andExpect(status().isOk());

        ArgumentCaptor<ReminderCreationRequest> captor = ArgumentCaptor.forClass(ReminderCreationRequest.class);

        verify(reminderService).updateReminder(eq(idReminder), captor.capture());

        ReminderCreationRequest request = captor.getValue();

        assertEquals("Updated reminder", request.content());
    }


    @Test
    void updateReminderReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        mockMvc.perform(put("/api/reminders/{idReminder}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "content": ""
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).updateReminder(anyLong(), any(ReminderCreationRequest.class));
    }


    @Test
    void updateReminderReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(put("/api/reminders/{idReminder}", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "content": "Updated reminder"
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).updateReminder(anyLong(), any(ReminderCreationRequest.class));
    }


    @Test
    void deleteReminderReturnsNoContentWhenSuccessfulRequest() throws Exception {

        Long idReminder = 1L;

        mockMvc.perform(delete("/api/reminders/{idReminder}", idReminder))
            .andExpect(status().isNoContent());

        verify(reminderService).deleteReminder(idReminder);
    }


    @Test
    void deleteReminderReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(delete("/api/reminders/{idReminder}", "abc"))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).deleteReminder(anyLong());
    }


    @Test
    void changeStatusReturnsReminderWhenSuccessfulRequest() throws Exception {

        Long idReminder = 1L;
        ReminderResponse response = mock(ReminderResponse.class);

        when(reminderService.changeReminderStatus(eq(idReminder), any())).thenReturn(response);

        mockMvc.perform(patch("/api/reminders/{idReminder}/status", idReminder)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "SENT"
                    }
                """))
            .andExpect(status().isOk());

        verify(reminderService).changeReminderStatus(eq(idReminder), any());
    }


    @Test
    void changeStatusPassesCorrectStatusToService() throws Exception {

        Long idReminder = 1L;
        ReminderResponse response = mock(ReminderResponse.class);

        when(reminderService.changeReminderStatus(eq(idReminder), any())).thenReturn(response);

        mockMvc.perform(patch("/api/reminders/{idReminder}/status", idReminder)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "SENT"
                    }
                """))
            .andExpect(status().isOk());

        
        verify(reminderService).changeReminderStatus(idReminder, ReminderStatus.SENT);
    }


    @Test
    void changeStatusReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(patch("/api/reminders/{idReminder}/status", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "SENT"
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).changeReminderStatus(anyLong(), any());
    }


    @Test
    void getRemindersByDateReturnsReminderListWhenSuccessfulRequest() throws Exception {

        ReminderResponse reminder = mock(ReminderResponse.class);

        when(reminderService.getRemindersByDate(anyInt(), anyInt(), anyInt())).thenReturn(List.of(reminder));

        mockMvc.perform(get("/api/reminders/by-date")
                .param("year", "2026")
                .param("month", "10")
                .param("day", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(reminderService).getRemindersByDate(2026, 10, 20);
    }


    @Test
    void getRemindersByDateReturnsBadRequestWhenMissingParam() throws Exception {

        mockMvc.perform(get("/api/reminders/by-date")
                .param("year", "2026")
                .param("month", "10"))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).getRemindersByDate(anyInt(), anyInt(), anyInt());
    }


    @Test
    void getRemindersByDateReturnsBadRequestWhenParamIsInvalid() throws Exception {

        mockMvc.perform(get("/api/reminders/by-date")
                .param("year", "abc")
                .param("month", "10")
                .param("day", "20"))
            .andExpect(status().isBadRequest());

        verify(reminderService, never()).getRemindersByDate(anyInt(), anyInt(), anyInt());
    }


    @Test
    void getSentRemindersReturnsReminderList() throws Exception {

        ReminderResponse reminder = mock(ReminderResponse.class);

        when(reminderService.getSentReminders()).thenReturn(List.of(reminder));

        mockMvc.perform(get("/api/reminders/sent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(reminderService).getSentReminders();
    }
}

