package com.example.task.controller;


import com.example.task.dto.SubtaskCompletedRequest;
import com.example.task.dto.SubtaskResponse;
import com.example.task.dto.TaskFinishedRequest;
import com.example.task.dto.TaskCreateRequest;
import com.example.task.dto.QuickTaskCreateRequest;
import com.example.task.dto.TaskResponse;
import com.example.task.service.TaskService;
import com.example.task.dto.TaskFilter;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 
 * <b>TaskController</b>
 * <br>
 * REST controller responsible for communication and task data exchange with the frontend side of application
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Receives filtering criteria and responds with filtered user tasks
     * @param filter    a DTO representing criteria to filter tasks
     * @return          a list of filtered tasks mapped to response DTOs
     */
    @GetMapping
    public List<TaskResponse> getTasks(@ModelAttribute TaskFilter filter) {
        return taskService.getFilteredTasks(filter);
    }

    /**
     * Sends response with a task data by the provided ID
     * @param idEvent   an ID of the requested task
     * @return          a response DTO with an task data
     */
    @GetMapping("/{idTask}")
    public TaskResponse getTask(@PathVariable("idTask") Long idTask) {
        return taskService.getTask(idTask);
    }
    
    /**
     * Receives a request DTO with data of a task to be created
     * @param request   a request DTO with task data provided by the user
     * @return          a response DTO with a created task
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request);
    }

    /**
     * Receives a shortened version of a request DTO with data of a task to be created
     * @param request   a request DTO with task data provided by the user
     * @return          a response DTO with a created task
     */
    @PostMapping("/create-quick")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTaskQuick(@Valid @RequestBody QuickTaskCreateRequest request) {
        return taskService.createTaskQuick(request);
    }
    
    /**
     * Receives a request DTO with modified data of an existing task
     * @param idEvent   an ID of the task to be modified
     * @param request   a request DTO with the task to be changed
     * @return          a response DTO with modified task data
     */
    @PutMapping("/{idTask}")
    public TaskResponse updateTask(@PathVariable("idTask") Long idTask, @Valid @RequestBody TaskCreateRequest request) {
        return taskService.updateTask(idTask, request);
    }

    /**
     * Receives a request to delete an task
     * @param idEvent    an ID of the task to be deleted
     */
    @DeleteMapping("/{idTask}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("idTask") Long idTask) {
        taskService.deleteTask(idTask);
    }

    /**
     * Receives a request to change a finished status of a task
     * @param request   a DTO with status value and task ID
     * @return          a task response DTO with changed finished status
     */
    @PatchMapping("/task")
    public TaskResponse setTaskFinished(@Valid @RequestBody TaskFinishedRequest request) {
        return taskService.setTaskFinished(request);
    }

    /**
     * Receives a request to change a completion status of a subtask
     * @param request   a DTO with status value and subtask ID
     * @return          a subtask response DTO with changed completion status
     */
    @PatchMapping("/subtask")
    public SubtaskResponse updateCompletion(@Valid @RequestBody SubtaskCompletedRequest request) {
        return taskService.setSubtaskCompleted(request);
    }

    /**
     * Sends all tasks filtered according to the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return      a list of response DTOs representing filtered tasks
     */
    @GetMapping("/by-date")
    public List<TaskResponse> getTasksByDate(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("day") int day) {
        return taskService.getTasksByDate(year, month, day);
    }

    /**
     * Sends all events whose start date is in future
     * @return  a list of response DTOs representing future events
     */
    @GetMapping("/future")
    public List<TaskResponse> getTasksInFuture() {
        return taskService.getTasksInFuture();
    }
    
    
    
}
