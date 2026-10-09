package com.example.user.controller;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * 
 * <b>AuthController</b>
 * <br>
 * REST controller responsible for user data exchange with the frontend side of application. 
 * Communication is associated with registration, authentication, account deletion, and authorization.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final Environment environment;

    public AuthController(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    /**
     * Receives data of the user that wants to create an account
     * @param request   a request DTO with user data
     * @return          a response with the HTTP status code
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request);
        
            return ResponseEntity.ok("Registration successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    /**
     * Issues a CSRF token cookie for the client.
     * @param csrfToken resolved Spring Security CSRF token
     * @return          an empty success response
     */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        // The token is deferred; reading it forces the cookie to be written
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Sends response about the user authentication status.
     * It returns 200 HTTP code if user is authenticated and 401 HTTP code if he is not authenticated, as well as information if the user is authenticated
     * @return
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(new AuthStatusResponse(true, "User is authenticated"));
        } else {
            return ResponseEntity.status(401).body(new AuthStatusResponse(false, "User is not authenticated"));
        }
    }

    /**
     * Receives a request to delete an account.
     * All required operations related to account removal are performed, including session invalidation.
     * @param request
     * @return  response with a HTTP status code and message informing that the deletion was successful or not
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        try {
            userService.deleteAccount();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            ResponseCookie expiredSessionCookie = ResponseCookie.from(
                environment.getRequiredProperty("server.servlet.session.cookie.name"),
                ""
            )
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

            return ResponseEntity.ok()
                .header("Set-Cookie", expiredSessionCookie.toString())
                .body("Account was successfully deleted");
        } catch (DataAccessException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /**
     * Sends a current user data
     * @return  a response DTO with the current user data
     */
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getUserResponse());
    }

    /**
     * Receives a request with updated username
     * @return a user response DTO with updated username
     */
    @PatchMapping("/user/update-username")
    public UserResponse updateUsername(@RequestBody String username) {
        return userService.updateUsername(username);
    }
    
    
    
    public static class AuthStatusResponse {
        public boolean authenticated;
        public String message;
        
        public AuthStatusResponse(boolean authenticated, String message) {
            this.authenticated = authenticated;
            this.message = message;
        }
        
        public boolean isAuthenticated() {
            return authenticated;
        }
        
        public String getMessage() {
            return message;
        }
    }
    

}
