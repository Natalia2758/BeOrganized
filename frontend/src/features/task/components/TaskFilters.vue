<script setup lang="ts">
import { onMounted } from 'vue';
import { useCategories } from '@/features/category/composables/useCategories.ts';

const {
    categories,
    loadCategories
} = useCategories()

onMounted(() => {
    loadCategories()
})

const props = defineProps<{
    /** Current task-title search query. */
    search: string
    /** Selected category ID, or undefined when no category filter is active. */
    catId: number | undefined
    /** Completion filter, or undefined to include all tasks. */
    completed: boolean | undefined
}>()

const emit = defineEmits<{
    /** Emits changes to the search query. */
    'update:search': [value: string]
    /** Emits changes to the selected category. */
    'update:idCategory': [value: number | undefined]
    /** Emits changes to the completion filter. */
    'update:isCompleted': [value: boolean | undefined]
    /** Requests that the parent clear all task filters. */
    'reset': []
}>()

/** Delegates resetting filter state to the parent view. */
function resetFilters() {
    emit('reset')
}

</script>

<template>
    <!-- Provides task search, category, and completion filters. -->
    <div class="d-flex flex-row gap-4">
        <input
            class="form-control w-25"
            :value="props.search"
            placeholder="Search tasks"
            type="text"
            @input="emit('update:search', ($event.target as HTMLInputElement).value)"
        >
        <select
            class="form-select w-25"
            :value="props.catId"
            @change="emit(
                'update:idCategory',
                Number(($event.target as HTMLSelectElement).value)
            )"
        >   
            <option value="" disabled selected>Category</option>
            <option
                v-for="category in categories"
                :key="category.idCategory"
                :value="category.idCategory"
            >
                {{ category.name }}
            </option>
        </select>

        <select
            class="form-select w-25"
            :value="props.completed"
            @change="emit(
                'update:isCompleted',
                ($event.target as HTMLSelectElement).value === 'undefined'
                ? undefined
                : ($event.target as HTMLSelectElement).value === 'true'
            )"
        >
            <option value="" disabled selected>Status</option>
            <option value="undefined">All</option>

            <option :value="true">Completed</option>

            <option :value="false">Active</option>
        </select>

        <button
            type="button"
            class="btn btn-subtle filter__reset"
            @click="resetFilters"
        >
            Reset
        </button>
    </div> 
</template>
<style lang="css" scoped>
.filter__reset {
    min-width: 8rem;
}
</style>