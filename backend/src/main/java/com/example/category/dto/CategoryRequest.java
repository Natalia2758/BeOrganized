package com.example.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 
 * <b>CategoryRequest</b><br>
 * 
 * A DTO record that transfers category data provided by the user during creation and update.
 * @param name name of the category
 * @param color color assigned to the category
 */
public record CategoryRequest(
    @NotBlank @Size(max = 200, message = "Name must not exceed 200 characters") String name,
    String color
) {

}
