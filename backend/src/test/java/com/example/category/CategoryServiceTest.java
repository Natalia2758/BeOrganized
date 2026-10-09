package com.example.category;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.category.dto.CategoryRequest;
import com.example.category.dto.CategoryResponse;
import com.example.category.mapper.CategoryMapper;
import com.example.category.model.Category;
import com.example.category.repository.CategoryRepository;
import com.example.category.service.CategoryService;
import com.example.common.throwables.exception.EntityAlreadyExistsException;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.event.repository.EventRepository;
import com.example.note.repository.NoteRepository;
import com.example.task.repository.TaskRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock 
    private CategoryRepository categoryRepo;
    @Mock 
    private CategoryMapper mapper;
    @Mock 
    private CurrentUserService currentUserService;
    @Mock 
    private UserRepository userRepo;
    @Mock 
    private TaskRepository taskRepo;
    @Mock 
    private EventRepository eventRepo;
    @Mock 
    private NoteRepository noteRepo;

    @InjectMocks 
    private CategoryService categoryService;

    @Test
    void getAllCategoriesReturnsCategoryListIfExist(){
        
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);

        CategoryResponse response1 = mock(CategoryResponse.class);
        CategoryResponse response2 = mock(CategoryResponse.class);
        List<CategoryResponse> catResponseList = List.of(response1, response2);

        Category cat1 = new Category();
        cat1.setUser(user);
        Category cat2 = new Category();
        cat2.setUser(user);
        List<Category> catListByUser = List.of(cat1, cat2);

        when(categoryRepo.findByUser(user)).thenReturn(catListByUser);
        when(mapper.toResponseList(catListByUser)).thenReturn(catResponseList);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(catResponseList, result);
        verify(categoryRepo).findByUser(user);
        verify(mapper).toResponseList(catListByUser);
    }

    @Test
    void getAllCategoriesReturnsEmptyListIfNotExist(){
        UserEntity user = authenticatedUser(1L);
        List<Category> catListByUser = new ArrayList<>();

        when(categoryRepo.findByUser(user)).thenReturn(catListByUser);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertTrue(result.isEmpty());
        verify(categoryRepo).findByUser(user);
        verify(mapper, never()).toResponseList(catListByUser);

    }

    @Test 
    void createCategoryReturnsCategoryResponseWhenSuccessfullyCreated(){

        UserEntity user = authenticatedUser(1L);

        String name = "name";
        String color = "#ff00ff";

        CategoryRequest request = new CategoryRequest(name, color);

        CategoryResponse response = new CategoryResponse(1L, name, color);

        when(categoryRepo.existsByName(name, user)).thenReturn(false);
        when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        ArgumentCaptor<Category> catCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepo).save(catCaptor.capture());
        verify(mapper).toResponse(any(Category.class));

        Category saved = catCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.color(), saved.getColor());
        assertEquals(user, saved.getUser());
        assertEquals(response, result);
    }

    @Test 
    void createCategoryFailsWhenEmptyCategoryRequest(){
        CategoryRequest request = null;

        assertThrows(InvalidRequestException.class, () -> categoryService.createCategory(request));
    }

    @Test 
    void createCategoryFailsWhenBlankCategoryName(){
        CategoryRequest request = new CategoryRequest("", "#ffffff");

        assertThrows(InvalidRequestException.class, () -> categoryService.createCategory(request));
    }

    @Test 
    void createCategoryFailsWhenColorIsNull(){
         CategoryRequest request = new CategoryRequest("Name", null);

        assertThrows(InvalidRequestException.class, () -> categoryService.createCategory(request));
    }

    @Test 
    void createCategoryFailsWhenCategoryNameExists(){
        String name = "name";
        String color = "#ff00ff";

        CategoryRequest request = new CategoryRequest(name, color);

        when(categoryRepo.existsByName(name, any(UserEntity.class))).thenReturn(true);
        assertThrows(EntityAlreadyExistsException.class, () -> categoryService.createCategory(request));
    }

    @Test 
    void createCategoryFailsWhenUserDoesNotExist(){
        Long idUser = 1L;

        CategoryRequest request = new CategoryRequest("name", "#ff00ff");

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.createCategory(request));
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void updateCategoryReturnsCategoryResponseWhenSuccessfullyUpdated() {
        Long idCat = 10L;
        Category category = new Category();
        category.setIdCategory(idCat);
        category.setName("Name");
        category.setColor("#ff00ff");

        String updatedName = "ChangedName";
        String updatedColor = "#00ff00";

        CategoryRequest request = new CategoryRequest(updatedName, updatedColor);

        CategoryResponse response = mock(CategoryResponse.class);

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepo.findByIdAndIdUser(idCat, 1L)).thenReturn(Optional.of(category));
        when(categoryRepo.save(category)).thenReturn(category);
        when(mapper.toResponse(any(Category.class))).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(idCat, request);
        
        assertEquals(updatedName, category.getName());
        assertEquals(updatedColor, category.getColor());
        assertEquals(response, result);
        verify(categoryRepo).save(category);
        verify(mapper).toResponse(category);
    }

    @Test
    void updateCategoryFailsIfCategoryDoesNotBelongToUser() {
        CategoryRequest request = mock(CategoryRequest.class);
        
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepo.findByIdAndIdUser(10L, 1L)).thenReturn(Optional.empty());
    
        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(10L, request));
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test 
    void deleteCategorySuccessfullyDeletesCategory() {
        Long idUser = 1L;
        UserEntity user = authenticatedUser(idUser);
        Long idCat = 10L;
        Category category = new Category();
        category.setIdCategory(idCat);
        category.setName("Name");
        category.setColor("#ff00ff");

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(categoryRepo.findByIdAndIdUser(idCat, idUser)).thenReturn(Optional.of(category));
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

        categoryService.deleteCategory(idCat);

        verify(taskRepo).clearCategoryByCategoryAndUser(idCat, user);
        verify(eventRepo).clearCategoryByCategoryAndUser(idCat, user);
        verify(noteRepo).clearCategoryByCategoryAndUser(idCat, user);
        verify(categoryRepo).delete(category);
    }

    @Test
    public void validateCreationRequestDoesNotThrowExceptionWhenCorrectRequest() {
        CategoryRequest request = new CategoryRequest("Name", "#ffffff");
        UserEntity user = authenticatedUser(1L);

        assertDoesNotThrow(() -> categoryService.validateCreationRequest(request, user));
        verify(categoryRepo.existsByName(request.name(), user));
    }

    @Test 
    void validateCreationRequestThrowsExceptionWhenNullRequest() {
        CategoryRequest request = null;
        UserEntity user = mock(UserEntity.class);

        assertThrows(InvalidRequestException.class, () -> categoryService.validateCreationRequest(request, user));
    }

    @Test 
    void validateCreationRequestThrowsExceptionWhenNullName() {
        CategoryRequest request = new CategoryRequest(null, "#ffffff");
        UserEntity user = mock(UserEntity.class);

        assertThrows(InvalidRequestException.class, () -> categoryService.validateCreationRequest(request, user));
    }

    @Test 
    void validateCreationRequestThrowsExceptionWhenBlankColor() {
        CategoryRequest request = new CategoryRequest("Name", "");
        UserEntity user = mock(UserEntity.class);

        assertThrows(InvalidRequestException.class, () -> categoryService.validateCreationRequest(request, user));
    }

    @Test 
    void validateCreationRequestThrowsExceptionWhenCategoryNameExists() {
        String name = "Name";
        CategoryRequest request = new CategoryRequest(name, "#ff0000");
        UserEntity user = mock(UserEntity.class);

        when(categoryRepo.existsByName(name, user)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> categoryService.validateCreationRequest(request, user));
    }


    private UserEntity authenticatedUser(Long idUser) {
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);
        lenient().when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        lenient().when(userRepo.findById(idUser)).thenReturn(Optional.of(user));
        return user;
    }



}
