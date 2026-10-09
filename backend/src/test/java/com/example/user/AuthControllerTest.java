package com.example.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.env.Environment;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.containsString;

import com.example.user.controller.AuthController;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserResponse;
import com.example.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private Environment environment;

    @Test
    void registerReturnsOkWhenRegistrationIsSuccessful() throws Exception {

        String requestJson = """
                {
                    "username": "username",
                    "email": "email@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful!"));

        verify(userService).registerUser(any(RegisterRequest.class));
    }


    @Test
    void registerReturnsBadRequestWhenRegistrationFails() throws Exception {

        doThrow(new IllegalArgumentException("Username already exists"))
                .when(userService)
                .registerUser(any(RegisterRequest.class));

        String requestJson = """
                {
                    "username": "username",
                    "email": "email@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));

        verify(userService).registerUser(any(RegisterRequest.class));
    }


    @Test
    void registerReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        String invalidRequestJson = """
                {
                    "username": "",
                    "email": "not-an-email",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(RegisterRequest.class));
    }

    @Test
    void logoutInvalidatesSessionAndReturnsOk() throws Exception {

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful!"));

        assertTrue(session.isInvalid());
    }

    @Test
    void checkAuthStatusReturnsAuthenticatedWhenUserIsAuthenticated() throws Exception {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("username", null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            mockMvc.perform(get("/api/auth/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(true))
                    .andExpect(jsonPath("$.message")
                            .value("User is authenticated"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }


    @Test
    void checkAuthStatusReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {

        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.message")
                        .value("User is not authenticated"));
    }


    @Test
    void checkAuthStatusReturnsUnauthorizedForAnonymousUser() throws Exception {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("anonymousUser", null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            mockMvc.perform(get("/api/auth/status"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.authenticated").value(false))
                    .andExpect(jsonPath("$.message")
                            .value("User is not authenticated"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void deleteAccountReturnsOkWhenDeletionIsSuccessful() throws Exception {

        String sessionCookieName = environment.getProperty("server.servlet.session.cookie.name");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(delete("/api/auth/delete-account")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie",
                        containsString(sessionCookieName + "=")))
                .andExpect(content().string("Account was successfully deleted"));

        verify(userService).deleteAccount();

        assertTrue(session.isInvalid());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deleteAccountReturnsInternalServerErrorWhenDatabaseOperationFails() throws Exception {

        doThrow(new DataAccessException("Database error") {})
                .when(userService)
                .deleteAccount();

        mockMvc.perform(delete("/api/auth/delete-account")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Database error"));

        verify(userService).deleteAccount();
    }


    @Test
    void getCurrentUserReturnsUser() throws Exception {

        UserResponse userResponse = new UserResponse(1L, "username", "email@example.com");

        when(userService.getUserResponse()).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email")
                        .value("email@example.com"));

        verify(userService).getUserResponse();
    }

    @Test
    void updateUsernameReturnsUpdatedUser() throws Exception {

        UserResponse userResponse = new UserResponse(1L, "newUsername", "email@example.com");

        when(userService.updateUsername("newUsername")).thenReturn(userResponse);

        mockMvc.perform(patch("/api/auth/user/update-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content("\"newUsername\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("newUsername"));

        verify(userService).updateUsername("newUsername");
    }
}
