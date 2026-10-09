package com.example.note;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
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

import com.example.note.dto.NoteCreateRequest;
import com.example.note.dto.NoteResponse;
import com.example.note.dto.QuickNoteCreateRequest;
import com.example.note.model.Note;
import com.example.note.repository.NoteRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class NoteApiIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    NoteRepository noteRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        noteRepo.deleteAll();
        userRepo.deleteAll();
        restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }

    @Test
    void shouldCreateListRetrieveUpdateAndDeleteNote() {
        UserEntity user = createUser();
        HttpHeaders headers = createHeaders();
        LocalDate date = LocalDate.now().plusDays(1);
        NoteCreateRequest request = new NoteCreateRequest(
            "Meeting notes",
            "Discuss project milestones",
            date,
            null
        );

        ResponseEntity<NoteResponse> createResponse = restTemplate.postForEntity(
            "/api/notes/create",
            new HttpEntity<>(request, headers),
            NoteResponse.class
        );

        assertTrue(createResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(createResponse.getBody() != null);

        assertTrue(createResponse.getBody().title().equals("Meeting notes"));
        assertTrue(createResponse.getBody().content().equals("Discuss project milestones"));

        Long idNote = createResponse.getBody().idNote();
        Note saved = noteRepo.findById(idNote).orElseThrow();
        assertTrue(saved.getTitle().equals("Meeting notes"));
        assertTrue(saved.getContent().equals("Discuss project milestones"));
        assertTrue(saved.getDate().equals(date));
        assertTrue(saved.getUser().getIdUser().equals(user.getIdUser()));

        ResponseEntity<List<NoteResponse>> listResponse = restTemplate.exchange(
            "/api/notes",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<NoteResponse>>() {}
        );
        assertTrue(listResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(listResponse.getBody() != null);
        assertTrue(listResponse.getBody().size() == 1);
        assertTrue(listResponse.getBody().get(0).idNote().equals(idNote));

        ResponseEntity<NoteResponse> getResponse = restTemplate.exchange(
            "/api/notes/" + idNote,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            NoteResponse.class
        );
        assertTrue(getResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(getResponse.getBody() != null);
        assertTrue(getResponse.getBody().content().equals("Discuss project milestones"));

        ResponseEntity<List<NoteResponse>> calendarResponse = restTemplate.exchange(
            "/api/notes/calendar?year=" + date.getYear()
                + "&month=" + date.getMonthValue()
                + "&day=" + date.getDayOfMonth(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<NoteResponse>>() {}
        );
        assertTrue(calendarResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(calendarResponse.getBody() != null);
        assertTrue(calendarResponse.getBody().size() == 1);
        assertTrue(calendarResponse.getBody().get(0).idNote().equals(idNote));

        NoteCreateRequest updateRequest = new NoteCreateRequest(
            "Updated meeting notes",
            "Review project milestones and next steps",
            date.plusDays(1),
            null
        );
        ResponseEntity<NoteResponse> updateResponse = restTemplate.exchange(
            "/api/notes/" + idNote,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            NoteResponse.class
        );
        assertTrue(updateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(updateResponse.getBody() != null);
        assertTrue(updateResponse.getBody().title().equals("Updated meeting notes"));
        assertTrue(updateResponse.getBody().content().equals("Review project milestones and next steps"));

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/notes/" + idNote,
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );
        assertTrue(deleteResponse.getStatusCode().equals(HttpStatus.NO_CONTENT));
        assertFalse(noteRepo.existsById(idNote));
    }

    @Test
    void shouldCreateQuickNoteAndRejectInvalidOrUnauthenticatedRequests() {
        createUser();
        HttpHeaders headers = createHeaders();

        QuickNoteCreateRequest request = new QuickNoteCreateRequest(
            "Quick note",
            "Remember to send the project update"
        );
        ResponseEntity<NoteResponse> createResponse = restTemplate.postForEntity(
            "/api/notes/create-quick",
            new HttpEntity<>(request, headers),
            NoteResponse.class
        );

        assertTrue(createResponse.getStatusCode().equals(HttpStatus.CREATED));
        assertTrue(createResponse.getBody() != null);
        assertTrue(createResponse.getBody().title().equals("Quick note"));
        assertTrue(createResponse.getBody().content().equals("Remember to send the project update"));

        NoteCreateRequest invalidRequest = new NoteCreateRequest(
            "Invalid note",
            "",
            LocalDate.now().plusDays(1),
            null
        );
        ResponseEntity<NoteResponse> invalidResponse = restTemplate.postForEntity(
            "/api/notes/create",
            new HttpEntity<>(invalidRequest, headers),
            NoteResponse.class
        );
        assertTrue(invalidResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        ResponseEntity<NoteResponse> unauthenticatedResponse = restTemplate.postForEntity(
            "/api/notes/create-quick",
            request,
            NoteResponse.class
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
