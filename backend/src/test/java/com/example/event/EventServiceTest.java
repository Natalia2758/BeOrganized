package com.example.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.category.model.Category;
import com.example.category.service.CategoryService;
import com.example.common.enums.RepeatedFrequency;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.event.dto.EventCreateRequest;
import com.example.event.dto.EventFilter;
import com.example.event.dto.EventResponse;
import com.example.event.dto.QuickEventCreateRequest;
import com.example.event.mapper.EventMapper;
import com.example.event.model.Event;
import com.example.event.repository.EventRepository;
import com.example.event.service.EventService;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private EventRepository eventRepo;

    @Mock
    private EventMapper mapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private EventService eventService;

    @Test
    void getAllEventsReturnsAllUserEvents() {
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);

        Event event1 = new Event();
        Event event2 = new Event();
        event1.setUser(user);
        event2.setUser(user);

        List<Event> eventsByUser = List.of(event1, event2);

        EventResponse response1 = createResponse();
        EventResponse response2 = createResponse();
        List<EventResponse> eventResponses = List.of(response1, response2);

        when(eventRepo.findByUser(user)).thenReturn(eventsByUser);
        when(mapper.toResponseList(eventsByUser)).thenReturn(eventResponses);

        List<EventResponse> result = eventService.getAllEvents();

        assertEquals(eventResponses, result);

        verify(currentUserService).getCurrentUserId();
        verify(userRepo).findById(idUser);
        verify(eventRepo).findByUser(user);
        verify(mapper).toResponseList(eventsByUser);
    }

    @Test
    void getFilteredEventsReturnsFilteredEvents() {
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);

        EventFilter filter = new EventFilter(2L, "meeting");

        Event event1 = new Event();
        Event event2 = new Event();

        List<Event> events = List.of(event1, event2);
        List<EventResponse> expectedResponses = List.of(createResponse(), createResponse());

        when(eventRepo.filterEvents(user, filter.search(), filter.idCategory())).thenReturn(events);
        when(mapper.toResponseList(events)).thenReturn(expectedResponses);

        List<EventResponse> result = eventService.getFilteredEvents(filter);

        assertEquals(expectedResponses, result);
        verify(eventRepo).filterEvents(
            user,
            filter.search(),
            filter.idCategory()
        );
        verify(mapper).toResponseList(events);
    }

    @Test
    void getEventsInFutureReturnsFutureEvents() {
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);

        List<Event> events = List.of(new Event(), new Event());

        List<EventResponse> expectedResponses =
            List.of(createResponse(), createResponse());

        when(eventRepo.findWithFutureOrPresentDate(eq(user), any(LocalDate.class), any(LocalTime.class))).thenReturn(events);
        when(mapper.toResponseList(events)).thenReturn(expectedResponses);

        List<EventResponse> result = eventService.getEventsInFuture();

        assertEquals(expectedResponses, result);
        verify(eventRepo).findWithFutureOrPresentDate(eq(user), any(LocalDate.class), any(LocalTime.class));
        verify(mapper).toResponseList(events);
    }

    @Test
    void getEventsByDateReturnsMappedEvents() {
        LocalDate date = LocalDate.of(2026, 9, 1);
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);

        List<Event> events = List.of(new Event());
        List<EventResponse> expectedResponse = List.of(createResponse());

        when(eventRepo.findByDateAndUser(date, user)).thenReturn(events);
        when(mapper.toResponseList(events)).thenReturn(expectedResponse);

        List<EventResponse> result = eventService.getEventsByDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());

        assertEquals(expectedResponse, result);
        verify(eventRepo).findByDateAndUser(date, user);
        verify(mapper).toResponseList(events);
    }

    @Test
    void getEventReturnsEventWhenExists() {
        Long idEvent = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Event event = new Event();
        EventResponse expectedResponse = mock(EventResponse.class);

        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.of(event));

        when(mapper.toResponse(event)).thenReturn(expectedResponse);

        EventResponse result = eventService.getEvent(idEvent);

        assertEquals(expectedResponse, result);
        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);
        verify(mapper).toResponse(event);
    }

    @Test
    void getEventThrowsExceptionWhenEventDoesNotExist() {
        Long idEvent = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getEvent(idEvent));
        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);
        verify(mapper, never()).toResponse(any(Event.class));
    }

    @Test
    void createEventSavesRequestValuesAndReturnsResponse() {
        Long idUser = 1L;
        Long idCategory = 2L;

        UserEntity user = authenticatedUser(idUser);
        Category category = new Category();

        LocalDate dateStart = LocalDate.now().plusDays(1);

        EventCreateRequest request = new EventCreateRequest(
            "Conference",
            "Details",
            idCategory,
            dateStart,
            dateStart.plusDays(1),
            false,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            false,
            RepeatedFrequency.EVERY_WEEK
        );

        EventResponse expectedResponse = createResponse();

        when(categoryService.categoryExistsById(idCategory)).thenReturn(true);
        when(categoryService.validateCatBelongsToUser(idCategory)).thenReturn(category);
        when(eventRepo.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenReturn(expectedResponse);

        EventResponse result = eventService.createEvent(request);

        assertEquals(expectedResponse, result);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepo).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();

        assertEquals(request.title(), savedEvent.getTitle());
        assertEquals(request.details(), savedEvent.getDetails());
        assertEquals(category, savedEvent.getCategory());
        assertEquals(request.dateStart(), savedEvent.getDateStart());
        assertEquals(request.dateEnd(), savedEvent.getDateEnd());
        assertEquals(request.timeStart(), savedEvent.getTimeStart());
        assertEquals(request.timeEnd(), savedEvent.getTimeEnd());

        assertTrue(savedEvent.hasDuration());
        assertTrue(savedEvent.isRepeated());

        assertEquals(request.repeatedFrequency(), savedEvent.getRepeatedFrequency());
        assertEquals(user, savedEvent.getUser());

        verify(categoryService).categoryExistsById(idCategory);
        verify(categoryService).validateCatBelongsToUser(idCategory);
        verify(mapper).toResponse(savedEvent);
    }

    @Test
    void createEventDoesNotSaveWhenRequestIsInvalid() {
        authenticatedUser(1L);

        EventCreateRequest request = new EventCreateRequest(
            "",
            null,
            null,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(1),
            false,
            null,
            null,
            false,
            null
        );

        assertThrows(InvalidRequestException.class, () -> eventService.createEvent(request));
        verify(eventRepo, never()).save(any(Event.class));
        verify(mapper, never()).toResponse(any(Event.class));
    }

    @Test
    void createEventQuickReturnsResponseWhenSuccessfullyCreated() {
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(1);

        QuickEventCreateRequest request = new QuickEventCreateRequest("Quick event", start, end);

        EventResponse expectedResponse = createResponse();

        when(eventRepo.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenReturn(expectedResponse);

        EventResponse result = eventService.createEventQuick(request);

        assertEquals(expectedResponse, result);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepo).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();

        assertEquals(request.title(), savedEvent.getTitle());
        assertEquals(request.dateStart(), savedEvent.getDateStart());
        assertEquals(request.dateEnd(), savedEvent.getDateEnd());
        assertEquals(user, savedEvent.getUser());

        verify(mapper).toResponse(savedEvent);
    }

    @Test
    void validateCategoryReturnsNullWhenCategoryIsNotProvided() {
        EventCreateRequest request = createRequest(null);

        assertNull(eventService.validateCategory(request));
        verify(categoryService, never()).categoryExistsById(anyLong());
        verify(categoryService, never()).validateCatBelongsToUser(anyLong());
    }

    @Test
    void validateCategoryReturnsCategoryWhenItExistsForUser() {
        Long idCategory = 2L;

        Category category = new Category();
        EventCreateRequest request = createRequest(idCategory);

        when(categoryService.categoryExistsById(idCategory)).thenReturn(true);
        when(categoryService.validateCatBelongsToUser(idCategory)).thenReturn(category);

        Category result = eventService.validateCategory(request);

        assertEquals(category, result);
        verify(categoryService).categoryExistsById(idCategory);
        verify(categoryService).validateCatBelongsToUser(idCategory);
    }

    @Test
    void validateCategoryThrowsExceptionWhenCategoryDoesNotExist() {
        Long idCategory = 2L;

        when(categoryService.categoryExistsById(idCategory)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> eventService.validateCategory(createRequest(idCategory)));
        verify(categoryService).categoryExistsById(idCategory);
        verify(categoryService, never()).validateCatBelongsToUser(anyLong());
    }

    @Test
    void updateEventUpdatesValuesAndReturnsResponse() {
        Long idEvent = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Event event = new Event();
        EventCreateRequest request = createRequest(2L);
        EventResponse expectedResponse = createResponse();

        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.of(event));
        when(eventRepo.save(event)).thenReturn(event);
        when(mapper.toResponse(event)).thenReturn(expectedResponse);

        EventResponse result = eventService.updateEvent(idEvent, request);

        assertEquals(expectedResponse, result);
        assertEquals(request.title(), event.getTitle());
        assertEquals(request.details(), event.getDetails());
        assertEquals(request.dateStart(), event.getDateStart());
        assertEquals(request.dateEnd(), event.getDateEnd());
        assertEquals(request.timeStart(), event.getTimeStart());
        assertEquals(request.timeEnd(), event.getTimeEnd());
        assertEquals(request.duration(), event.hasDuration());
        assertEquals(request.repeated(), event.isRepeated());
        assertEquals(request.repeatedFrequency(), event.getRepeatedFrequency());

        verify(eventRepo).save(event);
        verify(mapper).toResponse(event);
    }

    @Test
    void updateEventThrowsExceptionWhenEventDoesNotExist() {
        Long idEvent = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.updateEvent(idEvent, createRequest(null)));

        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);
        verify(eventRepo, never()).save(any(Event.class));
        verify(mapper, never()).toResponse(any(Event.class));
    }

    @Test
    void deleteEventDeletesEventWhenItExists() {
        Long idEvent = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Event event = new Event();

        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.of(event));

        eventService.deleteEvent(idEvent);

        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);
        verify(eventRepo).delete(event);
    }

    @Test
    void deleteEventThrowsExceptionWhenEventDoesNotExist() {
        Long idEvent = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.deleteEvent(idEvent));

        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);

        verify(eventRepo, never()).delete(any(Event.class));
    }

    @Test
    void validateEventBelongsToUserReturnsEventWhenItExists() {
        Long idEvent = 1L;
        Long idUser = 2L;

        Event event = new Event();

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.of(event));

        Event result = eventService.validateEventBelongsToUser(idEvent);

        assertEquals(event, result);
        verify(currentUserService).getCurrentUserId();
        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);
    }

    @Test
    void validateEventBelongsToUserThrowsExceptionWhenEventDoesNotExist() {
        Long idEvent = 1L;
        Long idUser = 2L;

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(eventRepo.findByIdAndIdUser(idEvent, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.validateEventBelongsToUser(idEvent));
        verify(eventRepo).findByIdAndIdUser(idEvent, idUser);
        verify(eventRepo, never()).findById(idEvent);
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenRequestIsNull() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(null));

        assertEquals("Event data is required.", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenTitleIsBlank() {
        EventCreateRequest request =
            new EventCreateRequest(
                "",
                null,
                null,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(1),
                false,
                null,
                null,
                false,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Event title is required.",  exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenStartDateIsMissing() {
        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                null,
                LocalDate.now().plusDays(1),
                false,
                null,
                null,
                false,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Date is required.", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenEndDateIsMissing() {
        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                LocalDate.now().plusDays(1),
                null,
                false,
                null,
                null,
                false,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Date is required.", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenStartDateIsInPast() {
        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                false,
                LocalTime.NOON,
                LocalTime.of(13, 0),
                false,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Date must be in the presence or future.",  exception.getMessage() );
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenEndDateIsBeforeStartDate() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.minusDays(1);

        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                start,
                end,
                false,
                LocalTime.NOON,
                LocalTime.of(13, 0),
                false,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Please choose the end date after or at the start date of event.", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenDurationHasNoTime() {
        LocalDate date = LocalDate.now().plusDays(1);

        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                date,
                date,
                true,
                null,
                null,
                false,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Please choose start and end time.", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenSameDayEndTimeIsNotAfterStartTime() {
        LocalDate date = LocalDate.now().plusDays(1);

        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                date,
                date,
                true,
                LocalTime.of(13, 0),
                LocalTime.of(12, 0),
                false,
                null
            );

        InvalidRequestException exception = assertThrows( InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Start time must preceed end time.", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenRepeatedWithoutFrequency() {
        LocalDate date = LocalDate.now().plusDays(1);

        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                date,
                date,
                false,
                null,
                null,
                true,
                null
            );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateCreationRequest(request));

        assertEquals("Please choose the repetition frequency.", exception.getMessage());
    }

    @Test
    void validateCreationRequestAllowsSameDayTimedEventsWhenEndTimeIsAfterStartTime() {
        LocalDate date = LocalDate.now().plusDays(1);

        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                date,
                date,
                true,
                LocalTime.NOON,
                LocalTime.of(13, 0),
                false,
                null
            );

        assertDoesNotThrow(() -> eventService.validateCreationRequest(request));
    }

    @Test
    void validateQuickCreationRequestThrowsExceptionWhenRequestIsNull() {
        assertThrows(InvalidRequestException.class, () -> eventService.validateQuickCreationRequest(null));
    }

    @Test
    void validateQuickCreationRequestThrowsExceptionWhenTitleIsBlank() {
        QuickEventCreateRequest request = new QuickEventCreateRequest("", LocalDate.now().plusDays(1), LocalDate.now().plusDays(1));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateQuickCreationRequest(request));

        assertEquals("Event title is required.", exception.getMessage());
    }

    @Test
    void validateQuickCreationRequestThrowsExceptionWhenDatesAreMissing() {
        QuickEventCreateRequest request = new QuickEventCreateRequest( "Event", null, LocalDate.now().plusDays(1));

        assertThrows(InvalidRequestException.class, () -> eventService.validateQuickCreationRequest(request));
    }

    @Test
    void validateQuickCreationRequestThrowsExceptionWhenStartDateIsInPast() {
        QuickEventCreateRequest request = new QuickEventCreateRequest("Event", LocalDate.now().minusDays(1), LocalDate.now());

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateQuickCreationRequest(request));

        assertEquals("Date must be in the presence or future.", exception.getMessage());
    }

    @Test
    void validateQuickCreationRequestThrowsExceptionWhenEndDateIsBeforeStartDate() {
        LocalDate start = LocalDate.now().plusDays(2);

        QuickEventCreateRequest request = new QuickEventCreateRequest("Event", start, start.minusDays(1));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> eventService.validateQuickCreationRequest(request));

        assertEquals("Please choose the end date after or at the start date of event.", exception.getMessage());
    }

    @Test
    void correctLogicalInconsistenciesAddsDurationAndRepetition() {
        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                null,
                null,
                false,
                LocalTime.NOON,
                LocalTime.of(13, 0),
                false,
                RepeatedFrequency.EVERY_WEEK
            );

        Event event = new Event();

        Event result = eventService.correctLogicalInconsistencies(request, event);

        assertEquals(event, result);
        assertTrue(event.hasDuration());
        assertTrue(event.isRepeated());
    }

    @Test
    void correctLogicalInconsistenciesKeepsExplicitFalseValues() {
        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                false,
                null
            );

        Event event = new Event();

        eventService.correctLogicalInconsistencies(request,  event);

        assertFalse(event.hasDuration());
        assertFalse(event.isRepeated());
    }

    @Test
    void calculateEventDatesReturnsEmptyListWhenFrequencyIsNull() {
        EventCreateRequest request = createRequest(null);

        assertTrue( eventService.calculateEventDates(request).isEmpty());
    }

    @Test
    void calculateEventDatesReturnsEmptyListWhenStartDateIsNull() {
        EventCreateRequest request =
            new EventCreateRequest(
                "Event",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                false,
                RepeatedFrequency.EVERY_WEEK
            );

        assertTrue(eventService.calculateEventDates(request).isEmpty());
    }

    @Test
    void calculateEventDatesReturnsDatesForEveryDay() {
        LocalDate start = LocalDate.of(2026, 12, 10);

        EventCreateRequest request = createRepeatedRequest(start, RepeatedFrequency.EVERYDAY);

        List<LocalDate> result = eventService.calculateEventDates(request);

        assertEquals(22, result.size());
        assertEquals(start, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 31), result.get(result.size() - 1) );
    }

    @Test
    void calculateEventDatesReturnsDatesForEveryWeek() {
        LocalDate start = LocalDate.of(2026, 12, 1);

        EventCreateRequest request = createRepeatedRequest(start, RepeatedFrequency.EVERY_WEEK);

        List<LocalDate> result = eventService.calculateEventDates(request);

        assertEquals(5, result.size());
        assertEquals(start, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 29), result.get(result.size() - 1));
    }

    @Test
    void calculateEventDatesReturnsDatesForEveryTwoWeeks() {
        LocalDate start = LocalDate.of(2026, 11, 1);

        EventCreateRequest request = createRepeatedRequest(start, RepeatedFrequency.EVERY_TWO_WEEKS);

        List<LocalDate> result = eventService.calculateEventDates(request);

        assertEquals(5, result.size());
        assertEquals(start, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 27), result.get(result.size() - 1));
    }

    @Test
    void calculateEventDatesReturnsDatesForEveryMonth() {
        LocalDate start = LocalDate.of(2026, 10, 1);

        EventCreateRequest request = createRepeatedRequest(start, RepeatedFrequency.EVERY_MONTH);

        List<LocalDate> result = eventService.calculateEventDates(request);

        assertEquals(3, result.size());
        assertEquals(start, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 1), result.get(result.size() - 1));
    }

    @Test
    void calculateEventDatesReturnsEmptyListForNoRepeat() {
        LocalDate start = LocalDate.of(2026, 10, 1);

        EventCreateRequest request = createRepeatedRequest(start, RepeatedFrequency.NO_REPEAT);

        assertTrue(eventService.calculateEventDates(request).isEmpty());
    }

    @Test
    void clearCategoryClearsCategoryFromEvents() {
        Long idCategory = 5L;
        UserEntity user = new UserEntity();

        eventService.clearCategory(idCategory, user);

        verify(eventRepo).clearCategoryByCategoryAndUser(idCategory, user);
    }

    @Test
    void deleteEventsByUserDeletesAllUserEvents() {
        UserEntity user = new UserEntity();

        eventService.deleteEventsByUser(user);

        verify(eventRepo).deleteByUser(user);
    }

    private UserEntity authenticatedUser(Long idUser) {
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        lenient().when(currentUserService.getCurrentUserId()).thenReturn(idUser);

        lenient().when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

        return user;
    }

    private EventCreateRequest createRequest(Long idCategory) {
        LocalDate dateStart = LocalDate.now().plusDays(1);

        return new EventCreateRequest(
            "Event",
            "Details",
            idCategory,
            dateStart,
            dateStart.plusDays(1),
            false,
            LocalTime.NOON,
            LocalTime.of(13, 0),
            false,
            null
        );
    }

    private EventCreateRequest createRepeatedRequest(
        LocalDate dateStart,
        RepeatedFrequency frequency
    ) {
        return new EventCreateRequest(
            "Event",
            "Details",
            null,
            dateStart,
            dateStart,
            false,
            null,
            null,
            true,
            frequency
        );
    }

    private EventResponse createResponse() {
        return new EventResponse(
            1L,
            "Event",
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            false,
            null,
            null
        );
    }
}