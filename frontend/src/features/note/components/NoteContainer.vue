<script setup lang="ts">
import { computed } from 'vue';
import type { Note } from '../types/note';
import NoteContainerItem from './NoteContainerItem.vue';

const props = defineProps<{
    /** Notes to display. */
    notes: Note[]
    /** Limits the collection to a dashboard preview when enabled. */
    forDashboard?: boolean
}>()

const emit = defineEmits<{
    /** Forwards a note deletion request to the parent view. */
    deleteRequest: [note: Note]
}>()

/** Limits dashboard previews while preserving all notes on the notes page. */
const displayedNotes = computed(() => {
    return props.forDashboard ? props.notes.slice(0, 6) : props.notes
})

</script>

<template>
    <!-- Renders the note collection and forwards item actions. -->
    <div class="note-container d-flex flex-wrap" :class="forDashboard ? 'note-container--dashboard' : ''">
        <NoteContainerItem
            v-for="note in displayedNotes"
            :key="note.idNote"
            :note="note"
            :for-dashboard="props.forDashboard"
            @delete-request="emit('deleteRequest', $event)"
        />
    </div>
</template>
<style scoped>
    .note-container {
        gap: var(--app-gap-md);
    }

    @media (max-width: 768px) {
        .note-container {
            --note-height: 8rem;

            max-height: calc-size(var(--note-height) * 3);

            &--for-dashboard {
                overflow-y: scroll;
            } 
        }
    }
</style>