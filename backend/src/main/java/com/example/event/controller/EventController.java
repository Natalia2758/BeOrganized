package com.example.event.controller;

import com.example.event.service.EventService;

import jakarta.validation.Valid;

import com.example.event.dto.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;



/**
 * 
 * <b>EventController</b>
 * <br>
 * REST controller responsible for communication and event data exchange with the frontend side of application
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Receives filtering criteria and responds with filtered user events
     * @param filter    a DTO representing criteria to filter events
     * @return          a list of filtered events mapped to response DTOs
     */
    @GetMapping
    public List<EventResponse> getEvents(@ModelAttribute EventFilter filter) {
        return eventService.getFilteredEvents(filter);
    }

    /**
     * Sends response with an event data by the provided ID
     * @param idEvent   an ID of the requested event
     * @return          a response DTO with an event data
     */
    @GetMapping("/{idEvent}")
    public EventResponse getEvent(@PathVariable("idEvent") Long idEvent) {
        return eventService.getEvent(idEvent);
    }

    /**
     * Receives a request DTO with data of an event to be created
     * @param request   a request DTO with event data provided by the user
     * @return          a response DTO with a created event
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventCreateRequest request) {
        return eventService.createEvent(request);
    }

    /**
     * Receives a shortened version of a request DTO with data of an event to be created
     * @param request   a request DTO with event data provided by the user
     * @return          a response DTO with a created event
     */
    @PostMapping("/create-quick")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEventQuick(@Valid @RequestBody QuickEventCreateRequest request) {
        return eventService.createEventQuick(request);
    }
    
    /**
     * Receives a request DTO with modified data of an existing event
     * @param idEvent   an ID of the event to be modified
     * @param request   a request DTO with the data to be changed
     * @return          a response DTO with modified event data
     */
    @PutMapping("/{idEvent}")
    public EventResponse updateEvent(@PathVariable("idEvent") Long idEvent, @RequestBody EventCreateRequest request) {    
        return eventService.updateEvent(idEvent, request);
    }
    
    /**
     * Receives a request to delete an event
     * @param idEvent an ID of the event to be deleted
     */
    @DeleteMapping("/{idEvent}")
    public void deleteEvent(@PathVariable("idEvent") Long idEvent) {
        eventService.deleteEvent(idEvent);
    }

    /**
     * Sends all events filtered according to the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return      a list of response DTOs representing filtered events
     */
    @GetMapping("/by-date")
    public List<EventResponse> getEventsByDate(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("day") int day) {
        return eventService.getEventsByDate(year, month, day);
    }

    /**
     * Sends all events whose start date is in future
     * @return  a list of response DTOs representing future events
     */
    @GetMapping("/future")
    public List<EventResponse> getEventsInFuture() {
        return eventService.getEventsInFuture();
    }
    
    
}
