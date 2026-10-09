package com.example.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
import com.example.common.throwables.exception.TimeBlockOccupiedException;
import com.example.task.dto.TaskCreateRequest;
import com.example.task.dto.TaskFilter;
import com.example.task.dto.TaskFinishedRequest;
import com.example.task.dto.TaskResponse;
import com.example.task.enums.Priority;
import com.example.task.mapper.SubtaskMapper;
import com.example.task.mapper.TaskMapper;
import com.example.task.model.Subtask;
import com.example.task.model.Task;
import com.example.task.repository.SubtaskRepository;
import com.example.task.repository.TaskRepository;
import com.example.task.service.TaskService;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private TaskMapper mapper;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private SubtaskRepository subtaskRepo;

    @Mock
    private SubtaskMapper subtaskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasksReturnsTasksByUserId() {
        Long idUser = 10L;
        UserEntity user = authenticatedUser(idUser);

        TaskResponse response1 = mock(TaskResponse.class);
        TaskResponse response2 = mock(TaskResponse.class);
        List<TaskResponse> taskList = List.of(response1, response2);

        Task task1 = new Task();
        Task task2 = new Task();
        task1.setUser(user);
        task2.setUser(user);

        List<Task> tasksByUser = List.of(task1, task2);

        when(taskRepo.findByUser(user)).thenReturn(tasksByUser);
        when(mapper.toResponseList(tasksByUser)).thenReturn(taskList);

        List<TaskResponse> result = taskService.getAllTasks();

        assertEquals(taskList, result);
        verify(taskRepo).findByUser(user);
        verify(mapper).toResponseList(tasksByUser);
    }

    @Test
    void createTaskReturnsTaskResponseWhenSuccessfullyCreated() {
        UserEntity user = authenticatedUser(1L);

        TaskCreateRequest request = createRequest(
            "Title",
            null,
            false,
            null,
            null,
            false,
            null,
            null,
            List.of()
        );

        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(Task.class))).thenReturn(expectedResponse);

        TaskResponse result = taskService.createTask(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepo).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertEquals(request.title(), savedTask.getTitle());
        assertEquals(request.description(), savedTask.getDescription());
        assertEquals(request.priority(), savedTask.getPriority());
        assertEquals(user, savedTask.getUser());
        assertFalse(savedTask.isFinished());
        assertEquals(0, savedTask.getProgress());
        assertTrue(savedTask.getDates().isEmpty());

        assertEquals(expectedResponse, result);

        verify(mapper).toResponse(savedTask);
    }

    @Test
    void createTaskAssignsValidCategoryWhenCategoryIsProvided() {
        authenticatedUser(1L);

        Long idCategory = 2L;

        Category category = new Category();

        TaskCreateRequest request = createRequest(
            "Title",
            null,
            false,
            null,
            null,
            false,
            null,
            idCategory,
            List.of()
        );

        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(categoryService.categoryExistsById(idCategory)).thenReturn(true);

        when(categoryService.validateCatBelongsToUser(idCategory)).thenReturn(category);

        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(Task.class))).thenReturn(expectedResponse);

        TaskResponse result = taskService.createTask(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepo).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertEquals(category, savedTask.getCategory());
        assertEquals(expectedResponse, result);

        verify(categoryService).categoryExistsById(idCategory);
        verify(categoryService).validateCatBelongsToUser(idCategory);
    }

    @Test
    void createTaskCorrectsDurationAndRepeatedValuesWhenRelatedValuesAreProvided() {
        authenticatedUser(1L);

        TaskCreateRequest request = createRequest(
            "Title",
            null,
            false,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            false,
            RepeatedFrequency.EVERY_WEEK,
            null,
            List.of()
        );

        when(taskRepo.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(Task.class))).thenReturn(mock(TaskResponse.class));

        taskService.createTask(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepo).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertTrue(savedTask.hasDuration());
        assertTrue(savedTask.isRepeated());
    }

    @Test
    void createTaskDoesNotSaveWhenTimeBlockIsOccupied() {
        authenticatedUser(1L);

        LocalDate date = LocalDate.now().plusDays(1);

        TaskCreateRequest request = createRequest(
            "Title",
            date,
            true,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            false,
            null,
            null,
            List.of()
        );

        when(taskRepo.existsOverlappingTimeBlock(
            date,
            request.timeStart(),
            request.timeEnd()
        )).thenReturn(true);

        assertThrows(TimeBlockOccupiedException.class, () -> taskService.createTask(request));

        verify(taskRepo, never()).save(any(Task.class));
        verify(mapper, never()).toResponse(any(Task.class));
    }

    @Test
    void getTaskReturnsTaskWhenExists() {
        Long idTask = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Task task = new Task();
        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.of(task));

        when(subtaskRepo.findByTask(task)).thenReturn(new ArrayList<>());

        when(taskRepo.save(task)).thenReturn(task);

        when(mapper.toResponse(task)).thenReturn(expectedResponse);

        TaskResponse result = taskService.getTask(idTask);

        assertEquals(expectedResponse, result);

        verify(taskRepo).findByIdAndIdUserWithCategory(idTask, idUser);
        verify(subtaskRepo).findByTask(task);
        verify(taskRepo).save(task);
        verify(mapper).toResponse(task);
    }

    @Test
    void getTaskThrowsExceptionWhenDoesNotExist() {
        Long idTask = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.getTask(idTask));

        verify(taskRepo).findByIdAndIdUserWithCategory(idTask, idUser);

        verify(subtaskRepo, never()).findByTask(any(Task.class));
        verify(mapper, never()).toResponse(any(Task.class));
    }

    @Test
    void updateTaskKeepsTaskValidWhenSubtaskListIsEmpty() {
        Long idTask = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Task task = new Task();
        task.setSubtasks(new ArrayList<>());

        TaskCreateRequest request = createRequest(
            "Updated title",
            null,
            false,
            null,
            null,
            false,
            null,
            null,
            List.of()
        );

        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.of(task));
        when(subtaskRepo.findByTask(task)).thenReturn(new ArrayList<>());
        when(taskRepo.save(task)).thenReturn(task);
        when(mapper.toResponse(task)).thenReturn(expectedResponse);

        TaskResponse result = taskService.updateTask(idTask, request);

        assertEquals(expectedResponse, result);
        verify(taskRepo).findByIdAndIdUserWithCategory(idTask, idUser);
        verify(subtaskRepo).findByTask(task);
        verify(taskRepo).save(task);
        verify(mapper).toResponse(task);
    }

    @Test
    void setTaskFinishedUpdatesFinishedStatusWhenTaskExists() {
        Long idTask = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Task task = new Task();
        task.setFinished(false);

        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.of(task));

        when(subtaskRepo.findByTask(task)).thenReturn(new ArrayList<>());

        when(taskRepo.save(task)).thenReturn(task);

        when(mapper.toResponse(task)).thenReturn(expectedResponse);

        TaskResponse result = taskService.setTaskFinished(new TaskFinishedRequest(idTask, true));

        assertTrue(task.isFinished());
        assertEquals(100, task.getProgress());
        assertEquals(expectedResponse, result);

        verify(taskRepo).save(task);
        verify(mapper).toResponse(task);
    }

    @Test
    void deleteTaskDeletesTaskWhenItExists() {
        Long idTask = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        Task task = new Task();

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.of(task));

        taskService.deleteTask(idTask);

        verify(taskRepo).delete(task);

        verify(subtaskRepo, never()).delete(any(Subtask.class));
    }

    @Test
    void getFilteredTasksReturnsFilteredTasksWhereCriteriaApply() {
        Long idUser = 10L;
        UserEntity user = authenticatedUser(idUser);

        TaskFilter filter = new TaskFilter(false, null, null);

        Task task1 = new Task();
        Task task2 = new Task();

        task1.setFinished(false);
        task2.setFinished(false);

        List<Task> filteredTasks = List.of(task1, task2);

        List<TaskResponse> expectedResponse = List.of(mock(TaskResponse.class), mock(TaskResponse.class));

        when(taskRepo.filterTasks(
            user,
            filter.finished(),
            filter.idCategory(),
            filter.search()
        )).thenReturn(filteredTasks);

        when(mapper.toResponseList(filteredTasks)).thenReturn(expectedResponse);

        List<TaskResponse> result = taskService.getFilteredTasks(filter);

        assertEquals(expectedResponse, result);

        verify(taskRepo).filterTasks(
            user,
            filter.finished(),
            filter.idCategory(),
            filter.search()
        );

        verify(mapper).toResponseList(filteredTasks);
    }

    @Test
    void validateTaskBelongsToUserReturnsTaskIfYes() {
        Long idTask = 1L;
        Long idUser = 10L;
        authenticatedUser(idUser);
        Task task = new Task();

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.of(task));

        Task result = taskService.validateTaskBelongsToUser(idTask);

        assertEquals(task, result);
        verify(taskRepo).findByIdAndIdUserWithCategory(idTask, idUser);
    }

    @Test
    void validateTaskBelongsToUserThrowsExceptionWhenTaskNotExists() {
        Long idTask = 1L;
        Long idUser = 10L;

        authenticatedUser(idUser);

        when(taskRepo.findByIdAndIdUserWithCategory(idTask, idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.validateTaskBelongsToUser(idTask));
        verify(taskRepo).findByIdAndIdUserWithCategory(idTask, idUser);
    }

    @Test
    void isTimeBlockOccupiedReturnsTrueIfDateAndTimeOverlap() {
        LocalDate date = LocalDate.of(2026, 9, 1);
        LocalTime timeStart = LocalTime.of(10, 20);
        LocalTime timeEnd = LocalTime.of(11, 20);

        when(taskRepo.existsOverlappingTimeBlock(date, timeStart, timeEnd)).thenReturn(true);

        boolean result = taskService.isTimeblockOccupied(date, timeStart, timeEnd);

        assertTrue(result);
        verify(taskRepo).existsOverlappingTimeBlock(date, timeStart, timeEnd);
    }

    @Test
    void isTimeBlockOccupiedReturnsFalseIfNoOverlaps() {
        LocalDate date = LocalDate.of(2026, 10, 20);
        LocalTime timeStart = LocalTime.of(6, 0);
        LocalTime timeEnd = LocalTime.of(8, 0);

        when(taskRepo.existsOverlappingTimeBlock(date, timeStart, timeEnd)).thenReturn(false);

        boolean result = taskService.isTimeblockOccupied(date, timeStart, timeEnd);

        assertFalse(result);

        verify(taskRepo).existsOverlappingTimeBlock(date, timeStart, timeEnd);
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenRequestIsNull() {
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.validateCreationRequest(null));

        assertEquals("Task data is required", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenTitleIsBlank() {
        TaskCreateRequest request = createRequest(
            "",
            null,
            false,
            null,
            null,
            false,
            null,
            null,
            List.of()
        );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.validateCreationRequest(request));

        assertEquals("Task title is required", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenDateInPast() {
        TaskCreateRequest request = createRequest(
            "Task title",
            LocalDate.of(2020, 10, 20),
            false,
            null,
            null,
            false,
            null,
            null,
            List.of()
        );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.validateCreationRequest(request));

        assertEquals("Date must be in the future",exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenDurationWithoutStartTime() {
        TaskCreateRequest request = createRequest(
            "Task title",
            LocalDate.now().plusDays(1),
            true,
            null,
            LocalTime.of(10, 20),
            false,
            null,
            null,
            List.of()
        );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.validateCreationRequest(request));

        assertEquals("Please provide start and end time", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenRepeatedWithoutFrequency() {
        TaskCreateRequest request = createRequest(
            "Task title",
            null,
            false,
            null,
            null,
            true,
            null,
            null,
            List.of()
        );

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> taskService.validateCreationRequest(request) );

        assertEquals("Please choose the repetition frequency", exception.getMessage());
    }

    @Test
    void validateCreationRequestThrowsExceptionWhenTimeBlockIsOccupied() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        TaskCreateRequest request = createRequest(
            "Task title",
            date,
            true,
            start,
            end,
            false,
            null,
            null,
            List.of()
        );

        when(taskRepo.existsOverlappingTimeBlock(date, start, end)).thenReturn(true);

        assertThrows(TimeBlockOccupiedException.class, () -> taskService.validateCreationRequest(request));

        verify(taskRepo).existsOverlappingTimeBlock(date, start, end);
    }

    @Test
    void calculateTaskDatesReturnsDatesListWhenRepeatedEveryday() {
        LocalDate startDate = LocalDate.of(2026, 12, 10);

        TaskCreateRequest request = createRequest(
            "Task title",
            startDate,
            false,
            null,
            null,
            true,
            RepeatedFrequency.EVERYDAY,
            null,
            List.of()
        );

        List<LocalDate> result = taskService.calculateTaskDates(request);

        assertEquals(22, result.size());
        assertEquals(startDate, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 31), result.get(result.size() - 1));
    }

    @Test
    void calculateTaskDatesReturnsDatesListWhenRepeatedEveryWeek() {
        LocalDate startDate = LocalDate.of(2026, 12, 1);

        TaskCreateRequest request = createRequest(
            "Task title",
            startDate,
            false,
            null,
            null,
            true,
            RepeatedFrequency.EVERY_WEEK,
            null,
            List.of()
        );

        List<LocalDate> result = taskService.calculateTaskDates(request);

        assertEquals(5, result.size());
        assertEquals(startDate, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 29), result.get(result.size() - 1));
    }

    @Test
    void calculateTaskDatesReturnsDatesListWhenRepeatedEveryTwoWeeks() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);

        TaskCreateRequest request = createRequest(
            "Task title",
            startDate,
            false,
            null,
            null,
            true,
            RepeatedFrequency.EVERY_TWO_WEEKS,
            null,
            List.of()
        );

        List<LocalDate> result = taskService.calculateTaskDates(request);

        assertEquals(5, result.size());
        assertEquals(startDate, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 27), result.get(result.size() - 1));
    }

    @Test
    void calculateTaskDatesReturnsDatesListWhenRepeatedEveryMonth() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);

        TaskCreateRequest request = createRequest(
            "Task title",
            startDate,
            false,
            null,
            null,
            true,
            RepeatedFrequency.EVERY_MONTH,
            null,
            List.of()
        );

        List<LocalDate> result = taskService.calculateTaskDates(request);

        assertEquals(3, result.size());
        assertEquals(startDate, result.get(0));
        assertEquals(LocalDate.of(2026, 12, 1), result.get(result.size() - 1));
    }

    @Test
    void calculateTaskDatesReturnsEmptyListWhenNotRepeated() {
        TaskCreateRequest request = createRequest(
            "Task title",
            LocalDate.of(2026, 10, 1),
            false,
            null,
            null,
            false,
            RepeatedFrequency.NO_REPEAT,
            null,
            List.of()
        );

        List<LocalDate> result = taskService.calculateTaskDates(request);

        assertTrue(result.isEmpty());
    }

    private UserEntity authenticatedUser(Long idUser) {
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        lenient().when(currentUserService.getCurrentUserId()).thenReturn(idUser);

        lenient().when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

        return user;
    }

    private TaskCreateRequest createRequest(
        String title,
        LocalDate startDate,
        boolean duration,
        LocalTime timeStart,
        LocalTime timeEnd,
        boolean repeated,
        RepeatedFrequency repeatedFrequency,
        Long idCategory,
        List<String> subtasks
    ) {
        return new TaskCreateRequest(
            title,
            null,
            Priority.MEDIUM,
            idCategory,
            startDate,
            duration,
            timeStart,
            timeEnd,
            repeated,
            repeatedFrequency,
            subtasks
        );
    }
}
