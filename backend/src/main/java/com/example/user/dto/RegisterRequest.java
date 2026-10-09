package com.example.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

/**
 * 
 * <b>RegisterRequest</b><br>
 * 
 * A DTO record used to transfer user data during registration. These data will be used for authentication purposes.
 * 
 * @param email     email address entered by the user
 * @param username  username of the user
 * @param password  user's password
 */
public record RegisterRequest(
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be in an appropriate format")
    String email,
    @NotBlank(message = "Username must not be blank")
    @Size(max = 50, message = "Username is too long")
    String username,
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {

}
