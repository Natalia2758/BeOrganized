<script setup lang="ts">
import { useCategories } from '@/features/category/composables/useCategories';
import { onMounted } from 'vue';


const {
    categories,
    loadCategories
} = useCategories()


onMounted(() => {
    loadCategories()
})

const props = defineProps<{
    /** Current event-title search query. */
    search: string
    /** Selected category ID, or undefined when no category is selected. */
    catId: number | undefined
}>()

const emit = defineEmits<{
    /** Emits changes to the search query. */
    'update:search': [value: string]
    /** Emits changes to the selected category. */
    'update:idCategory': [value: number | undefined]
    /** Requests that the parent clear the active filters. */
    'reset': []
}>()

/** Delegates resetting filter state to the parent view. */
function resetFilters() {
    emit('reset')
}

</script>

<template>
    <!-- Provides event search and category filters and can reset them. -->
    <div class="d-flex flex-row gap-4 ">
        <input
            class="form-control w-25"
            :value="props.search"
            placeholder="Search events"
            type="text"
            @input="emit('update:search', ($event.target as HTMLInputElement).value)"
        />
        <select
            class="form-select w-25"
            :value="props.catId"
            @change="emit('update:idCategory', Number(($event.target as HTMLInputElement).value))"
        >
            <option value="" disabled selected>Category</option>
            <option
                v-for="cat in categories"
                :key="cat.idCategory"
                :value="cat.idCategory"
            >
                {{ cat.name }}
            </option>
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