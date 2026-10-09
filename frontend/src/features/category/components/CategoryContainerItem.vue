<script setup lang="ts">
import { computed } from 'vue';
import type { Category } from '../types/category';

const props = defineProps<{
    /** Category represented by this item. */
    category: Category
}>()

const emit = defineEmits<{
    /** Emits the category ID when deletion is requested. */
    delete : [idCategory: number]
}>()

/** Color used as the category card background. */
const color = computed(() => props.category.color)

</script>
<template>
    <!-- Displays one category and emits a request to delete it. -->
    <div class="category card px-3 py-4 flex-row justify-content-between align-items-center" :style="{ backgroundColor: color ?? '' }">
        
        <h3>{{ props.category.name }}</h3>
        
        <button
            type="button"
            class="btn btn-lg link-danger link-opacity-75-hover"
            @click="emit('delete', props.category.idCategory)"
        >
            <i class="bi bi-trash-fill"></i>
        </button>
    </div>
</template>
<style lang="css" scoped>
.category {
    --category-width: 14rem;

    flex: 0 0 10rem;
    width: var(--category-width);
    max-width: var(--category-width);
    min-width: var(--category-width);
    border: 0;
    box-shadow: var(--app-shadow);
}

@media (max-width: 768px) {
    .category {
        --category-width: 100%;
        height: 4rem;
    }
}
</style>