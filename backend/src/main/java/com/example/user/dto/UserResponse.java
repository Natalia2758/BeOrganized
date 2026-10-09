package com.example.user.dto;

/**
 * 
 * <b>UserResponse</b><br>
 * 
 * A DTO record used to send user data to the frontend.
 * 
 * @param idUser    database ID of the user
 * @param email     email address of the user
 * @param username  user password
 */
public record UserResponse(
    Long idUser,
    String email,
    String username
) {

}
