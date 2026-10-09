package com.example.category.dto;

/**
 * 
 * <b>CategoryResponse</b><br>
 * A DTO record used to send category data to frontend.
 * 
 * @param idCategory category ID from the database
 * @param name category name
 * @param color color assigned to the category
 */
public record CategoryResponse(
    Long idCategory,
    String name,
    String color
) {

}
