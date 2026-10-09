package com.example.reminder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

import com.example.reminder.dto.ReminderCreationRequest;
import com.example.reminder.dto.ReminderResponse;
import com.example.reminder.dto.ReminderStatusChangeRequest;
import com.example.reminder.enums.ReminderStatus;
import com.example.reminder.enums.ReminderType;
import com.example.reminder.enums.Timing;
import com.example.reminder.model.Reminder;
import com.example.reminder.repository.ReminderRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ReminderApiIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ReminderRepository reminderRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        reminderRepo.deleteAll();
        userRepo.deleteAll();
        restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }

    @Test
    void shouldCreateListRetrieveUpdateAndDeleteReminder() {
        UserEntity user = createUser();
        HttpHeaders headers = createHeaders();
        LocalDate remindDate = LocalDate.now().plusDays(2);
        LocalTime remindTime = LocalTime.of(10, 0);
        ReminderCreationRequest request = new ReminderCreationRequest(
            "Send project update",
            ReminderType.NONE,
            null,
            null,
            false,
            Timing.SET_MANUALLY,
            remindDate,
            remindTime
        );

        ResponseEntity<List<ReminderResponse>> createResponse = restTemplate.exchange(
            "/api/reminders/create",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<List<ReminderResponse>>() {}
        );

        assertTrue(createResponse.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(createResponse.getBody() != null);
        assertTrue(createResponse.getBody().size() == 1);

        ReminderResponse createdReminder = createResponse.getBody().get(0);
        assertTrue(createdReminder.content().equals("Send project update"));
        assertTrue(createdReminder.remindDate().equals(remindDate));
        assertTrue(createdReminder.remindTime().equals(remindTime));
        assertTrue(createdReminder.status().equals(ReminderStatus.PENDING));

        Long idReminder = createdReminder.idReminder();
        Reminder saved = reminderRepo.findById(idReminder).orElseThrow();
        assertTrue(saved.getContent().equals("Send project update"));
        assertTrue(saved.getUser().getIdUser().equals(user.getIdUser()));
        assertTrue(saved.getStatus().equals(ReminderStatus.PENDING));

        ResponseEntity<List<ReminderResponse>> listResponse = restTemplate.exchange(
            "/api/reminders",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<ReminderResponse>>() {}
        );
        assertTrue(listResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(listResponse.getBody() != null);
        assertTrue(listResponse.getBody().size() == 1);
        assertTrue(listResponse.getBody().get(0).idReminder().equals(idReminder));

        ResponseEntity<ReminderResponse> getResponse = restTemplate.exchange(
            "/api/reminders/" + idReminder,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ReminderResponse.class
        );
        assertTrue(getResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(getResponse.getBody() != null);
        assertTrue(getResponse.getBody().content().equals("Send project update"));

        ResponseEntity<List<ReminderResponse>> dateResponse = restTemplate.exchange(
            "/api/reminders/by-date?year=" + remindDate.getYear()
                + "&month=" + remindDate.getMonthValue()
                + "&day=" + remindDate.getDayOfMonth(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<ReminderResponse>>() {}
        );
        assertTrue(dateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(dateResponse.getBody() != null);
        assertTrue(dateResponse.getBody().size() == 1);
        assertTrue(dateResponse.getBody().get(0).idReminder().equals(idReminder));

        ResponseEntity<ReminderResponse> statusResponse = restTemplate.exchange(
            "/api/reminders/" + idReminder + "/status",
            HttpMethod.PATCH,
            new HttpEntity<>(new ReminderStatusChangeRequest(idReminder, ReminderStatus.SENT), headers),
            ReminderResponse.class
        );
        assertTrue(statusResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(statusResponse.getBody() != null);
        assertTrue(statusResponse.getBody().status().equals(ReminderStatus.SENT));

        ResponseEntity<List<ReminderResponse>> sentResponse = restTemplate.exchange(
            "/api/reminders/sent",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<ReminderResponse>>() {}
        );
        assertTrue(sentResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(sentResponse.getBody() != null);
        assertTrue(sentResponse.getBody().size() == 1);
        assertTrue(sentResponse.getBody().get(0).idReminder().equals(idReminder));

        LocalDate updatedDate = remindDate.plusDays(1);
        ReminderCreationRequest updateRequest = new ReminderCreationRequest(
            "Send final project update",
            ReminderType.NONE,
            null,
            null,
            false,
            Timing.SET_MANUALLY,
            updatedDate,
            LocalTime.of(11, 30)
        );
        ResponseEntity<ReminderResponse> updateResponse = restTemplate.exchange(
            "/api/reminders/" + idReminder,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            ReminderResponse.class
        );
        assertTrue(updateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(updateResponse.getBody() != null);
        assertTrue(updateResponse.getBody().content().equals("Send final project update"));
        assertTrue(updateResponse.getBody().remindDate().equals(updatedDate));
        assertTrue(updateResponse.getBody().remindTime().equals(LocalTime.of(11, 30)));
        assertTrue(updateResponse.getBody().status().equals(ReminderStatus.PENDING));

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/reminders/" + idReminder,
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );
        assertTrue(deleteResponse.getStatusCode().equals(HttpStatus.NO_CONTENT));
        assertFalse(reminderRepo.existsById(idReminder));
    }

    @Test
    void shouldRejectInvalidAndUnauthenticatedReminderRequests() {
        createUser();
        HttpHeaders headers = createHeaders();

        ReminderCreationRequest invalidRequest = new ReminderCreationRequest(
            "",
            ReminderType.NONE,
            null,
            null,
            false,
            Timing.SET_MANUALLY,
            LocalDate.now().plusDays(1),
            LocalTime.of(10, 0)
        );
        ResponseEntity<List<ReminderResponse>> invalidResponse = restTemplate.exchange(
            "/api/reminders/create",
            HttpMethod.POST,
            new HttpEntity<>(invalidRequest, headers),
            new ParameterizedTypeReference<List<ReminderResponse>>() {}
        );
        assertTrue(invalidResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        ReminderCreationRequest request = new ReminderCreationRequest(
            "Call the client",
            ReminderType.NONE,
            null,
            null,
            false,
            Timing.SET_MANUALLY,
            LocalDate.now().plusDays(1),
            LocalTime.of(10, 0)
        );
        ResponseEntity<List<ReminderResponse>> unauthenticatedResponse = restTemplate.exchange(
            "/api/reminders/create",
            HttpMethod.POST,
            new HttpEntity<>(request, createJsonHeaders()),
            new ParameterizedTypeReference<List<ReminderResponse>>() {}
        );
        assertTrue(unauthenticatedResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = createJsonHeaders();
        headers.add(HttpHeaders.COOKIE, logIn());
        return headers;
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private UserEntity createUser() {
        UserEntity user = new UserEntity();
        user.setEmail("email@example.com");
        user.setUsername("username");
        user.setPassword(passwordEncoder.encode("password123"));

        return userRepo.save(user);
    }

    private String logIn() {
        MultiValueMap<String, String> loginData = new LinkedMultiValueMap<>();
        loginData.add("email", "email@example.com");
        loginData.add("password", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> loginRequest = new HttpEntity<>(loginData, headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);
        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);

        assertTrue(cookies != null && !cookies.isEmpty());
        assertTrue(loginResponse.getStatusCode().equals(HttpStatus.OK));

        return cookies.get(0);
    }
}
