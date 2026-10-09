package com.example.reminder.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.task.model.Task;
import com.example.task.service.TaskService;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

/**
 * <b>ReminderService</b>
 * <br>
 * A service class responsible for handling, processing, and modifying reminder data
 */
@Service
public class ReminderService {

    private final ReminderRepository reminderRepo;
    private final ReminderMapper mapper;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final TaskService taskService;
    private final EventService eventService;
    private final ReminderSseService sseService;
    
    public ReminderService(
        ReminderRepository reminderRepo,
        ReminderMapper mapper,
        TaskService taskService,
        EventService eventService,
        CurrentUserService currentUserService,
        UserRepository userRepo,
        ReminderSseService sseService
    ) {
        this.reminderRepo = reminderRepo;
        this.mapper = mapper;
        this.taskService = taskService;
        this.eventService = eventService;
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.sseService = sseService;
    }

    /**
     * Retrieves all reminders created by the user
     * @return a list of user reminders
     */
    public List<ReminderResponse> getAllReminders() {
        UserEntity user = getCurrentUser();
        List<Reminder> reminders = reminderRepo.findByUser(user);
        return mapper.toResponseList(reminders);
    }

    /**
     * Retrieves a reminder by database ID
     * @param idReminder    an ID of reminder to be fetched
     * @return              a reminder DTO response
     */
    public ReminderResponse getReminder(Long idReminder) {
        Reminder reminder = validateReminderBelongsToUser(idReminder);
        return mapper.toResponse(reminder);
    }

    /**
     * Retrieves a reminder list filtered by date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return      a list of response DTOs representing filtered reminders
     */
    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersByDate(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        List<Reminder> reminders = reminderRepo.findByDateAndUser(date, getCurrentUser());
        return mapper.toResponseList(reminders);
    }

    /**
     * Retrieves all reminders that have sent status
     * @return  a list of sent reminders
     */
    public List<ReminderResponse> getSentReminders() {

        UserEntity user = getCurrentUser();
        List<Reminder> reminders = reminderRepo.findByUserAndStatus(user, ReminderStatus.SENT);

        return mapper.toResponseList(reminders);
    }

    /**
     * Generates a list of reminders 
     * @param request
     * @return
     */
    @Transactional 
    public List<ReminderResponse> createRemindersList(ReminderCreationRequest request) {
        validateCreationRequest(request);

        UserEntity user = getCurrentUser();

        List<LocalDateTime> dates = determineDateAndTime(request);

        List<Reminder> reminders = dates.stream()
            .map(date -> createReminder(request, date, user))
            .toList();
        

        return mapper.toResponseList(reminderRepo.saveAll(reminders));
    }

    // createReminder() - consider if user set timing manually or chose particular version
    public Reminder createReminder(ReminderCreationRequest request, LocalDateTime dateTime, UserEntity user) {

        Reminder reminder = new Reminder();

        reminder.setContent(request.content());
        reminder.setType(request.type());
        reminder.setIdEvent(request.idEvent());
        reminder.setIdTask(request.idTask());
        reminder.setRemindAt(request.remindAt());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setUser(user);
        reminder.setRemindDate(LocalDate.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth()));
        reminder.setRemindTime(LocalTime.of(dateTime.getHour(), dateTime.getMinute()));
        reminder.setRepeatForAll(request.repeatForAll());

        return reminder;
    }

    public ReminderResponse updateReminder(Long idReminder, ReminderCreationRequest request) {

        Reminder reminder = validateReminderBelongsToUser(idReminder);
        validateCreationRequest(request);

        reminder.setContent(request.content());
        reminder.setRemindDate(request.remindDate());
        reminder.setRemindTime(request.remindTime());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setSentTime(null);

        return mapper.toResponse(reminderRepo.save(reminder));
    }

    public void deleteReminder(Long idReminder) {
        Reminder reminder = validateReminderBelongsToUser(idReminder);

        reminderRepo.delete(reminder);
    }

    private ReminderResponse triggerReminder(Reminder reminder) {

        boolean sent = sseService.sendToUser(reminder.getUser().getIdUser(), mapper.toResponse(reminder));
        
        if (sent) {
            reminder.setStatus(ReminderStatus.SENT);
            reminder.setSentTime(LocalDateTime.now());
        } else {
            reminder.setStatus(ReminderStatus.TRIGGERED);
        }
        
        return mapper.toResponse(reminder);
    }

    @Transactional 
    public void processDueReminders() {
        List<Reminder> reminders = reminderRepo.findByStatusAndRemindDateTime(ReminderStatus.PENDING, ReminderStatus.TRIGGERED, LocalDate.now(), LocalTime.now());
        reminders.forEach(this::triggerReminder);
        reminderRepo.saveAll(reminders);
    }

    public List<LocalDateTime> determineDateAndTime(ReminderCreationRequest request) {
        List<LocalDate> dateOfTarget = new ArrayList<>();
        LocalTime timeOfTarget = null;

        switch (request.type()) {
            case EVENT -> {
                Event event = validateEventBelongsToUser(request.idEvent());

                if (request.remindAt() != Timing.SET_MANUALLY && isFutureOrPresentDate(event.getDateStart(), event.getTimeStart())) {
                    if (event.isRepeated() && request.repeatForAll() == true) {
                        dateOfTarget = event.getDates();
                    } else {
                        dateOfTarget.add(event.getDateStart());
                    }
                } else {
                    throw new InvalidRequestException("PAST_EVENT_DATE", "Date of the chosen event is in the past.");
                }
                
                timeOfTarget = dateOfTarget != null ? event.getTimeStart() : null;
            }
            case TASK -> {
                Task task = validateTaskBelongsToUser(request.idTask());

                if (request.remindAt() != Timing.SET_MANUALLY && isFutureOrPresentDate(task.getStartDate(), task.getTimeStart())) {
                    if (task.isRepeated() && request.repeatForAll() == true) {
                        dateOfTarget = task.getDates();
                    } else {
                        dateOfTarget.add(task.getStartDate());
                    }
                } else {
                    throw new InvalidRequestException("PAST_TASK_DATE", "Date of the chosen task is in the past.");
                }

                timeOfTarget = dateOfTarget != null ? task.getTimeStart() : null;
            }      
            case NONE -> {
            }
        }

        return calculateDateAndTime(request, dateOfTarget, timeOfTarget);
    }

    

    public Event validateEventBelongsToUser(Long idEvent) {
        
        return eventService.validateEventBelongsToUser(idEvent);
    }

    public Task validateTaskBelongsToUser(Long idTask) {
        
        return taskService.validateTaskBelongsToUser(idTask);
    }

    public boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    public boolean isFutureOrPresentDate(LocalDate date, LocalTime time) {
        if (isFutureDate(date)) {
            return true;
        } else if (date != null && date.isEqual(LocalDate.now())) {
            if (time != null && time.isAfter(LocalTime.now())) {
                return true;
            }
        }
        return false;
        
    }

    public List<LocalDateTime> calculateDateAndTime(ReminderCreationRequest request, List<LocalDate> dateOfTarget, LocalTime timeOfTarget) {

        List<LocalDateTime> listOfDateTime = new ArrayList<>();

        switch (request.remindAt()) {
            case AT_TIME -> {
                listOfDateTime = requireTargetDateAndTime(dateOfTarget, timeOfTarget);
            }
            case QUARTER_BEFORE -> {
                listOfDateTime = requireTargetDateAndTime(dateOfTarget, timeOfTarget).stream()
                    .map(dateTime -> dateTime.minusMinutes(15))
                    .toList();
            }
            case HOUR_BEFORE -> {

                listOfDateTime = requireTargetDateAndTime(dateOfTarget, timeOfTarget).stream()
                    .map(dateTime -> dateTime.minusHours(1))
                    .toList();
            }
            case DAY_BEFORE -> {

                listOfDateTime = requireTargetDateAndTime(dateOfTarget, timeOfTarget).stream()
                    .map(dateTime -> dateTime.minusDays(1))
                    .toList();
            }
            case SET_MANUALLY -> {
                
                    listOfDateTime = calculateWhenRepeatedAndManually(dateOfTarget, request);
                
            }
        }
        return listOfDateTime;
    }

    public List<LocalDateTime> calculateWhenRepeatedAndManually(List<LocalDate> dateOfTarget, ReminderCreationRequest request) {
    
        if (dateOfTarget.isEmpty()) {
            return List.of(LocalDateTime.of(request.remindDate(), request.remindTime()));
        }

        LocalDate firstDate = dateOfTarget.get(0);
        ReminderType type = request.type();

        if (firstDate.isBefore(request.remindDate())) {
            if (type == ReminderType.EVENT) {
                throw new InvalidRequestException("DATE_AFTER_EVENT", "Reminder date must be before an event.");
            } else {
                throw new InvalidRequestException("DATE_AFTER_TASK", "Reminder date must be before a task.");
            }
        }

        long differenceInTime = request.remindDate().datesUntil(firstDate).count();

        return dateOfTarget.stream()
            .map(date -> date.minusDays(differenceInTime))
            .map(date -> LocalDateTime.of(date, request.remindTime()))
            .toList();
    }

    private void validateCreationRequest(ReminderCreationRequest request) {
        if (request == null) {
            throw new InvalidRequestException("NO_REMINDER_DATA", "Reminder data is required.");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new InvalidRequestException("REMINDER_TITLE_REQUIRED", "Reminder content is required.");
        }
        if (request.type() == null) {
            throw new InvalidRequestException("REMINDER_TYPE_REQUIRED", "Reminder type is required.");
        }
        if (request.remindAt() == null) {
            throw new InvalidRequestException("REMINDER_TIMING_REQUIRED", "Reminder timing option is required.");
        }
        if (request.type() != ReminderType.NONE && (request.idEvent() == null && request.idTask() == null)) {
            throw new InvalidRequestException("OBJECT_ID_REQUIRED", "Choose an event or task or change type to 'Other'");
        }
        if (request.type() == ReminderType.NONE && request.remindAt() != Timing.SET_MANUALLY) {
            throw new InvalidRequestException("REMINDER_DATE_TIME_REQUIRED", "Reminders of the type 'Other' must have specified date and time manually.");
        }
        if (request.remindAt() == Timing.SET_MANUALLY
            && (request.remindDate() == null || request.remindTime() == null)) {
            throw new InvalidRequestException("REMINDER_DATE_TIME_REQUIRED", "Date and time are required.");
        }
    }

    private List<LocalDateTime> requireTargetDateAndTime(List<LocalDate> dateOfTarget, LocalTime timeOfTarget) {
        if (dateOfTarget.isEmpty() || timeOfTarget == null) {
            throw new InvalidRequestException("OBJECT_DATE_TIME_REQUIRED", "A future target date and time are required");
        }
        return dateOfTarget.stream()
            .map(date -> LocalDateTime.of(date, timeOfTarget))
            .toList();
    }

    public ReminderResponse changeReminderStatus(Long idReminder, ReminderStatus status) {
        Reminder reminder = validateReminderBelongsToUser(idReminder);
        
        reminder.setStatus(status);
        Reminder changed = reminderRepo.save(reminder);
        return mapper.toResponse(changed);
    }

    

    public Reminder validateReminderBelongsToUser(Long idReminder) {
        Long idUser = currentUserService.getCurrentUserId();
        return reminderRepo.findByIdAndIdUser(idReminder, idUser)
            .orElseThrow(() -> new NotFoundException("Reminder", idReminder));
    }

    public void deleteRemindersByUser(UserEntity user) {
        reminderRepo.deleteByUser(user);
    }

    private UserEntity getCurrentUser() {
        Long idUser = currentUserService.getCurrentUserId();
        return userRepo.findById(idUser)
            .orElseThrow(() -> new NotFoundException("User", idUser));
    }

}
