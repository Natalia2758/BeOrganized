package com.example.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.task.dto.QuickTaskCreateRequest;
import com.example.task.dto.SubtaskCompletedRequest;
import com.example.task.dto.SubtaskResponse;
import com.example.task.dto.TaskCreateRequest;
import com.example.task.dto.TaskFinishedRequest;
import com.example.task.dto.TaskResponse;
import com.example.task.enums.Priority;
import com.example.task.model.Subtask;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class TaskApiIntegrationTest {

    @Container
    @ServiceConnection 
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired 
    TestRestTemplate restTemplate;

    @Autowired
    TaskRepository taskRepo;

    @Autowired 
    UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    @BeforeEach 
    void setUp() {
        taskRepo.deleteAll();
        userRepo.deleteAll();
        restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }

    @Test
    void shouldCreateTask() {
        // ARRANGE
        UserEntity user = createUser();

        TaskCreateRequest request = new TaskCreateRequest(
            "Study",
            null,
            Priority.HIGH,
            null,
            LocalDate.now().plusDays(1),
            false,
            null,
            null,
            false,
            null,
            List.of()
        );

        HttpHeaders headers = authenticatedHeaders();

        HttpEntity<TaskCreateRequest> requestEntity =
            new HttpEntity<>(request, headers);

        // ACT
        ResponseEntity<TaskResponse> response =
            restTemplate.postForEntity(
                "/api/tasks/create",
                requestEntity,
                TaskResponse.class
            );

        // ASSERT - HTTP
        assertTrue(response.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(response.getBody() != null);

        // ASSERT - RESPONSE
        TaskResponse body = response.getBody();

        assertTrue(body.title().equals("Study"));
        assertTrue(body.progress() == 0);
        assertFalse(body.finished());

        // ASSERT - DATABASE
        Long idTask = body.idTask();

        Task saved = taskRepo.findById(idTask)
            .orElseThrow();

        assertTrue(saved.getTitle().equals("Study"));
        assertTrue(saved.getUser().getIdUser() == user.getIdUser());
        assertTrue(saved.getProgress() == 0);
        assertFalse(saved.isFinished());
        assertTrue(saved.getSubtasks().isEmpty());
    }

    @Test
    void shouldCreateTaskWithSubtasks() {
        // ARRANGE
        createUser();

        TaskCreateRequest request = new TaskCreateRequest(
            "Study",
            null,
            Priority.HIGH,
            null,
            LocalDate.now().plusDays(1),
            false,
            null,
            null,
            false,
            null,
            List.of(
                "Do homework",
                "Prepare presentation",
                "Learn biology concepts"
            )
        );

        HttpHeaders headers = authenticatedHeaders();

        HttpEntity<TaskCreateRequest> requestEntity =
            new HttpEntity<>(request, headers);

        // ACT
        ResponseEntity<TaskResponse> response =
            restTemplate.postForEntity(
                "/api/tasks/create",
                requestEntity,
                TaskResponse.class
            );

        // ASSERT - HTTP
        assertTrue(response.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(response.getBody() != null);


        // ASSERT - DATABASE
        Long idTask = response.getBody().idTask();

        Task saved = taskRepo.findById(idTask)
            .orElseThrow();

        List<Subtask> subtasks = saved.getSubtasks();

        assertTrue(subtasks.size() == 3);
        assertTrue(subtasks.stream()
            .allMatch(subtask -> subtask.getTask().getIdTask().equals(saved.getIdTask()))
        );
    }

    @Test
    void shouldReturn400StatusWhenBlankTaskTitle() {
        // ARRANGE
        createUser();

        TaskCreateRequest request = new TaskCreateRequest(
            "",
            null,
            Priority.HIGH,
            null,
            LocalDate.now().plusDays(1),
            false,
            null,
            null,
            false,
            null,
            List.of()
        );

        HttpHeaders headers = authenticatedHeaders();

        HttpEntity<TaskCreateRequest> requestEntity =
            new HttpEntity<>(request, headers);

        // ACT
        ResponseEntity<TaskResponse> response =
            restTemplate.postForEntity(
                "/api/tasks/create",
                requestEntity,
                TaskResponse.class
            );

        // ASSERT
        assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        assertTrue(taskRepo.count() == 0);
    }

    @Test
    void shouldReturn401StatusWhenUnauthenticated() {
        // ARRANGE
        TaskCreateRequest request = new TaskCreateRequest(
            "",
            null,
            Priority.HIGH,
            null,
            LocalDate.now().plusDays(1),
            false,
            null,
            null,
            false,
            null,
            List.of()
        );

        // ACT
        ResponseEntity<TaskResponse> response =
            restTemplate.postForEntity(
                "/api/tasks/create",
                request,
                TaskResponse.class
            );

        // ASSERT
        assertTrue(response.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void shouldCreateQuickTaskAndRetrieveItThroughTaskEndpoints() {
        // ARRANGE
        createUser();

        HttpHeaders headers = authenticatedHeaders();

        LocalDate startDate =
            LocalDate.now().plusDays(1);

        QuickTaskCreateRequest request =
            new QuickTaskCreateRequest(
                "Read a book",
                Priority.MEDIUM,
                startDate
            );

        HttpEntity<QuickTaskCreateRequest> requestEntity =
            new HttpEntity<>(request, headers);

        // ACT - CREATE
        ResponseEntity<TaskResponse> createResponse =
            restTemplate.postForEntity(
                "/api/tasks/create-quick",
                requestEntity,
                TaskResponse.class
            );

        // ASSERT - CREATE
        assertTrue(createResponse.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(createResponse.getBody() != null);

        Long idTask =
            createResponse.getBody().idTask();

        // ACT - GET BY ID
        ResponseEntity<TaskResponse> getResponse =
            restTemplate.exchange(
                "/api/tasks/" + idTask,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TaskResponse.class
            );

        // ASSERT - GET BY ID
        assertTrue(getResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(getResponse.getBody().title().equals("Read a book"));
        assertTrue(getResponse.getBody() != null);

        // ACT - FILTER BY SEARCH
        ResponseEntity<List<TaskResponse>> filterResponse =
            restTemplate.exchange(
                "/api/tasks?search=book",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<TaskResponse>>() {}
            );

        // ASSERT - FILTER
        assertTrue(filterResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(filterResponse.getBody() != null);
        assertTrue(filterResponse.getBody().size() == 1);
        assertTrue(filterResponse.getBody().get(0).idTask()== idTask);


        // ACT - GET BY DATE
        ResponseEntity<List<TaskResponse>> dateResponse =
            restTemplate.exchange(
                "/api/tasks/by-date?year=" + startDate.getYear()
                    + "&month=" + startDate.getMonthValue()
                    + "&day=" + startDate.getDayOfMonth(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<TaskResponse>>() {}
            );

        // ASSERT - GET BY DATE

        assertTrue(dateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(dateResponse.getBody() != null);
        assertTrue(dateResponse.getBody().size() == 1);
        assertTrue(dateResponse.getBody().get(0).idTask() == idTask);

        // ACT - GET FUTURE TASKS
        ResponseEntity<List<TaskResponse>> futureResponse =
            restTemplate.exchange(
                "/api/tasks/future",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<TaskResponse>>() {}
            );

        // ASSERT - FUTURE
        assertTrue(futureResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(futureResponse.getBody() != null);
        assertTrue(futureResponse.getBody().stream()
            .allMatch(task -> task.idTask() == idTask)
        );

        // ACT - FINISH TASK
        ResponseEntity<TaskResponse> finishResponse =
            restTemplate.exchange(
                "/api/tasks/task",
                HttpMethod.PATCH,
                new HttpEntity<>(
                    new TaskFinishedRequest(idTask, true),
                    headers
                ),
                TaskResponse.class
            );

        // ASSERT - FINISH
        assertTrue(finishResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(finishResponse.getBody() != null);
        assertTrue(finishResponse.getBody().finished());
        assertTrue(finishResponse.getBody().progress() == 100);
    }

    @Test
    void shouldUpdateTaskCompleteSubtaskAndDeleteTask() {
        // ARRANGE
        createUser();

        HttpHeaders headers = authenticatedHeaders();

        TaskCreateRequest createRequest =
            new TaskCreateRequest(
                "Study",
                null,
                Priority.HIGH,
                null,
                LocalDate.now().plusDays(1),
                false,
                null,
                null,
                false,
                null,
                List.of(
                    "Read chapter",
                    "Take notes"
                )
            );

        // ACT - CREATE
        ResponseEntity<TaskResponse> createResponse =
            restTemplate.postForEntity(
                "/api/tasks/create",
                new HttpEntity<>(createRequest, headers),
                TaskResponse.class
            );

        // ASSERT - CREATE
        assertTrue(createResponse.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(createResponse.getBody() != null);

        Long idTask =
            createResponse.getBody().idTask();

        // ACT - UPDATE
        TaskCreateRequest updateRequest =
            new TaskCreateRequest(
                "Study biology",
                "Prepare for the exam",
                Priority.MEDIUM,
                null,
                LocalDate.now().plusDays(2),
                false,
                null,
                null,
                false,
                null,
                List.of(
                    "Review notes",
                    "Practice questions"
                )
            );

        ResponseEntity<TaskResponse> updateResponse =
            restTemplate.exchange(
                "/api/tasks/" + idTask,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, headers),
                TaskResponse.class
            );

        // ASSERT - UPDATE
        assertTrue(updateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(updateResponse.getBody() != null);
        assertTrue(updateResponse.getBody().title().equals("Study biology"));
        assertTrue(updateResponse.getBody().subtasks().size() == 2);

        // ACT - COMPLETE SUBTASK
        Long idSubtask =
            updateResponse.getBody()
                .subtasks()
                .get(0)
                .idSubtask();

        ResponseEntity<SubtaskResponse> subtaskResponse =
            restTemplate.exchange(
                "/api/tasks/subtask",
                HttpMethod.PATCH,
                new HttpEntity<>(
                    new SubtaskCompletedRequest(idSubtask, true),
                    headers
                ),
                SubtaskResponse.class
            );

        // ASSERT - SUBTASK
        assertTrue(subtaskResponse.getBody() != null);
        assertTrue(subtaskResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(subtaskResponse.getBody().completed());

        // ACT - GET TASK AGAIN
        ResponseEntity<TaskResponse> getResponse =
            restTemplate.exchange(
                "/api/tasks/" + idTask,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TaskResponse.class
            );

        // ASSERT - PROGRESS
        assertTrue(getResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(getResponse.getBody() != null);
        assertTrue(getResponse.getBody().progress() == 50);
        assertFalse(getResponse.getBody().finished());

        // ACT - DELETE
        ResponseEntity<Void> deleteResponse =
            restTemplate.exchange(
                "/api/tasks/" + idTask,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
            );

        // ASSERT - DELETE
        assertTrue(deleteResponse.getStatusCode().equals(HttpStatus.NO_CONTENT));
        assertFalse(taskRepo.existsById(idTask));
    }

    private UserEntity createUser() {
        UserEntity user = new UserEntity();
        user.setEmail("email@example.com");
        user.setUsername("username");
        user.setPassword(
            passwordEncoder.encode("password123")
        );

        return userRepo.save(user);
    }

    private String logIn() {
        MultiValueMap<String, String> loginData =
            new LinkedMultiValueMap<>();

        loginData.add(
            "email",
            "email@example.com"
        );

        loginData.add(
            "password",
            "password123"
        );

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
            MediaType.APPLICATION_FORM_URLENCODED
        );

        HttpEntity<MultiValueMap<String, String>> loginRequest =
            new HttpEntity<>(
                loginData,
                headers
            );

        ResponseEntity<String> loginResponse =
            restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                String.class
            );

        assertTrue(loginResponse.getStatusCode().equals(HttpStatus.OK));

        String setCookie =
            loginResponse.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        assertTrue(setCookie != null);

        return setCookie.split(";", 2)[0];
    }

    private HttpHeaders authenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
            MediaType.APPLICATION_JSON
        );

        headers.add(
            HttpHeaders.COOKIE,
            logIn()
        );

        return headers;
    }
}

