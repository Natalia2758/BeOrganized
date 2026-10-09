package com.example.user.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.user.model.UserLoggedIn;

/**
 * 
 * <b>CurrentUserService</b><br>
 * 
 * Service class used to get data of the logged-in user from the security context.
 */
@Service
public class CurrentUserService {

    /**
     * Returns ID of the logged-in user from the security context 
     * @return current user ID
     */
    public Long getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {

            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserLoggedIn user)) {
            throw new IllegalStateException("Unexpected authentication principal");
        }

        return user.getIdUser();
    }

    /**
     * Returns email of the logged-in user from the security context
     * @return current user email
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");

        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserLoggedIn user)) {
            throw new IllegalStateException("Unexpected authentication principal");
        }

        return user.getEmail();

    }

}
