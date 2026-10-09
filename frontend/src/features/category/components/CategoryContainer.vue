<script lang="ts" setup>
import type { Category } from '../types/category.ts';
import CategoryContainerItem from './CategoryContainerItem.vue';

const props = defineProps<{
    /** Categories to display. */
    categories: Category[]
}>()

const emit = defineEmits<{
    /** Forwards a category deletion request to the parent view. */
    delete: [idCategory: number]
}>()

</script>
<template>
    <!-- Renders the user's categories and forwards category deletion requests. -->
    <div class="d-flex flex-row gap-4 flex-wrap">
        <CategoryContainerItem
            v-for="cat in categories"
            :key="cat.idCategory"
            :category="cat"
            @delete="emit('delete', $event)"
        />
    </div>
</template>
<style scoped>
</style>