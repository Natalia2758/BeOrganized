<script setup lang="ts">
import { computed } from "vue";
import type { Note } from "../types/note";

const props = defineProps<{
    /** Note represented by this card. */
    note: Note
    /** Applies the dashboard card presentation when enabled. */
    forDashboard?: boolean
}>()

const emit = defineEmits<{
    /** Requests deletion of this note from the parent view. */
    deleteRequest: [note: Note]
}>()

/** Uses the assigned category color or the default note background. */
const catColor = computed(() => props.note.category?.color ?? '#ffe7ae')

</script>

<template>
    <!-- Displays one note with its category and delete action. -->
    <!-- 
    card 
    - content truncated so that it fits card size
    -->
    <div class="note d-flex flex-column overflow-hidden p-2" :class="forDashboard ? 'note--dashboard' : 'note--container'" :style="'background-color: ' + catColor">
            
        <div class="d-flex flex-row align-items-center justify-content-between px-1 px-lg-2">
            <RouterLink
                class="note__title link-dark link-offset-3 link-opacity-75-hover link-underline link-underline-opacity-0 link-underline-opacity-50-hover" 
                :to="{ 
                    name: 'note-details',
                    params: {
                        idNote: props.note.idNote
                    }
                }"
            >
                <h4>{{ props.note.title ?? 'No title'}}</h4>
            </RouterLink>
            <button 
                type="button"
                class="btn btn-lg link-danger link-opacity-75-hover align-self-start"
                @click="emit('deleteRequest', props.note)"
            >
                <i class="bi bi-trash-fill"></i>
            </button>
        </div>
        <div class="d-flex p-2">
            
            <div>{{ props.note.content }}</div>
        </div>
    </div>
    
</template>
<style lang="css" scoped>

.note {
    flex: 0 0 10rem;
    border: 0;
    box-shadow: var(--app-shadow);
    border-radius: var(--app-radius-sm);
}

.note--container {
    --note-width: 16rem;
    --note-height: 8rem;
    width: var(--note-width);
    max-width: var(--note-width);
    min-width: var(--note-width);
    height: var(--note-height);
}

.note--dashboard {
    --note-width: 14rem;
    width: var(--note-width);
    max-width: var(--note-width);
    min-width: var(--note-width);
}

@media (max-width: 576px) {
    .note--container, .note--dashboard {
        --note-width: 100%;
        --note-height: 8rem;
        width: var(--note-width);
        max-width: var(--note-width);
        min-width: var(--note-width);
        height: var(--note-height);
    }
}
</style>
