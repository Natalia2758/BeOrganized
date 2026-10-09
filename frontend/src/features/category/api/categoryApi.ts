import { type Category, type CategoryRequest } from "../types/category"
import { toApiError } from "@/common/api/apiError"
import http from "@/common/api/http"

/** Retrieves all categories belonging to the current user. */
export async function getCategories(): Promise<Category[]> {

    try {
        const response = await http.get<Category[]>('/categories')

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
    
}

/**
 * Retrieves one category by ID.
 * @param idCategory ID of the category to retrieve.
 */
export async function getCategory(idCategory: number): Promise<Category> {
    try {
        const response = await http.get<Category>(`/categories/${idCategory}`)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates a category from its name and color.
 * @param request Category name and color.
 */
export async function createCategory(request: CategoryRequest): Promise<Category> {

    try {
        const response = await http.post<Category>('/categories/create', request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates a category's name and color.
 * @param idCategory ID of the category to update.
 * @param request Updated category name and color.
 */
export async function updateCategory(idCategory: number, request: CategoryRequest): Promise<Category> {
    try {
        const response = await http.put<Category>(`/categories/${idCategory}`, request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Deletes a category by ID.
 * @param idCategory ID of the category to delete.
 */
export async function deleteCategory(idCategory: number): Promise<void> {
    try {
        await http.delete(`/categories/${idCategory}`)
    } catch (error) {
        throw toApiError(error)
    }
}