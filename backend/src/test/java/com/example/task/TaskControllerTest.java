package com.example.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.example.task.controller.TaskController;
import com.example.task.dto.QuickTaskCreateRequest;
import com.example.task.dto.SubtaskCompletedRequest;
import com.example.task.dto.SubtaskResponse;
import com.example.task.dto.TaskCreateRequest;
import com.example.task.dto.TaskFilter;
import com.example.task.dto.TaskFinishedRequest;
import com.example.task.dto.TaskResponse;
import com.example.task.service.TaskService;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void getTasksReturnsTaskList() throws Exception {

        TaskResponse task = mock(TaskResponse.class);

        when(taskService.getFilteredTasks(any(TaskFilter.class))).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).getFilteredTasks(any(TaskFilter.class));
    }

    @Test 
    void getTasksReturnsTaskListWhenFiltersProvided() throws Exception {
        when(taskService.getFilteredTasks(any(TaskFilter.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks")
                .param("finished", "false")
                .param("idCategory", "10")
                .param("search", "title")
            )
            .andExpect(status().isOk());
        
        ArgumentCaptor<TaskFilter> captor = ArgumentCaptor.forClass(TaskFilter.class);

        verify(taskService).getFilteredTasks(captor.capture());

        TaskFilter filter = captor.getValue();

        assertFalse(filter.finished());
        assertEquals(10L, filter.idCategory());
        assertEquals("title", filter.search());
    }

    @Test 
    void getTasksReturnsBadRequestWhenIncorrectFilters() throws Exception {

        mockMvc.perform(get("/api/tasks")
                .param("finished", "abc")
            )
            .andExpect(status().isBadRequest());

        verify(taskService, never()).getFilteredTasks(any(TaskFilter.class));
    }

    @Test 
    void getTaskReturnsTask() throws Exception {
        Long idTask = 1L;
        TaskResponse task = mock(TaskResponse.class);

        when(taskService.getTask(idTask)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/{idTask}", idTask))
            .andExpect(status().isOk());
            
        verify(taskService).getTask(idTask);
    }

    @Test 
    void getTaskReturnsBadRequestWhenIncorrectId() throws Exception {
        
        mockMvc.perform(get("/api/tasks/{idTask}", "abc"))
            .andExpect(status().isBadRequest());

        verify(taskService, never()).getTask(anyLong());
    }

    @Test 
    void createTaskReturnsCreatedStatusWhenValidRequest() throws Exception {
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "study",
                        "duration": "false",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isCreated());

        verify(taskService).createTask(any(TaskCreateRequest.class));

    }

    @Test 
    void createTaskReturnsBadRequestStatusWhenTitleIsBlank() throws Exception {

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "duration": "false",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(TaskCreateRequest.class));
    }

    @Test 
    void createTaskReturnsBadRequestStatusWhenPastDate() throws Exception {

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "study",
                        "startDate": "2025-03-02",
                        "duration": "false",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(TaskCreateRequest.class));
    }

    @Test 
    void createTaskReturnsBadRequestStatusWhenIncorrectPriority() throws Exception {

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "study",
                        "priority": "NO",
                        "duration": "false",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(TaskCreateRequest.class));
    }

    @Test 
    void createTaskReturnsBadRequestStatusWhenStartAfterEnd() throws Exception {

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "study",
                        "startDate": "2026-12-10",
                        "duration": "true",
                        "timeStart": "11:00",
                        "timeEnd": "10:00",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(TaskCreateRequest.class));
    }

    @Test 
    void createTaskReturnsBadRequestStatusWhenIncorrectDateFormat() throws Exception {

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "study",
                        "startDate": "10.12.2026",
                        "duration": "false",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(TaskCreateRequest.class));
    }

    @Test 
    void createTaskReturnsBadRequestStatusWhenTitleTooLong() throws Exception {

        mockMvc.perform(post("/api/tasks/create")
            .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe1qqqqqqqqqqqqqqqqqqe123",
                        "duration": "false",
                        "repeated": "false"
                    }
                """)
            )
            .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(TaskCreateRequest.class));
    }

    @Test 
    void createTaskQuickReturnsCreatedStatusWhenValidRequest() throws Exception {
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.createTaskQuick(any(QuickTaskCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks/create-quick")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "title": "study",
                    "priority": "LOW",
                    "startDate": "2026-12-10"
                }
            """)
            )
            .andExpect(status().isCreated());

        verify(taskService).createTaskQuick(any(QuickTaskCreateRequest.class));
    }

    @Test 
    void createTaskQuickReturnsBadRequestStatusWhenIncorrectRequest() throws Exception {
        
        mockMvc.perform(post("/api/tasks/create-quick")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "title": ""
                }
            """)
            )
            .andExpect(status().isBadRequest());

        verify(taskService, never()).createTaskQuick(any(QuickTaskCreateRequest.class));
    }


    @Test 
    void updateTaskReturnsTaskWhenValidRequest() throws Exception {

        Long idTask = 1L;
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.updateTask(eq(idTask), any(TaskCreateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/tasks/{idTask}", idTask)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "updated title"
                    }
                """)
            )
            .andExpect(status().isOk());

        verify(taskService).updateTask(eq(idTask), any(TaskCreateRequest.class));

    }

    @Test 
    void updateTaskPassesCorrectRequestToService() throws Exception {
        Long idTask = 1L;

        when(taskService.updateTask(eq(idTask), any(TaskCreateRequest.class))).thenReturn(mock(TaskResponse.class));

        mockMvc.perform(put("/api/tasks/{idTask}", idTask)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "updated title"
                    }
                """)
            )
            .andExpect(status().isOk());

        ArgumentCaptor<TaskCreateRequest> captor = ArgumentCaptor.forClass(TaskCreateRequest.class);

        verify(taskService).updateTask(eq(idTask), captor.capture());

        TaskCreateRequest request = captor.getValue();

        assertEquals("updated title", request.title());
    }

    @Test 
    void updateTaskReturnsBadRequestStatusWhenInvalidRequest() throws Exception {
        mockMvc.perform(put("/api/tasks/{idTask}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": ""
                    }
                """)
            )
            .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTask(anyLong(), any(TaskCreateRequest.class));
    }

    @Test 
    void deleteTaskReturnsNoContentStatusWhenSuccessfulRequest() throws Exception {
        Long idTask = 1L;

        mockMvc.perform(delete("/api/tasks/{idTask}", idTask))
            .andExpect(status().isNoContent());

        verify(taskService).deleteTask(idTask);
    }

    @Test 
    void setTaskFinishedReturnsTaskWhenSuccessfulRequest() throws Exception {
        TaskResponse response = mock(TaskResponse.class);
        
        when(taskService.setTaskFinished(any(TaskFinishedRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/tasks/task")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "idTask": "1",
                    "finished": "true"
                }
                
            """)
            )
            .andExpect(status().isOk());

        verify(taskService).setTaskFinished(any(TaskFinishedRequest.class));
    }

    @Test 
    void setTaskFinishedPassesCorrectRequestToService() throws Exception {

        when(taskService.setTaskFinished(any(TaskFinishedRequest.class))).thenReturn(mock(TaskResponse.class));

        mockMvc.perform(patch("/api/tasks/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "idTask": "1",
                        "finished": "false"
                    }
                """)
            )
            .andExpect(status().isOk());

        ArgumentCaptor<TaskFinishedRequest> captor = ArgumentCaptor.forClass(TaskFinishedRequest.class);

        verify(taskService).setTaskFinished(captor.capture());

        TaskFinishedRequest request = captor.getValue();

        assertEquals(1L, request.idTask());
        assertEquals(false, request.finished());
    }

    @Test 
    void setTaskFinishedReturnsBadRequestStatusWhenIncorrectRequest() throws Exception {
        mockMvc.perform(patch("/api/tasks/task")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "idTask": "1",
                    "finished": ""
                }
            """)
            )
            .andExpect(status().isBadRequest());

        verify(taskService, never()).setTaskFinished(any(TaskFinishedRequest.class));
    }

    @Test 
    void updateCompletionReturnsSubtaskWhenSuccessfulRequest() throws Exception {
        SubtaskResponse response = mock(SubtaskResponse.class);

        when(taskService.setSubtaskCompleted(any(SubtaskCompletedRequest.class))).thenReturn(response);
        
        mockMvc.perform(patch("/api/tasks/subtask")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "idSubtask": "2",
                        "completed": "true"
                    }
                    """)
            )
            .andExpect(status().isOk());

        verify(taskService).setSubtaskCompleted(any(SubtaskCompletedRequest.class));
    
    }

    @Test 
    void updateCompletionReturnsBadrequestStatusWhenIncorrectRequest() throws Exception {
        
        mockMvc.perform(patch("/api/tasks/subtask")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "idSubtask": "",
                        "completed": "true"
                    }
                    """)
            )
            .andExpect(status().isBadRequest());

        verify(taskService, never()).setSubtaskCompleted(any(SubtaskCompletedRequest.class));
    
    }

    @Test 
    void getTasksByDateReturnsTaskListWhenSuccessfulRequest() throws Exception {

        TaskResponse task = mock(TaskResponse.class);
        when(taskService.getTasksByDate(anyInt(), anyInt(), anyInt())).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/by-date")
                .param("year", "2026")
                .param("month", "10")
                .param("day", "20")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(taskService).getTasksByDate(2026, 10, 20);
    }

    @Test 
    void getTasksByDateReturnsBadRequestWhenMissingParam() throws Exception {

        mockMvc.perform(get("/api/tasks/by-date")
                .param("year", "2026")
                .param("month", "10")
            )
            .andExpect(status().isBadRequest());

        verify(taskService, never()).getTasksByDate(anyInt(), anyInt(), anyInt());
    }

    @Test 
    void getTasksInFutureReturnsTaskList() throws Exception {
        when(taskService.getTasksInFuture()).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/future"))
            .andExpect(status().isOk());

        verify(taskService).getTasksInFuture();
    }
    

}
