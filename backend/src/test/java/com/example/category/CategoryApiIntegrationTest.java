package com.example.category;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.example.category.dto.CategoryRequest;
import com.example.category.dto.CategoryResponse;
import com.example.category.model.Category;
import com.example.category.repository.CategoryRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CategoryApiIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    CategoryRepository categoryRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        categoryRepo.deleteAll();
        userRepo.deleteAll();
        restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    }

    @Test
    void shouldCreateListUpdateAndDeleteCategory() {
        UserEntity user = createUser();
        HttpHeaders headers = createHeaders();
        CategoryRequest request = new CategoryRequest("Work", "#336699");

        ResponseEntity<CategoryResponse> createResponse = restTemplate.postForEntity(
            "/api/categories/create",
            new HttpEntity<>(request, headers),
            CategoryResponse.class
        );

        assertTrue(createResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(createResponse.getBody() != null);

        assertTrue(createResponse.getBody().name().equals("Work"));
        assertTrue(createResponse.getBody().color().equals("#336699"));

        Long idCategory = createResponse.getBody().idCategory();
        Category saved = categoryRepo.findById(idCategory).orElseThrow();
        assertTrue(saved.getName().equals("Work"));
        assertTrue(saved.getColor().equals("#336699"));
        assertTrue(saved.getUser().getIdUser().equals(user.getIdUser()));

        ResponseEntity<List<CategoryResponse>> listResponse = restTemplate.exchange(
            "/api/categories",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<CategoryResponse>>() {}
        );
        assertTrue(listResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(listResponse.getBody() != null);
        assertTrue(listResponse.getBody().size() == 1);
        assertTrue(listResponse.getBody().get(0).idCategory().equals(idCategory));

        CategoryRequest updateRequest = new CategoryRequest("Personal", "#993366");
        ResponseEntity<CategoryResponse> updateResponse = restTemplate.exchange(
            "/api/categories/" + idCategory,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            CategoryResponse.class
        );
        assertTrue(updateResponse.getStatusCode().equals(HttpStatus.OK));
        assertTrue(updateResponse.getBody() != null);
        assertTrue(updateResponse.getBody().name().equals("Personal"));
        assertTrue(updateResponse.getBody().color().equals("#993366"));

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/categories/" + idCategory,
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );
        assertTrue(deleteResponse.getStatusCode().equals(HttpStatus.OK));
        assertFalse(categoryRepo.existsById(idCategory));
    }

    @Test
    void shouldRejectInvalidAndUnauthenticatedCategoryRequests() {
        createUser();
        HttpHeaders headers = createHeaders();

        CategoryRequest invalidRequest = new CategoryRequest("", "#336699");
        ResponseEntity<CategoryResponse> invalidResponse = restTemplate.postForEntity(
            "/api/categories/create",
            new HttpEntity<>(invalidRequest, headers),
            CategoryResponse.class
        );
        assertTrue(invalidResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));

        CategoryRequest request = new CategoryRequest("Work", "#336699");
        ResponseEntity<CategoryResponse> unauthenticatedResponse = restTemplate.postForEntity(
            "/api/categories/create",
            request,
            CategoryResponse.class
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
