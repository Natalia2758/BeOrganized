package com.example.category.controller;

import com.example.category.dto.CategoryRequest;
import com.example.category.dto.CategoryResponse;
import com.example.category.service.CategoryService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * 
 * <b>CategoryController</b>
 * <br>
 * REST controller responsible for communication and category data exchange with the frontend side of application
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Sends response with all user categories to the frontend
     * @return a list of category response DTOs
     */
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    /**
     * Receives a request DTO with the data of a category to be created
     * @param request   a request DTO with category data provided by the user
     * @return          a response DTO with a created category
     */
    @PostMapping("/create")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }
    
    /**
     * Receives a request DTO with modified data of an existing category
     * @param idCategory    an ID of the category to be modified
     * @param request       a request DTO with the data to be changed
     * @return              a response DTO with modified category data
     */
    @PutMapping("/{idCategory}")
    public CategoryResponse updateCategory(@PathVariable(required = true) Long idCategory, @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(idCategory, request);
    }

    /**
     * Receives a request to delete a category
     * @param idCategory    an ID of the category to be deleted
     */
    @DeleteMapping("/{idCategory}")
    public void deleteCategory(@PathVariable(required = true) Long idCategory) {
        categoryService.deleteCategory(idCategory);
    }
    
}
