package com.example.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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

import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserResponse;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthApiIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }

    @Test
    void shouldRegisterLoginRetrieveAndUpdateCurrentUserThenLogout() {
        RegisterRequest request = new RegisterRequest(
            "email@example.com",
            "username",
            "password123"
        );

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            String.class
        );

        assertTrue(registerResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(registerResponse.getBody().equals("Registration successful!"));

        UserEntity saved = userRepo.findByEmail("email@example.com").orElseThrow();
        assertTrue(saved.getUsername().equals("username"));
        assertTrue(passwordEncoder.matches("password123", saved.getPassword()));

        String sessionCookie = logIn("email@example.com", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);

        ResponseEntity<AuthStatusResponse> statusResponse = restTemplate.exchange(
            "/api/auth/status",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            AuthStatusResponse.class
        );
        assertTrue(statusResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(statusResponse.getBody() != null);
        assertTrue(statusResponse.getBody().authenticated());

        ResponseEntity<UserResponse> userResponse = restTemplate.exchange(
            "/api/auth/user",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            UserResponse.class
        );
        assertTrue(userResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(userResponse.getBody() != null);
        assertTrue(userResponse.getBody().idUser().equals(saved.getIdUser()));
        assertTrue(userResponse.getBody().email().equals("email@example.com"));
        assertTrue(userResponse.getBody().username().equals("username"));

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.add(HttpHeaders.COOKIE, sessionCookie);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<UserResponse> updateResponse = restTemplate.exchange(
            "/api/auth/user/update-username",
            HttpMethod.PATCH,
            new HttpEntity<>("updated-user", updateHeaders),
            UserResponse.class
        );
        assertTrue(updateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(updateResponse.getBody() != null);
        assertTrue(updateResponse.getBody().username().equals("updated-user"));

        ResponseEntity<String> logoutResponse = restTemplate.exchange(
            "/api/auth/logout",
            HttpMethod.POST,
            new HttpEntity<>(headers),
            String.class
        );
        assertTrue(logoutResponse.getStatusCode().equals(HttpStatus.OK));

        ResponseEntity<AuthStatusResponse> loggedOutStatusResponse = restTemplate.exchange(
            "/api/auth/status",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            AuthStatusResponse.class
        );
        assertTrue(loggedOutStatusResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        assertTrue(loggedOutStatusResponse.getBody() != null);
        assertFalse(loggedOutStatusResponse.getBody().authenticated());
    }

    @Test
    void shouldRejectInvalidAndDuplicateRegistrationAndInvalidLogin() {
        RegisterRequest invalidRequest = new RegisterRequest(
            "not-an-email",
            "",
            "short"
        );
        ResponseEntity<String> invalidResponse = restTemplate.postForEntity(
            "/api/auth/register",
            invalidRequest,
            String.class
        );
        assertTrue(invalidResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        assertTrue(userRepo.count() == 0);

        RegisterRequest request = new RegisterRequest(
            "email@example.com",
            "username",
            "password123"
        );
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            String.class
        );
        assertTrue(registerResponse.getStatusCode().equals(HttpStatus.OK));

        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            String.class
        );
        assertTrue(duplicateResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        assertTrue(userRepo.count() == 1);

        ResponseEntity<String> invalidLoginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            createLoginRequest("email@example.com", "wrong-password"),
            String.class
        );
        assertTrue(invalidLoginResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void shouldDeleteAccountAndInvalidateItsSession() {
        registerUser();
        String sessionCookie = logIn("email@example.com", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);

        UserEntity user = userRepo.findByEmail("email@example.com").orElseThrow();
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
            "/api/auth/delete-account",
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            String.class
        );

        assertTrue(deleteResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(deleteResponse.getBody().equals("Account was successfully deleted"));
        assertFalse(userRepo.existsById(user.getIdUser()));
        assertTrue(deleteResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=0"));

        ResponseEntity<UserResponse> userResponse = restTemplate.exchange(
            "/api/auth/user",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            UserResponse.class
        );
        assertTrue(userResponse.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
    }

    private void registerUser() {
        RegisterRequest request = new RegisterRequest(
            "email@example.com",
            "username",
            "password123"
        );
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            String.class
        );
        assertTrue(response.getStatusCode().equals(HttpStatus.OK));
    }

    private String logIn(String email, String password) {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            createLoginRequest(email, password),
            String.class
        );
        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);

        assertTrue(cookies != null && !cookies.isEmpty());
        assertTrue(loginResponse.getStatusCode().equals(HttpStatus.OK));

        return cookies.get(0).split(";", 2)[0];
    }

    private HttpEntity<MultiValueMap<String, String>> createLoginRequest(String email, String password) {
        MultiValueMap<String, String> loginData = new LinkedMultiValueMap<>();
        loginData.add("email", email);
        loginData.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return new HttpEntity<>(loginData, headers);
    }

    public record AuthStatusResponse(boolean authenticated, String message) {
    }
}
