package com.example.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.category.controller.CategoryController;
import com.example.category.dto.CategoryRequest;
import com.example.category.dto.CategoryResponse;
import com.example.category.service.CategoryService;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getAllCategoriesReturnsCategoryList() throws Exception {

        CategoryResponse category = mock(CategoryResponse.class);

        when(categoryService.getAllCategories()).thenReturn(java.util.List.of(category));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));

        verify(categoryService).getAllCategories();
    }

    @Test
    void createCategoryReturnsCategoryWhenValidRequest() throws Exception {

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Work"
                    }
                """))
            .andExpect(status().isOk());

        verify(categoryService).createCategory(any(CategoryRequest.class));
    }

    @Test
    void createCategoryPassesCorrectRequestToService() throws Exception {

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(mock(CategoryResponse.class));

        mockMvc.perform(post("/api/categories/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Work",
                        "color": "#ffffff"
                    }
                """))
            .andExpect(status().isOk());

        ArgumentCaptor<CategoryRequest> captor = ArgumentCaptor.forClass(CategoryRequest.class);

        verify(categoryService).createCategory(captor.capture());

        CategoryRequest request = captor.getValue();

        assertEquals("Work", request.name());
        assertEquals("#ffffff", request.color());
    }

    @Test
    void createCategoryReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        mockMvc.perform(post("/api/categories/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "",
                        "color": "#000000"
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CategoryRequest.class));
    }

    @Test
    void updateCategoryReturnsCategoryWhenValidRequest() throws Exception {

        Long idCategory = 1L;
        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryService.updateCategory(eq(idCategory), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/{idCategory}", idCategory)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Updated category"
                    }
                """))
            .andExpect(status().isOk());

        verify(categoryService).updateCategory(eq(idCategory), any(CategoryRequest.class));
    }

    @Test
    void updateCategoryPassesCorrectRequestToService()
            throws Exception {

        Long idCategory = 1L;

        when(categoryService.updateCategory(eq(idCategory), any(CategoryRequest.class))).thenReturn(mock(CategoryResponse.class));

        mockMvc.perform(put("/api/categories/{idCategory}", idCategory)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Updated category"
                    }
                """))
            .andExpect(status().isOk());

        ArgumentCaptor<CategoryRequest> captor = ArgumentCaptor.forClass(CategoryRequest.class);

        verify(categoryService).updateCategory(eq(idCategory), captor.capture());

        CategoryRequest request = captor.getValue();

        assertEquals("Updated category", request.name());
    }

    @Test
    void updateCategoryReturnsBadRequestWhenRequestIsInvalid() throws Exception {

        mockMvc.perform(put("/api/categories/{idCategory}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": ""
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(categoryService, never())
                .updateCategory(
                    anyLong(),
                    any(CategoryRequest.class));
    }

    @Test
    void updateCategoryReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(put("/api/categories/{idCategory}", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Updated category"
                    }
                """))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(anyLong(), any(CategoryRequest.class));
    }

    @Test
    void deleteCategoryReturnsNoContentWhenSuccessfulRequest() throws Exception {

        Long idCategory = 1L;

        mockMvc.perform(delete("/api/categories/{idCategory}", idCategory))
            .andExpect(status().isOk());

        verify(categoryService).deleteCategory(idCategory);
    }

    @Test
    void deleteCategoryReturnsBadRequestWhenIdIsInvalid() throws Exception {

        mockMvc.perform(delete("/api/categories/{idCategory}", "abc"))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).deleteCategory(anyLong());
    }
}


