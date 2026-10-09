package com.example.category.service;

import com.example.category.dto.CategoryRequest;
import com.example.category.dto.CategoryResponse;
import com.example.category.mapper.CategoryMapper;
import com.example.category.model.Category;
import com.example.category.repository.CategoryRepository;
import com.example.common.throwables.exception.EntityAlreadyExistsException;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.event.repository.EventRepository;
import com.example.note.repository.NoteRepository;
import com.example.user.model.UserEntity;
import com.example.task.repository.TaskRepository;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>CategoryService</b>
 * <br>
 * Service class that handles all business logic and operations related to categories, including creating, deleting, retrieving, and helper functions
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final CategoryMapper mapper;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final TaskRepository taskRepo;
    private final EventRepository eventRepo;
    private final NoteRepository noteRepo;

    public CategoryService(
        CategoryRepository categoryRepo,
        CategoryMapper mapper,
        CurrentUserService currentUserService,
        UserRepository userRepo,
        TaskRepository taskRepo,
        EventRepository eventRepo,
        NoteRepository noteRepo
    ) {
        this.categoryRepo = categoryRepo;
        this.mapper = mapper;
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
        this.eventRepo = eventRepo;
        this.noteRepo = noteRepo;
    }

    /**
     * Retrieves all categories created by the user
     * @return a list of category response DTO
     */
    public List<CategoryResponse> getAllCategories() {
        UserEntity user = getCurrentUser();
        List<Category> categories = categoryRepo.findByUser(user);
        return !categories.isEmpty() ? mapper.toResponseList(categories) : new ArrayList<>();
    }

    /**
     * Validates data provided by the user, creates a new category from the data, and saves category to the database
     * @param request   a request DTO containing category data
     * @return          a response DTO with newly created category's data
     */
    public CategoryResponse createCategory(CategoryRequest request) {

        UserEntity user = getCurrentUser();

        validateCreationRequest(request, user);
        
        Category category = new Category();
        category.setName(request.name());
        category.setColor(request.color());
        category.setUser(user);
        return mapper.toResponse(categoryRepo.save(category));
    }

    /**
     * Validates the category with provided ID belongs to the user, updates category, and returns updated category response DTO
     * @param idCategory    an ID of the category to be updated
     * @param request       a request DTO with the data that the category should be updated with
     * @return              a response DTO with updated category data
     */
    public CategoryResponse updateCategory(Long idCategory, CategoryRequest request) {
        Category category = validateCatBelongsToUser(idCategory);
        
        validateCreationRequest(request, getCurrentUser());

        category.setName(request.name());
        category.setColor(request.color());

        return mapper.toResponse(categoryRepo.save(category));
    }

    /**
     * Validates the category with provided ID belongs to the user, deletes the category, and clears its relations with objects of other classes
     * @param idCategory an ID of the category to be deleted
     */
    @Transactional
    public void deleteCategory(Long idCategory) {
        Category category = validateCatBelongsToUser(idCategory);

        UserEntity user = getCurrentUser();

        taskRepo.clearCategoryByCategoryAndUser(idCategory, user);
        eventRepo.clearCategoryByCategoryAndUser(idCategory, user);
        noteRepo.clearCategoryByCategoryAndUser(idCategory, user);

        categoryRepo.delete(category);
    }

    /**
     * Deletes all user categories. Used when deleting account
     * @param user user object
     */
    public void deleteCategoriesByUser(UserEntity user) {
        categoryRepo.deleteByUser(user);
    }

    /**
     * Validates the category belongs to a specific user
     * @param idCategory    an ID of a category to be checked
     * @return              an object of the verified category
     */
    public Category validateCatBelongsToUser(Long idCategory) {
        Long idUser = currentUserService.getCurrentUserId();
        return categoryRepo.findByIdAndIdUser(idCategory, idUser)
            .orElseThrow(() -> new NotFoundException("Category", idCategory));
    }

    /**
     * Checks if a category already exists
     * @param idCategory    an ID of the category to be checked
     * @return              true if category exists, false if it does not exist
     */
    public boolean categoryExistsById(Long idCategory) {
        return categoryRepo.existsById(idCategory);
    }

    /**
     * Validates a category against a logical and business rules
     * @param request   a request DTO to be validated
     */
    public void validateCreationRequest(CategoryRequest request, UserEntity user) {
        if (request == null){
            throw new InvalidRequestException("NO_CATEGORY_DATA", "Category data is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRequestException("CATEGORY_NAME_REQUIRED","Category name is required");
        }
        if (request.color() == null || request.color().isBlank()) {
            throw new InvalidRequestException("CATEGORY_COLOR_REQUIRED","Category color is required");
        }
        if (categoryRepo.existsByName(request.name(), user)) {
            throw new EntityAlreadyExistsException("Category", "name");
        }
    }

    /**
     * Retrieves a current user object
     * @return  an object with the current user's data
     */
    private UserEntity getCurrentUser() {
        Long idUser = currentUserService.getCurrentUserId();
        return userRepo.findById(idUser)
            .orElseThrow(() -> new NotFoundException("User", idUser));
    }
}
