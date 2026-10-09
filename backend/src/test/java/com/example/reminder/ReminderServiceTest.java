package com.example.reminder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.event.model.Event;
import com.example.event.service.EventService;
import com.example.reminder.dto.ReminderCreationRequest;
import com.example.reminder.dto.ReminderResponse;
import com.example.reminder.enums.ReminderStatus;
import com.example.reminder.enums.ReminderType;
import com.example.reminder.enums.Timing;
import com.example.reminder.mapper.ReminderMapper;
import com.example.reminder.model.Reminder;
import com.example.reminder.repository.ReminderRepository;
import com.example.reminder.service.ReminderService;
import com.example.reminder.service.ReminderSseService;
import com.example.task.model.Task;
import com.example.task.service.TaskService;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepo;

    @Mock
    private ReminderMapper mapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private TaskService taskService;

    @Mock
    private EventService eventService;

    @Mock
    private ReminderSseService sseService;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void getAllRemindersReturnsUserReminders() {
        Long idUser = 1L;

        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        Reminder reminder = new Reminder();
        List<Reminder> reminders = List.of(reminder);

        ReminderResponse response = createReminderResponse();

        List<ReminderResponse> responses = List.of(response);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));
        when(reminderRepo.findByUser(user)).thenReturn(reminders);
        when(mapper.toResponseList(reminders)).thenReturn(responses);

        List<ReminderResponse> result = reminderService.getAllReminders();

        assertEquals(responses, result);
        verify(reminderRepo).findByUser(user);
        verify(mapper).toResponseList(reminders);
    }

    @Test
    void getReminderReturnsReminderWhenItExists() {
        Long idReminder = 1L;
        Long idUser = 2L;

        Reminder reminder = new Reminder();
        ReminderResponse response = createReminderResponse();

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser))
            .thenReturn(Optional.of(reminder));
        when(mapper.toResponse(reminder)).thenReturn(response);

        ReminderResponse result = reminderService.getReminder(idReminder);

        assertEquals(response, result);

        verify(reminderRepo).findByIdAndIdUser(idReminder, idUser);
        verify(mapper).toResponse(reminder);
    }


    @Test
    void getReminderThrowsExceptionWhenReminderDoesNotExist() {
        Long idReminder = 1L;
        Long idUser = 2L;

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reminderService.getReminder(idReminder));
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getRemindersByDateReturnsMappedReminders() {
        Long idUser = 1L;

        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        LocalDate date = LocalDate.of(2026, 5, 20);

        Reminder reminder = new Reminder();
        List<Reminder> reminders = List.of(reminder);

        ReminderResponse response = createReminderResponse();
        List<ReminderResponse> responses = List.of(response);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));
        when(reminderRepo.findByDateAndUser(date, user)).thenReturn(reminders);
        when(mapper.toResponseList(reminders)).thenReturn(responses);

        List<ReminderResponse> result = reminderService.getRemindersByDate(2026, 5, 20);

        assertEquals(responses, result);
        verify(reminderRepo).findByDateAndUser(date, user);
        verify(mapper).toResponseList(reminders);
    }

    @Test
    void getSentRemindersReturnsSentReminders() {
        Long idUser = 1L;

        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        Reminder reminder = new Reminder();
        List<Reminder> reminders = List.of(reminder);

        ReminderResponse response = createReminderResponse();

        List<ReminderResponse> responses = List.of(response);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));
        when(reminderRepo.findByUserAndStatus(user, ReminderStatus.SENT)).thenReturn(reminders);
        when(mapper.toResponseList(reminders)).thenReturn(responses);

        List<ReminderResponse> result = reminderService.getSentReminders();

        assertEquals(responses, result);
        verify(reminderRepo).findByUserAndStatus(user, ReminderStatus.SENT);
        verify(mapper).toResponseList(reminders);
    }

    @Test
    void createReminderCreatesReminderWithCorrectValues() {
        Long idUser = 1L;

        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 10, 14, 30);

        when(request.content()).thenReturn("Test reminder");
        when(request.type()).thenReturn(ReminderType.NONE);
        when(request.idEvent()).thenReturn(null);
        when(request.idTask()).thenReturn(null);
        when(request.remindAt()).thenReturn(Timing.SET_MANUALLY);
        when(request.repeatForAll()).thenReturn(false);

        Reminder result =  reminderService.createReminder(request, dateTime, user);

        assertEquals("Test reminder", result.getContent());
        assertEquals(ReminderType.NONE, result.getType());
        assertNull(result.getIdEvent());
        assertNull(result.getIdTask());
        assertEquals(Timing.SET_MANUALLY, result.getRemindAt());
        assertEquals(ReminderStatus.PENDING, result.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(LocalDate.of(2026, 6, 10), result.getRemindDate());
        assertEquals(LocalTime.of(14, 30), result.getRemindTime());
        assertFalse(result.isRepeatForAll());
    }

    @Test
    void updateReminderUpdatesValuesAndReturnsResponse() {
        Long idReminder = 1L;
        Long idUser = 2L;

        Reminder reminder = new Reminder();

        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDate date = LocalDate.of(2026, 7, 15);
        LocalTime time = LocalTime.of(10, 30);

        ReminderResponse response = mock(ReminderResponse.class);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.of(reminder));

        when(request.content()).thenReturn("Updated reminder");
        when(request.type()).thenReturn(ReminderType.NONE);
        when(request.remindAt()).thenReturn(Timing.SET_MANUALLY);
        when(request.remindDate()).thenReturn(date);
        when(request.remindTime()).thenReturn(time);

        when(reminderRepo.save(reminder)).thenReturn(reminder);
        when(mapper.toResponse(reminder)).thenReturn(response);

        ReminderResponse result = reminderService.updateReminder(idReminder, request);

        assertEquals(response, result);
        assertEquals("Updated reminder", reminder.getContent());
        assertEquals(date, reminder.getRemindDate());
        assertEquals(time, reminder.getRemindTime());
        assertEquals(ReminderStatus.PENDING, reminder.getStatus());
        assertNull(reminder.getSentTime());
        verify(reminderRepo).save(reminder);
        verify(mapper).toResponse(reminder);
    }

    @Test
    void deleteReminderDeletesReminderWhenItExists() {
        Long idReminder = 1L;
        Long idUser = 2L;

        Reminder reminder = new Reminder();

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.of(reminder));

        reminderService.deleteReminder(idReminder);

        verify(reminderRepo).delete(reminder);
    }


    @Test
    void deleteReminderThrowsExceptionWhenReminderDoesNotExist() {
        Long idReminder = 1L;
        Long idUser = 2L;

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reminderService.deleteReminder(idReminder));
        verify(reminderRepo, never()).delete(any());
    }

    @Test
    void changeReminderStatusUpdatesStatusAndReturnsResponse() {
        Long idReminder = 1L;
        Long idUser = 2L;

        Reminder reminder = new Reminder();
        ReminderResponse response = mock(ReminderResponse.class);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.of(reminder));
        when(reminderRepo.save(reminder)).thenReturn(reminder);
        when(mapper.toResponse(reminder)).thenReturn(response);

        ReminderResponse result = reminderService.changeReminderStatus(idReminder, ReminderStatus.SENT);

        assertEquals(response, result);
        assertEquals(ReminderStatus.SENT, reminder.getStatus());
        verify(reminderRepo).save(reminder);
        verify(mapper).toResponse(reminder);
    }

    @Test
    void validateReminderBelongsToUserReturnsReminderWhenItExists() {
        Long idReminder = 1L;
        Long idUser = 2L;

        Reminder reminder = new Reminder();

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.of(reminder));

        Reminder result = reminderService.validateReminderBelongsToUser(idReminder);

        assertEquals(reminder, result);
        verify(reminderRepo).findByIdAndIdUser(idReminder, idUser);
    }


    @Test
    void validateReminderBelongsToUserThrowsExceptionWhenReminderDoesNotExist() {
        Long idReminder = 1L;
        Long idUser = 2L;

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(reminderRepo.findByIdAndIdUser(idReminder, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reminderService.validateReminderBelongsToUser(idReminder));
    }

    @Test
    void isFutureDateReturnsTrueForFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertTrue(reminderService.isFutureDate(futureDate));
    }


    @Test
    void isFutureDateReturnsFalseForToday() {
        assertFalse(reminderService.isFutureDate(LocalDate.now()));
    }


    @Test
    void isFutureDateReturnsFalseForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertFalse(reminderService.isFutureDate(pastDate));
    }


    @Test
    void isFutureDateReturnsFalseForNull() {
        assertFalse(reminderService.isFutureDate(null));
    }


    @Test
    void isFutureOrPresentDateReturnsTrueForFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertTrue(reminderService.isFutureOrPresentDate(futureDate, LocalTime.of(10, 0)));
    }


    @Test
    void isFutureOrPresentDateReturnsFalseForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertFalse(reminderService.isFutureOrPresentDate(pastDate,LocalTime.of(10, 0)));
    }


    @Test
    void isFutureOrPresentDateReturnsFalseForNullDate() {
        assertFalse(reminderService.isFutureOrPresentDate(null,LocalTime.of(10, 0)));
    }


    @Test
    void calculateDateAndTimeReturnsTargetTimeWhenAtTime() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDate date = LocalDate.of(2026, 8, 10);
        LocalTime time = LocalTime.of(15, 30);

        when(request.remindAt()).thenReturn(Timing.AT_TIME);

        List<LocalDateTime> result = reminderService.calculateDateAndTime(request, List.of(date), time);

        assertEquals(List.of(LocalDateTime.of(date, time)), result);
    }


    @Test
    void calculateDateAndTimeSubtractsQuarterHour() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDate date = LocalDate.of(2026, 8, 10);
        LocalTime time = LocalTime.of(15, 30);

        when(request.remindAt()).thenReturn(Timing.QUARTER_BEFORE);

        List<LocalDateTime> result = reminderService.calculateDateAndTime(request, List.of(date), time);

        assertEquals(List.of(LocalDateTime.of(2026, 8, 10, 15, 15)), result);
    }


    @Test
    void calculateDateAndTimeSubtractsHour() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDate date = LocalDate.of(2026, 8, 10);
        LocalTime time = LocalTime.of(15, 30);

        when(request.remindAt()).thenReturn(Timing.HOUR_BEFORE);

        List<LocalDateTime> result = reminderService.calculateDateAndTime(request, List.of(date), time);

        assertEquals(List.of(LocalDateTime.of(2026, 8, 10, 14, 30)), result);
    }


    @Test
    void calculateDateAndTimeSubtractsDay() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDate date = LocalDate.of(2026, 8, 10);
        LocalTime time = LocalTime.of(15, 30);

        when(request.remindAt()).thenReturn(Timing.DAY_BEFORE);

        List<LocalDateTime> result = reminderService.calculateDateAndTime(request, List.of(date), time);

        assertEquals(List.of(LocalDateTime.of(2026, 8, 9, 15, 30)), result);
    }

    @Test
    void calculateDateAndTimeThrowsExceptionWhenTargetDateIsMissing() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        when(request.remindAt()).thenReturn(Timing.AT_TIME);

        assertThrows(InvalidRequestException.class, () -> reminderService.calculateDateAndTime(request, List.of(), LocalTime.of(10, 0)));
    }


    @Test
    void calculateDateAndTimeThrowsExceptionWhenTargetTimeIsMissing() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        when(request.remindAt()).thenReturn(Timing.AT_TIME);

        assertThrows(InvalidRequestException.class, () -> reminderService.calculateDateAndTime(request, List.of(LocalDate.of(2026, 8, 10)), null));
    }


    @Test
    void calculateWhenRepeatedAndManuallyReturnsManualDateWhenTargetIsEmpty() {
        ReminderCreationRequest request =
            mock(ReminderCreationRequest.class);

        LocalDate reminderDate = LocalDate.of(2026, 8, 10);
        LocalTime reminderTime = LocalTime.of(12, 30);

        when(request.remindDate()).thenReturn(reminderDate);
        when(request.remindTime()).thenReturn(reminderTime);

        List<LocalDateTime> result = reminderService.calculateWhenRepeatedAndManually(List.of(), request);

        assertEquals(List.of(LocalDateTime.of(reminderDate, reminderTime)), result);
    }


    @Test
    void calculateWhenRepeatedAndManuallyCalculatesDatesForEvent() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        LocalDate reminderDate = LocalDate.of(2026, 8, 10);
        LocalTime reminderTime = LocalTime.of(12, 30);

        List<LocalDate> targetDates = List.of(LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 24));

        when(request.remindDate()).thenReturn(reminderDate);
        when(request.remindTime()).thenReturn(reminderTime);
        when(request.type()).thenReturn(ReminderType.EVENT);

        List<LocalDateTime> result = reminderService.calculateWhenRepeatedAndManually(targetDates, request);

        assertEquals(List.of(LocalDateTime.of(2026, 8, 10, 12, 30), LocalDateTime.of(2026, 8, 17, 12, 30)), result);
    }


    @Test
    void calculateWhenRepeatedAndManuallyThrowsExceptionWhenReminderDateIsAfterEvent() {
        ReminderCreationRequest request = mock(ReminderCreationRequest.class);

        when(request.remindDate()).thenReturn(LocalDate.of(2026, 8, 20));

        when(request.remindTime()).thenReturn(LocalTime.of(12, 0));

        when(request.type()).thenReturn(ReminderType.EVENT);

        assertThrows(InvalidRequestException.class, () -> reminderService.calculateWhenRepeatedAndManually(List.of(LocalDate.of(2026, 8, 15)), request));
    }


    @Test
    void deleteRemindersByUserDeletesUserReminders() {
        UserEntity user = new UserEntity();
        user.setIdUser(1L);

        reminderService.deleteRemindersByUser(user);

        verify(reminderRepo).deleteByUser(user);
    }

    @Test
    void validateEventBelongsToUserDelegatesToEventService() {
        Long idEvent = 10L;

        Event event = new Event();

        when(eventService.validateEventBelongsToUser(idEvent)).thenReturn(event);

        Event result = reminderService.validateEventBelongsToUser(idEvent);

        assertEquals(event, result);
        verify(eventService).validateEventBelongsToUser(idEvent);
    }


    @Test
    void validateTaskBelongsToUserDelegatesToTaskService() {
        Long idTask = 20L;

        Task task = new Task();

        when(taskService.validateTaskBelongsToUser(idTask)).thenReturn(task);

        Task result = reminderService.validateTaskBelongsToUser(idTask);

        assertEquals(task, result);
        verify(taskService).validateTaskBelongsToUser(idTask);
    }

    private ReminderResponse createReminderResponse() {
        return new ReminderResponse(
            1L,
            "Reminder",
            ReminderType.NONE,
            null,
            null,
            false,
            Timing.SET_MANUALLY,
            LocalDate.of(2026, 2, 20),
            LocalTime.of(10, 10),
            null,
            ReminderStatus.PENDING
        );
    }

}
