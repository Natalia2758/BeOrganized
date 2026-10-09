package com.example.task.service;

import com.example.task.model.Subtask;
import com.example.task.model.Task;
import com.example.task.dto.SubtaskCompletedRequest;
import com.example.task.dto.TaskFinishedRequest;
import com.example.task.dto.SubtaskResponse;
import com.example.task.dto.TaskCreateRequest;
import com.example.task.dto.QuickTaskCreateRequest;
import com.example.task.dto.TaskResponse;
import com.example.task.mapper.SubtaskMapper;
import com.example.task.mapper.TaskMapper;
import com.example.task.repository.SubtaskRepository;
import com.example.task.repository.TaskRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;
import com.example.task.dto.TaskFilter;

import org.springframework.transaction.annotation.Transactional;

import com.example.category.service.CategoryService;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.common.throwables.exception.TimeBlockOccupiedException;
import com.example.category.model.Category;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

/**
 * <b>TaskService</b>
 * <br>
 * Service class that handles all business logic and operations related to tasks, inlcuding creating, deleting, retrieving, and helper functions
 */
@Service
public class TaskService {

    private final TaskRepository taskRepo;
    private final TaskMapper taskMapper;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final SubtaskRepository subtaskRepo;
    private final SubtaskMapper subtaskMapper;

    public TaskService(
        TaskRepository taskRepo,
        TaskMapper taskMapper,
        CategoryService categoryService,
        CurrentUserService currentUserService,
        UserRepository userRepo,
        SubtaskRepository subtaskRepo,
        SubtaskMapper subtaskMapper
    ) {
        this.taskRepo = taskRepo;
        this.taskMapper = taskMapper;
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.subtaskRepo = subtaskRepo;
        this.subtaskMapper = subtaskMapper;
    }

    /**
     * Retrieves all tasks created by the user
     * @return a list of task response DTO
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        UserEntity user = getCurrentUser();
        return taskMapper.toResponseList(taskRepo.findByUser(user));
    }

    /**
     * Retrieves tasks fetched according to the provided filters
     * @param filter    a DTO with filtering attributes
     * @return          a list of task response DTO
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getFilteredTasks(TaskFilter filter) {

        UserEntity user = getCurrentUser();
        List<Task> tasks = taskRepo.filterTasks(
            user,
            filter.finished(),
            filter.idCategory(),
            filter.search()
        );

        return taskMapper.toResponseList(tasks);
    }

    /**
     * Determines what is the current time and date. Retrieves all user tasks after current date and time.
     * @return a list of response DTO of future tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksInFuture() {
        UserEntity user = getCurrentUser();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        return taskMapper.toResponseList(taskRepo.findWithFutureOrPresentDate(user, date, time));
    }

    /**
     * Retrieves a task by provided ID
     * @param idEvent   an ID of the task
     * @return          an event response DTO of the fetched task
     */
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long idTask) {
        Task task = validateTaskBelongsToUser(idTask);
        Task updated = calculateTaskProgress(task);
        return taskMapper.toResponse(updated);
    }

    /**
     * Validates data provided by the user, creates a new task from the data, and saves task to the database
     * @param request  a request DTO containing task data
     * @return          a response DTO with newly created task's data
     */
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        
        UserEntity user = getCurrentUser();

        validateCreationRequest(request);
        
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setCategory(validateCategory(request));
        task.setStartDate(request.startDate());
        task.setTimeStart(request.timeStart());
        task.setTimeEnd(request.timeEnd());
        task.setRepeatedFrequency(request.repeatedFrequency());
        task.setDates(calculateTaskDates(request));
        task.setProgress(0);
        task.setFinished(false);
        task.setUser(user);
        
        task = correctLogicalInconsistencies(request, task);
        Task created = taskRepo.save(task);

        if (request.subtasks() != null && !request.subtasks().isEmpty()) {
           created.setSubtasks(addSubtasks(request.subtasks(), created));
        }
        
        return taskMapper.toResponse(created);
    }

    /**
     * Maps strings of subtasks' titles to the Subtask list and saves the list to the database
     * 
     * @param strings   a list of subtask strings written by the user
     * @param task      a task to which these subtasks belong
     * @return          a list of created Subtask objects
     */
    public List<Subtask> addSubtasks(List<String> strings, Task task) {
        if (strings.isEmpty()) throw new IllegalArgumentException("List of subtasks titles is empty");

        List<Subtask> subtasks = strings.stream()
            .map(string -> subtaskRepo.save(new Subtask(string, false, task)))
            .collect(Collectors.toCollection(ArrayList::new));

       return subtasks;
    }

    /**
     * Validates data provided by the user, creates a new task from the quick task creation request, and saves task to the database
     * @param request  a request DTO containing task data
     * @return          a response DTO with newly created task's data
     */
    public TaskResponse createTaskQuick(QuickTaskCreateRequest request) {
        UserEntity user = getCurrentUser();

        validateQuickCreationRequest(request);

        Task task = new Task();
        task.setTitle(request.title());
        task.setPriority(request.priority());
        task.setStartDate(request.startDate());
        task.setFinished(false);
        task.setUser(user);

        Task created = taskRepo.save(task);

        return taskMapper.toResponse(created);
    }


    /**
     * Validates the task with provided ID belongs to the user, updates task, and returns updated task response DTO
     * @param idCategory    an ID of the task to be updated
     * @param request       a request DTO with the data that the task should be updated with
     * @return              a response DTO with updated task data
     */
    @Transactional
    @Modifying
    public TaskResponse updateTask(Long idTask, TaskCreateRequest request) {
        Task task = validateTaskBelongsToUser(idTask);
            
        validateCreationRequest(request);
        
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCategory(validateCategory(request));
        task.setStartDate(request.startDate());
        task.setDuration(request.duration());
        task.setTimeStart(request.timeStart());
        task.setTimeEnd(request.timeEnd());
        task.setRepeated(request.repeated());
        task.setRepeatedFrequency(request.repeatedFrequency());
        task.getDates().clear();
        task.getDates().addAll(calculateTaskDates(request));
        task.setPriority(request.priority());

        List<String> requestSubtasks = request.subtasks() == null ? new ArrayList<>() : request.subtasks();
        task.getSubtasks().clear();
        if (!requestSubtasks.isEmpty()) {
            task.getSubtasks().addAll(addSubtasks(requestSubtasks, task));
        }

        return taskMapper.toResponse(calculateTaskProgress(task));
    }

    /**
     * Updates task's finish status
     * @param request   a request DTO with the changed finish status 
     * @return          a task response DTO with updated status
     */
    @Transactional
    public TaskResponse setTaskFinished(TaskFinishedRequest request) {
        Task task = validateTaskBelongsToUser(request.idTask());

        task.setFinished(request.finished());
        Task updated = calculateTaskProgress(task);

        return taskMapper.toResponse(updated);
    }

    /**
     * Helper method used to calculate task progress including subtask completion, if existing, and save the value to the database.
     * @param task  task which progress will be calculated
     * @return      a task with updated progress level
     */
    public Task calculateTaskProgress(Task task) {
        List<Subtask> subtasks = subtaskRepo.findByTask(task);
        long percentage;
        
        if (subtasks.size() > 0) {
            long completedSubtasks = subtasks.stream()
                .filter(subtask -> subtask.isCompleted() == true)
                .count();
            percentage = Math.round((double) completedSubtasks / subtasks.size() * 100);
            boolean finished = percentage == 100 ? true : false;
            task.setFinished(finished);
        } else {
            percentage = task.isFinished() ? 100 : 0;
        }
        
        task.setProgress(percentage);
        return taskRepo.save(task);
    }
    
    /**
     * Updates single subtask completion status anc triggers task progress recalculation.
     * @param request   a request DTO with the changed completion status 
     * @return          a subtask with updated completion status
     */
    @Transactional
    public SubtaskResponse setSubtaskCompleted(SubtaskCompletedRequest request) {
        Subtask subtask = subtaskRepo.findById(request.idSubtask())
            .orElseThrow(() -> new NotFoundException("Subtask", request.idSubtask()));
        
        validateTaskBelongsToUser(subtask.getTask().getIdTask());

        subtask.setCompleted(request.completed());
        subtaskRepo.save(subtask);
        calculateTaskProgress(subtask.getTask());

        return subtaskMapper.toResponse(subtask);
    }

    /**
     * Validates the task with provided ID belongs to the user and deletes the task
     * @param idTask an ID of the task to be deleted
     */
    @Transactional
    public void deleteTask(Long idTask) {
        Task task = validateTaskBelongsToUser(idTask);
        taskRepo.delete(task);
    }

    /**
     * Validates the task belongs to a specific user
     * @param idTask    an ID of an task  to be checked
     * @return           an object of the verified task 
     */
    public Task validateTaskBelongsToUser(Long idTask) {
        Long idUser = currentUserService.getCurrentUserId();
        
        return taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)
            .orElseThrow(() -> new NotFoundException("Task", idTask));
    }

    /**
     * Checks if any other task is assigned with the particular date and time to avoid overlapping timeblocks
     * @param date          a date of the task
     * @param timeStart     a time of the task start
     * @param timeEnd       a time of the task end
     * @return              true if other task exists within this timeblock, true if not
     */
    public boolean isTimeblockOccupied(LocalDate date, LocalTime timeStart, LocalTime timeEnd) {
        return taskRepo.existsOverlappingTimeBlock(date, timeStart, timeEnd);
    }

    /**
     * Ensures the provided task creation request contains correct data
     * @param request a request DTO with task data provided by the user
     */
    public void validateCreationRequest(TaskCreateRequest request) {
        if (request == null){
            throw new InvalidRequestException("NO_TASK_DATA","Task data is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidRequestException("TASK_TITLE_REQUIRED", "Task title is required");
        }
        if (request.startDate() != null && request.timeStart() != null && request.timeEnd() != null
                && isTimeblockOccupied(request.startDate(), request.timeStart(), request.timeEnd())) {
            throw new TimeBlockOccupiedException(request.startDate(), request.timeStart(), request.timeEnd());
        }
        if (request.startDate() != null && (request.startDate().isBefore(LocalDate.now()))) {
            throw new InvalidRequestException("TASK_DATE_IN_PAST", "Date must be in the future");
        }
        if (request.duration() && (request.timeStart() == null || request.timeEnd() == null)) {
            throw new InvalidRequestException("TASK_TIME_REQUIRED","Please provide start and end time");
        }
        if (request.repeated() && request.repeatedFrequency() == null) {
            throw new InvalidRequestException("TASK_REPETITION_FREQUENCY_REQUIRED", "Please choose the repetition frequency");
        }
    }

    /**
     * Ensures the provided quick task creation request contains correct data
     * @param request a request DTO with task data provided by the user
     */
    public void validateQuickCreationRequest(QuickTaskCreateRequest request) {
        if (request == null){
            throw new InvalidRequestException("NO_TASK_DATA", "Task data is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidRequestException("TASK_TITLE_REQUIRED", "Task title is required");
        }
        if (request.startDate() != null && (request.startDate().isBefore(LocalDate.now()))) {
            throw new InvalidRequestException("TASK_DATE_IN_PAST", "Date must be in the future");
        }
    }

    /**
     * Validates that selected category exists and belongs to the user
     * @param request   a task creation request DTO
     * @return          a Category object if it exsists and belongs to the user
     */
    public Category validateCategory(TaskCreateRequest request) {
        if (request.idCategory() != null && categoryService.categoryExistsById(request.idCategory())) {
            Category category;
            category = categoryService.validateCatBelongsToUser(request.idCategory());
            return category;
        } else {
            return null;
        }
    }

    /**
     * Helper method to ensure logical consistency of the data being saved
     * @param request   a task creation request DTO 
     * @param task     a task object
     * @return          a task object with resolved inconsistencies
     */
    public Task correctLogicalInconsistencies(TaskCreateRequest request, Task task) {
        
        if (request.timeStart() != null && request.timeEnd() != null && !request.duration()) {
            task.setDuration(!request.duration());
        } else {
            task.setDuration(request.duration());
        }
        if (request.repeatedFrequency() != null && !request.repeated()) {
            task.setRepeated(!request.repeated());
        } else {
            task.setRepeated(request.repeated());
        }
        return task;
    }

    /**
     * Calculates dates of the task in case when user wants the task to be repeated in time
     * @param task a task creation request DTO
     * @return      a list of dates for the task
     */
    public List<LocalDate> calculateTaskDates(TaskCreateRequest task) {

        if (task.repeatedFrequency() == null || task.startDate() == null) return new ArrayList<>();

        LocalDate first = task.startDate();
        LocalDate firstDayNextYear = LocalDate.of(first.getYear() + 1, 1, 1);
        List<LocalDate> dates = new ArrayList<>();

        switch (task.repeatedFrequency()) {
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
        return dates;
    }

    /**
     * Retrieves tasks filtered by the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return  a list of response DTOs representing filtered tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByDate(int year, int month, int day) {
        UserEntity user = getCurrentUser();
        List<Task> tasks = taskRepo.findByStartDate(user, LocalDate.of(year, month, day));
        return taskMapper.toResponseList(tasks);
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

    /**
     * Removes category relationships from all tasks assigned to the specific category
     * @param idCategory    an ID of the category
     * @param user          an object with user data
     */
    public void clearCategory(Long idCategory, UserEntity user) {
        taskRepo.clearCategoryByCategoryAndUser(idCategory, user);
    }

    /**
     * Deletes all tasks that belong to the user
     * @param user an object with the user data
     */
    @Transactional
    public void deleteTasksByUser(UserEntity user) {
        subtaskRepo.deleteByUser(user);
        taskRepo.deleteByUser(user);
    }

}
