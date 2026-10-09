import { ref } from "vue";
import type { Category } from "../types/category";
import { getCategory } from "../api/categoryApi";

/** Manages the loading state for one category. */
export function useCategory() {
    const category = ref<Category>()
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Fetches category details and records any loading error.
     * @param idCategory ID of the category to load.
     */
    async function loadCategory(idCategory: number) {
        loading.value = true
        error.value = null

        try {
            category.value = await getCategory(idCategory)
        } catch (e) {
            error.value = 'Failed to load categories'
            throw e
        } finally {
            loading.value = false
        }
    }

    return {
        category,
        loading,
        error,
        loadCategory
    }
}