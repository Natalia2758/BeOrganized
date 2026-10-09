package com.example.event;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.event.controller.EventController;
import com.example.event.dto.EventCreateRequest;
import com.example.event.dto.EventFilter;
import com.example.event.dto.EventResponse;
import com.example.event.dto.QuickEventCreateRequest;
import com.example.event.service.EventService;

@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    void getEventsReturnsEventList() throws Exception {

        EventResponse event = mock(EventResponse.class);

        when(eventService.getFilteredEvents(any(EventFilter.class))).thenReturn(List.of(event));

        mockMvc.perform(get("/api/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(eventService).getFilteredEvents(any(EventFilter.class));
    }

    @Test
    void getEventsReturnsEventListWhenFiltersProvided() throws Exception {

        when(eventService.getFilteredEvents(any(EventFilter.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/events")
                .param("idCategory", "2")
                .param("search", "meeting"))
            .andExpect(status().isOk());

        ArgumentCaptor<EventFilter> captor = ArgumentCaptor.forClass(EventFilter.class);

        verify(eventService).getFilteredEvents(captor.capture());

        EventFilter filter = captor.getValue();

        assertEquals(2L, filter.idCategory());
        assertEquals("meeting", filter.search());
    }

    @Test
    void getEventsReturnsBadRequestWhenFilterIsInvalid() throws Exception {

        mockMvc.perform(get("/api/events")
                .param("finished", "abc"))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).getFilteredEvents(any(EventFilter.class));
    }

    @Test
    void getEventReturnsEvent() throws Exception {

        Long idEvent = 1L;
        EventResponse event = mock(EventResponse.class);

        when(eventService.getEvent(idEvent)).thenReturn(event);

        mockMvc.perform(get("/api/events/{idEvent}", idEvent))
            .andExpect(status().isOk());

        verify(eventService).getEvent(idEvent);
    }

    @Test
    void getEventReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(get("/api/events/{idEvent}", "abc"))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).getEvent(anyLong());
    }

    @Test
    void createEventReturnsCreatedStatusWhenValidRequest() throws Exception {

        EventResponse response = mock(EventResponse.class);

        when(eventService.createEvent(any(EventCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/events/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Team meeting",
                        "dateStart": "2026-12-10",
                        "dateEnd": "2026-12-10",
                        "duration": "true",
                        "timeStart": "10:00",
                        "timeEnd": "11:00",
                        "repeated": "false"
                    }
                """))
            .andExpect(status().isCreated());

        verify(eventService).createEvent(any(EventCreateRequest.class));
    }

    @Test
    void createEventPassesCorrectRequestToService() throws Exception {

        when(eventService.createEvent(any(EventCreateRequest.class))).thenReturn(mock(EventResponse.class));

        mockMvc.perform(post("/api/events/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Team meeting",
                        "dateStart": "2026-12-10",
                        "dateEnd": "2026-12-10",
                        "duration": "true",
                        "timeStart": "10:00",
                        "timeEnd": "11:00"
                    }
                """))
            .andExpect(status().isCreated());

        ArgumentCaptor<EventCreateRequest> captor = ArgumentCaptor.forClass(EventCreateRequest.class);

        verify(eventService).createEvent(captor.capture());

        EventCreateRequest request = captor.getValue();

        assertEquals("Team meeting", request.title());
        assertEquals("2026-12-10", request.dateStart());
        assertEquals(true, request.duration());
        assertEquals("10:00", request.timeStart());
    }

    @Test
    void createEventReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        mockMvc.perform(post("/api/events/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "dateStart": "2026-12-10",
                        "dateEnd": "2026-12-10",
                        "duration": "true",
                        "timeStart": "10:00",
                        "timeEnd": "11:00"
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any(EventCreateRequest.class));
    }

    @Test
    void createEventQuickReturnsCreatedStatusWhenValidRequest() throws Exception {

        EventResponse response = mock(EventResponse.class);

        when(eventService.createEventQuick(any(QuickEventCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/events/create-quick")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Meeting",
                        "dateStart": "2026-12-10"
                    }
                """))
            .andExpect(status().isCreated());

        verify(eventService).createEventQuick(any(QuickEventCreateRequest.class));
    }

    @Test
    void createEventQuickReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        mockMvc.perform(post("/api/events/create-quick")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": ""
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).createEventQuick(any(QuickEventCreateRequest.class));
    }

    @Test
    void updateEventReturnsEventWhenValidRequest() throws Exception {

        Long idEvent = 1L;
        EventResponse response = mock(EventResponse.class);

        when(eventService.updateEvent(eq(idEvent), any(EventCreateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/events/{idEvent}", idEvent)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Team meeting",
                        "dateStart": "2026-12-10",
                        "dateEnd": "2026-12-10",
                        "duration": "true",
                        "timeStart": "10:00",
                        "timeEnd": "11:00"
                    }
                """))
            .andExpect(status().isOk());

        verify(eventService).updateEvent(eq(idEvent), any(EventCreateRequest.class));
    }

    @Test
    void updateEventPassesCorrectRequestToService() throws Exception {

        Long idEvent = 1L;

        when(eventService.updateEvent(eq(idEvent), any(EventCreateRequest.class))).thenReturn(mock(EventResponse.class));

        mockMvc.perform(put("/api/events/{idEvent}", idEvent)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Team meeting",
                        "dateStart": "2026-12-10",
                        "dateEnd": "2026-12-10",
                        "duration": "true",
                        "timeStart": "10:00",
                        "timeEnd": "11:00"
                    }
                """))
            .andExpect(status().isOk());

        ArgumentCaptor<EventCreateRequest> captor = ArgumentCaptor.forClass(EventCreateRequest.class);

        verify(eventService).updateEvent(eq(idEvent), captor.capture());

        EventCreateRequest request = captor.getValue();

        assertEquals("Updated meeting", request.title());
        assertEquals("2026-12-10", request.dateEnd());
        assertEquals("11:00", request.timeEnd());
    }

    @Test
    void updateEventReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(put("/api/events/{idEvent}", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Updated meeting",
                        "dateStart": "2026-12-10",
                        "dateEnd": "2026-12-10",
                        "duration": "false",
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEvent(anyLong(), any(EventCreateRequest.class));
    }

    @Test
    void deleteEventReturnsNoContentWhenSuccessfulRequest() throws Exception {

        Long idEvent = 1L;

        mockMvc.perform(delete("/api/events/{idEvent}", idEvent))
            .andExpect(status().isOk());

        verify(eventService).deleteEvent(idEvent);
    }

    @Test
    void deleteEventReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(delete("/api/events/{idEvent}", "abc"))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).deleteEvent(anyLong());
    }

    @Test
    void getEventsByDateReturnsEventListWhenSuccessfulRequest() throws Exception {

        EventResponse event = mock(EventResponse.class);

        when(eventService.getEventsByDate(anyInt(), anyInt(), anyInt())).thenReturn(List.of(event));

        mockMvc.perform(get("/api/events/by-date")
                .param("year", "2026")
                .param("month", "10")
                .param("day", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(eventService).getEventsByDate(2026, 10, 20);
    }

    @Test
    void getEventsByDateReturnsBadRequestWhenMissingParam() throws Exception {

        mockMvc.perform(get("/api/events/by-date")
                .param("year", "2026")
                .param("month", "10"))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).getEventsByDate(anyInt(), anyInt(), anyInt());
    }

    @Test
    void getEventsByDateReturnsBadRequestWhenParamIsInvalid() throws Exception {

        mockMvc.perform(get("/api/events/by-date")
                .param("year", "abc")
                .param("month", "10")
                .param("day", "20"))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).getEventsByDate(anyInt(), anyInt(), anyInt());
    }

    @Test
    void getEventsInFutureReturnsEventList() throws Exception {

        when(eventService.getEventsInFuture()).thenReturn(List.of());

        mockMvc.perform(get("/api/events/future"))
            .andExpect(status().isOk());

        verify(eventService).getEventsInFuture();
    }
}

