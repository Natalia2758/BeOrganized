<script setup lang="ts">
import { computed, onMounted } from 'vue';
import CategoryContainer from '../components/CategoryContainer.vue';
import { useCategories } from '../composables/useCategories';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';

const {
    categories,
    loading,
    error,
    loadCategories,
    removeCategory
} = useCategories()

onMounted(() => {
    void loadCategories()
})

/** Removes the selected category through the category composable. */
async function handleDelete(idCategory: number) {
    
    await removeCategory(idCategory)
}

/** Shows the empty state only after loading finishes without categories. */
const isEmpty = computed(() => {
    return !loading.value && categories.value.length === 0
})

</script>

<template>
    <!-- Displays the user's categories and coordinates category deletion. -->
    <!-- 
     # list of categories
     # option to delete each category
     # btn to add category
     -->
     <div class="d-flex flex-row align-items-center justify-content-between">
        <h1 class="d-flex gap-4">
            <span>Categories</span>
            <i class="bi bi-grid"></i>
        </h1>
        
        <RouterLink class="btn btn-primary" :to="{ name: 'category-creation'}" >
            <span class="d-none d-md-inline">Add category</span>
            <span class="d-inline d-md-none"><i class="bi bi-plus-lg"></i></span>
               
        </RouterLink>
    </div>
 
    <CommonContainer :margin-bottom="true">
        <template #1>
            <div v-if="isEmpty">
                No categories found.
            </div>
            <p v-else-if="loading">Loading categories...</p>
            <p v-else-if="error">{{ error }}</p>
            <CategoryContainer
                v-else
                :categories="categories"
                @delete="handleDelete"
            />
        </template>
    </CommonContainer>
</template>
