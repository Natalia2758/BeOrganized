<script setup lang="ts">

/** Date used to display the current calendar month and year. */
defineProps<{
    currentDate: Date,
}>()
/** Navigation actions handled by the parent calendar view. */
const emit = defineEmits<{
    previous: [],
    next: [],
    today: []
}>()

</script>
<template>
    <!-- Displays the current calendar period and navigation controls. -->
    <header class="calendar__header d-flex flex-row justify-content-between align-items-center py-1">
        
        <button
            class="btn"
            @click="emit('previous')"
        >
            <i class="bi bi-caret-left-fill"></i>
        </button>

        <h3>
            {{
                currentDate.toLocaleDateString(
                'en-US',
                {
                    month: 'long',
                    year: 'numeric'
                }
                )
            }}
        </h3>

        <button
            @click="emit('next')"
            class="btn"  
        >
            <i class="bi bi-caret-right-fill"></i>
        </button>
    </header>
</template>

<style scoped>
.calendar__header {
    width: 100%;
    background-color: var(--app-secondary);
    color: var(--app-primary-text-dark);
    border-radius: var(--app-radius-sm);
}

</style>