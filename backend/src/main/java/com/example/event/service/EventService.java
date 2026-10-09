package com.example.event.service;

import com.example.category.service.CategoryService;
import com.example.event.mapper.EventMapper;
import com.example.event.repository.EventRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;
import com.example.event.dto.EventCreateRequest;
import com.example.event.dto.QuickEventCreateRequest;
import com.example.event.dto.EventFilter;
import com.example.event.dto.EventResponse;
import com.example.event.model.Event;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.category.model.Category;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>EventService</b>
 * <br>
 * Service class that handles all business logic and operations related to events, inlcuding creating, deleting, retrieving, and helper functions
 */
@Service
public class EventService {

    private final CategoryService categoryService;
    private final EventRepository eventRepo;
    private final EventMapper mapper;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;

    public EventService(
        EventRepository eventRepo,
        EventMapper mapper,
        CategoryService categoryService,
        CurrentUserService currentUserService,
        UserRepository userRepo
    ) {
        this.eventRepo = eventRepo;
        this.mapper = mapper;
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
    }

    /**
     * Retrieves all events created by the user
     * @return a list of event response DTO
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        UserEntity user = getCurrentUser();
        return mapper.toResponseList(eventRepo.findByUser(user));
    }

    /**
     * Retrieves events fetched according to the provided filters
     * @param filter    a DTO with filtering attributes
     * @return          a list of event response DTO
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getFilteredEvents(EventFilter filter) {
        UserEntity user = getCurrentUser();
        return mapper.toResponseList(eventRepo.filterEvents(user, filter.search(), filter.idCategory()));
    }

    /**
     * Determines what is the current time and date. Retrieves all user events after current date and time.
     * @return a list of response DTO of future events
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsInFuture() {
        UserEntity user = getCurrentUser();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        return mapper.toResponseList(eventRepo.findWithFutureOrPresentDate(user, date, time));
    }

    /**
     * Retrieves an event by provided ID
     * @param idEvent   an ID of the event
     * @return          an event response DTO of the fetched event
     */
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long idEvent) {
        Event event = validateEventBelongsToUser(idEvent);
        return mapper.toResponse(event);
    }

    /**
     * Validates data provided by the user, creates a new event from the data, and saves event to the database
     * @param request  a request DTO containing event data
     * @return          a response DTO with newly created event's data
     */
    @Transactional
    public EventResponse createEvent(EventCreateRequest request) {

        UserEntity user = getCurrentUser();

        validateCreationRequest(request);

        Event event = new Event();
        event.setTitle(request.title());
        event.setDetails(request.details());
        event.setCategory(validateCategory(request));
        event.setDateStart(request.dateStart());
        event.setDateEnd(request.dateEnd());
        event.setTimeStart(request.timeStart());
        event.setTimeEnd(request.timeEnd());
        event.setRepeatedFrequency(request.repeatedFrequency());
        event.setDates(calculateEventDates(request));
        event.setUser(user);

        event = correctLogicalInconsistencies(request, event);

        return mapper.toResponse(eventRepo.save(event));
    }

    /**
     * Validates data provided by the user, creates a new event from the quick event creation request, and saves event to the database
     * @param request  a request DTO containing event data
     * @return          a response DTO with newly created event's data
     */
    @Transactional
    public EventResponse createEventQuick(QuickEventCreateRequest request) {

        UserEntity user = getCurrentUser();

        validateQuickCreationRequest(request);

        Event event = new Event();
        event.setTitle(request.title());
        event.setDateStart(request.dateStart());
        event.setDateEnd(request.dateEnd());
        event.setUser(user);

        return mapper.toResponse(eventRepo.save(event));
    }

    /**
     * Validates the event with provided ID belongs to the user, updates event, and returns updated event response DTO
     * @param idEvent   an ID of the event to be updated
     * @param request       a request DTO with the data that the event should be updated with
     * @return              a response DTO with updated event data
     */
    @Transactional
    @Modifying
    public EventResponse updateEvent(Long idEvent, EventCreateRequest request) {
        
        Event event = validateEventBelongsToUser(idEvent);

        event.setTitle(request.title());
        event.setDetails(request.details());
        event.setDateStart(request.dateStart());
        event.setDateEnd(request.dateEnd());
        event.setDuration(request.duration());
        event.setTimeStart(request.timeStart());
        event.setTimeEnd(request.timeEnd());
        event.setRepeated(request.repeated());
        event.setRepeatedFrequency(request.repeatedFrequency());
        event.setDates(calculateEventDates(request));

        return mapper.toResponse(eventRepo.save(event));
    }

    /**
     * Validates the event with provided ID belongs to the user and deletes the event
     * @param idEvent an ID of the event to be deleted
     */
    @Transactional
    public void deleteEvent(Long idEvent) {
        Event event = validateEventBelongsToUser(idEvent);

        eventRepo.delete(event);
    }

    /**
     * Validates the event belongs to a specific user
     * @param idEvent    an ID of an event to be checked
     * @return              an object of the verified event
     */
    public Event validateEventBelongsToUser(Long idEvent) {
        Long idUser = currentUserService.getCurrentUserId();
        
        return eventRepo.findByIdAndIdUser(idEvent, idUser)
            .orElseThrow(() -> new NotFoundException("Event", idEvent));
    }

    /**
     * Ensures the provided event creation request contains correct data
     * @param request a request DTO with event data provided by the user
     */
    public void validateCreationRequest(EventCreateRequest request) {
        if (request == null){
            throw new InvalidRequestException("NO_EVENT_DATA", "Event data is required.");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidRequestException("EVENT_TITLE_REQUIRED", "Event title is required.");
        }
        if (request.dateStart() == null || request.dateEnd() == null) {
            throw new InvalidRequestException("EVENT_DATE_REQUIRED", "Date is required.");
        }
        if (request.dateStart() != null && request.dateStart().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("EVENT_DATE_IN_PAST", "Date must be in the presence or future.");
        }
        if (request.dateEnd() == null || request.dateEnd().isBefore(request.dateStart())) {
            throw new InvalidRequestException("EVENT_START_AFTER_END", "Please choose the end date after or at the start date of event.");
        }
        if (request.duration() && (request.timeStart() == null || request.timeEnd() == null)) {
            throw new InvalidRequestException("EVENT_TIME_REQUIRED", "Please choose start and end time.");
        }
        if (request.timeStart() != null && request.timeEnd() != null
            && request.dateStart() != null && request.dateEnd() != null
            && request.dateStart().isEqual(request.dateEnd())
            && !request.timeEnd().isAfter(request.timeStart())) {
            throw new InvalidRequestException("EVENT_TIME_START_AFTER_END", "Start time must preceed end time.");
        }
        if (request.dateStart().equals(LocalDate.now()) && request.timeStart() != null && request.timeStart().isBefore(LocalTime.now())) {
            throw new InvalidRequestException("EVENT_TIME_IN_PAST", "Time must be in the presence or future.");
        }
        if (request.repeated() && request.repeatedFrequency() == null) {
            throw new InvalidRequestException("EVENT_REPETITION_FREQUENCY_REQUIRED", "Please choose the repetition frequency.");
        }
    }

    /**
     * Ensures the provided quick event creation request contains correct data
     * @param request a request DTO with event data provided by the user
     */
    public void validateQuickCreationRequest(QuickEventCreateRequest request) {
        if (request == null){
            throw new InvalidRequestException("NO_EVENT_DATA", "Event data is required.");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidRequestException("EVENT_TITLE_REQUIRED", "Event title is required.");
        }
        if (request.dateStart() == null || request.dateEnd() == null) {
            throw new InvalidRequestException("EVENT_DATE_REQUIRED", "Date is required.");
        }
        if (request.dateStart() != null && request.dateStart().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("EVENT_DATE_IN_PAST", "Date must be in the presence or future.");
        }
        if (request.dateEnd() == null || request.dateEnd().isBefore(request.dateStart())) {
            throw new InvalidRequestException("EVENT_START_AFTER_END", "Please choose the end date after or at the start date of event.");
        }
        
    }

    /**
     * Validates that selected category exists and belongs to the current user
     * @param request   an event creation request DTO
     * @return          a Category object if it exsists and belongs to the current user
     */
    public Category validateCategory(EventCreateRequest request) {
        if (request.idCategory() == null) {
            return null;
        }

        if (categoryService.categoryExistsById(request.idCategory())) {
            Category category;
            category = categoryService.validateCatBelongsToUser(request.idCategory());
            return category;
        } else {
            throw new NotFoundException("Category", request.idCategory());
        }  
    }

    /**
     * Helper method to ensure logical consistency of the data being saved
     * @param request   an event creation request DTO 
     * @param event     an event object
     * @return          an event object with resolved inconsistencies
     */
    public Event correctLogicalInconsistencies(EventCreateRequest request, Event event) {
        
        if (request.timeStart() != null && request.timeEnd() != null && !request.duration()) {
            event.setDuration(!request.duration());
        } else {
            event.setDuration(request.duration());
        }
        if (request.repeatedFrequency() != null && !request.repeated()) {
            event.setRepeated(!request.repeated());
        } else {
            event.setRepeated(request.repeated());
        }

        return event;
    }

    /**
     * Calculates dates of the event in case when user wants the event to be repeated in time
     * @param event an event creation request DTO
     * @return      a list of dates for the event
     */
    public List<LocalDate> calculateEventDates(EventCreateRequest event) {
        if (event.repeatedFrequency() == null || event.dateStart() == null) return new ArrayList<>();

        LocalDate first = event.dateStart();
        LocalDate firstDayNextYear = LocalDate.of(first.getYear() + 1, 1, 1);
        List<LocalDate> dates = new ArrayList<>();
         
        switch (event.repeatedFrequency()) {
            case EVERYDAY -> {
                dates = first.datesUntil(firstDayNextYear).toList();
            }
            case EVERY_WEEK -> {
                dates = first.datesUntil(firstDayNextYear, Period.ofWeeks(1)).toList();
            }
            case EVERY_TWO_WEEKS -> {
                dates = first.datesUntil(firstDayNextYear, Period.ofWeeks(2)).toList();
            }
            case EVERY_MONTH -> {
                dates = first.datesUntil(firstDayNextYear, Period.ofMonths(1)).toList();
            }
            case NO_REPEAT -> {
            }
        }
        
        return new ArrayList<>(dates);
    }

    /**
     * Retrieves events filtered by the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return  a list of response DTOs representing filtered events
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByDate(int year, int month, int day) {
        List<Event> events = eventRepo.findByDateAndUser(LocalDate.of(year, month, day), getCurrentUser());
        return mapper.toResponseList(events);
    }

    /**
     * Removes category relationships from all events assigned to the specific category
     * @param idCategory    an ID of the category
     * @param user          an object with user data
     */
    public void clearCategory(Long idCategory, UserEntity user) {
        eventRepo.clearCategoryByCategoryAndUser(idCategory, user);
    }

    /**
     * Deletes all events that belong to the user
     * @param user an object with the user data
     */
    public void deleteEventsByUser(UserEntity user) {
        eventRepo.deleteByUser(user);
    }

    /**
     * Retrieves a current user object
     * @return  an object with the current user's data
     */
    private UserEntity getCurrentUser() {
        Long idUser = currentUserService.getCurrentUserId();
        return userRepo.findById(idUser)
            .orElseThrow(() -> new NotFoundException("User", idUser));
    }
}
