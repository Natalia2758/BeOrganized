package com.example.event;

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

import com.example.event.dto.EventCreateRequest;
import com.example.event.dto.EventResponse;
import com.example.event.dto.QuickEventCreateRequest;
import com.example.event.model.Event;
import com.example.event.repository.EventRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class EventApiIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EventRepository eventRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        eventRepo.deleteAll();
        userRepo.deleteAll();
        restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }

    @Test
    void shouldCreateEventAndRetrieveItThroughEventEndpoints() {
        UserEntity user = createUser();
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = startDate.plusDays(1);
        EventCreateRequest request = new EventCreateRequest(
            "Project conference",
            "Annual project meeting",
            null,
            startDate,
            endDate,
            true,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            false,
            null
        );

        HttpHeaders headers = createHeaders();
        ResponseEntity<EventResponse> createResponse = restTemplate.postForEntity(
            "/api/events/create",
            new HttpEntity<>(request, headers),
            EventResponse.class
        );

        assertTrue(createResponse.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(createResponse.getBody() != null);

        assertTrue(createResponse.getBody().title().equals("Project conference"));

        Long idEvent = createResponse.getBody().idEvent();
        Event saved = eventRepo.findById(idEvent).orElseThrow();
        assertTrue(saved.getTitle().equals("Project conference"));
        assertTrue(saved.getUser().getIdUser().equals(user.getIdUser()));
        assertTrue(saved.getDateStart().equals(startDate));
        assertTrue(saved.getDateEnd().equals(endDate));

        ResponseEntity<EventResponse> getResponse = restTemplate.exchange(
            "/api/events/" + idEvent,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            EventResponse.class
        );
        assertTrue(getResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(getResponse.getBody() != null);
        assertTrue(getResponse.getBody().details().equals("Annual project meeting"));

        ResponseEntity<List<EventResponse>> filterResponse = restTemplate.exchange(
            "/api/events?search=conference",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );
        assertTrue(filterResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(filterResponse.getBody() != null);
        assertTrue(filterResponse.getBody().size() == 1);

        LocalDate eventDate = startDate.plusDays(1);
        ResponseEntity<List<EventResponse>> dateResponse = restTemplate.exchange(
            "/api/events/by-date?year=" + eventDate.getYear()
                + "&month=" + eventDate.getMonthValue()
                + "&day=" + eventDate.getDayOfMonth(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );
        assertTrue(dateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(dateResponse.getBody() != null);
        assertTrue(dateResponse.getBody().stream()
            .anyMatch(event -> event.idEvent().equals(idEvent))
        );

        ResponseEntity<List<EventResponse>> futureResponse = restTemplate.exchange(
            "/api/events/future",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );
        assertTrue(futureResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(futureResponse.getBody() != null);
        assertTrue(futureResponse.getBody().stream()
            .anyMatch(event -> event.idEvent().equals(idEvent))
        );

        EventCreateRequest updateRequest = new EventCreateRequest(
            "Updated conference",
            "Updated details",
            null,
            startDate.plusDays(1),
            endDate.plusDays(1),
            false,
            null,
            null,
            false,
            null
        );
        ResponseEntity<EventResponse> updateResponse = restTemplate.exchange(
            "/api/events/" + idEvent,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            EventResponse.class
        );
        assertTrue(updateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(updateResponse.getBody() != null);
        assertTrue(updateResponse.getBody().title().equals("Updated conference"));
        assertTrue(updateResponse.getBody().details().equals("Updated details"));

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/events/" + idEvent,
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );
        assertTrue(deleteResponse.getStatusCode().equals(HttpStatus.OK));
        assertFalse(eventRepo.existsById(idEvent));
    }

    @Test
    void shouldCreateQuickEventAndRejectInvalidOrUnauthenticatedRequests() {
        createUser();

        LocalDate startDate = LocalDate.now().plusDays(1);
        QuickEventCreateRequest request = new QuickEventCreateRequest(
            "Quick appointment",
            startDate,
            startDate
        );
        HttpHeaders headers = createHeaders();

        ResponseEntity<EventResponse> createResponse = restTemplate.postForEntity(
            "/api/events/create-quick",
            new HttpEntity<>(request, headers),
            EventResponse.class
        );

        assertTrue(createResponse.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(createResponse.getBody() != null);
        assertTrue(createResponse.getBody().title().equals("Quick appointment"));

        EventCreateRequest blankTitleRequest = new EventCreateRequest(
            "",
            null,
            null,
            startDate,
            startDate,
            false,
            null,
            null,
            false,
            null
        );
        ResponseEntity<EventResponse> invalidResponse = restTemplate.postForEntity(
            "/api/events/create",
            new HttpEntity<>(blankTitleRequest, headers),
            EventResponse.class
        );
        assertTrue(invalidResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        ResponseEntity<EventResponse> unauthenticatedResponse = restTemplate.postForEntity(
            "/api/events/create-quick",
            request,
            EventResponse.class
        );
        assertTrue(unauthenticatedResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, logIn());
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
