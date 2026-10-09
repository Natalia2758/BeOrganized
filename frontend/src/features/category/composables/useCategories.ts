import { ref } from "vue"
import { type Category, type CategoryRequest } from "../types/category"
import { createCategory, deleteCategory, getCategories } from "../api/categoryApi"
import { getCategoryErrorMessage, getCategoryListErrorMessage } from "../api/categoryApiError"


/** Manages the category collection and its loading and error state. */
export function useCategories() {

    const categories = ref<Category[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    /** Loads all categories for the current user. */
    async function loadCategories(): Promise<void> {
        loading.value = true
        error.value = null

        try {
            categories.value = await getCategories()
        } catch (e) {
            error.value = getCategoryListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /**
     * Creates a category and appends it to the collection.
     * @param request Category name and color.
     */
    async function addCategory(request: CategoryRequest): Promise<void> {
        error.value = null

        try {
            const createdCat = await createCategory(request)

            categories.value.push(createdCat)
        } catch (e) {
            error.value = getCategoryErrorMessage(e)
            throw e
        }
    }

    /**
     * Deletes a category and removes it from the collection.
     * @param idCategory ID of the category to remove.
     */
    async function removeCategory(idCategory: number) {
        error.value = null
        loading.value = true

        try {
            await deleteCategory(idCategory)

            categories.value = categories.value.filter(category => category.idCategory !== idCategory)
        } catch (e) {
            error.value = getCategoryErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    return {
        categories,
        loading,
        error,
        loadCategories,
        addCategory,
        removeCategory
    }
}