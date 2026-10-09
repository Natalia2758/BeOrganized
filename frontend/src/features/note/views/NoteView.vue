<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import NoteContainer from '../components/NoteContainer.vue';
import { useNotes } from '../composables/useNotes';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import type { Note } from '../types/note.ts';

const {
    notes,
    loading,
    error,
    loadNotes,
    removeNote
} = useNotes()

const noteToDelete = ref<Note | null>(null)

onMounted(async () => {
    await loadNotes()
})

/** Shows the empty state only after note loading has completed. */
const isEmpty = computed(() => {
    return !loading.value && notes.value.length === 0
})

/** Opens deletion confirmation for the selected note. */
function handleDeleteRequest(note: Note) {
    noteToDelete.value = note
}

/** Deletes the selected note and clears the pending selection. */
async function handleConfirmDelete() {
    if (!noteToDelete.value) return

    try {
        await removeNote(noteToDelete.value.idNote)
    } finally {
        noteToDelete.value = null
    }
}

</script>

<template>
    <!-- Displays notes and provides navigation to note creation and details. -->
    <div>
        <div class="d-flex flex-row align-items-center justify-content-between">
            <h1 class="d-flex gap-4">
                <span>Notes</span>
                <i class="bi bi-card-list"></i>
            </h1>
            <RouterLink class="btn btn-primary" :to="{ name: 'note-creation'}">
                <span class="d-none d-md-inline">Add note</span>
                <span class="d-inline d-md-none"><i class="bi bi-plus-lg"></i></span>
            </RouterLink>
        </div>
        
        <CommonContainer :margin-bottom="true">
            <template #1>
                <div v-if="isEmpty">
                    No notes found.
                </div>
                <div v-else-if="loading">Loading notes...</div>
                <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
                <NoteContainer
                    v-else
                    :notes="notes"
                    @delete-request="handleDeleteRequest"
                />
            </template>
        </CommonContainer>
        
    </div>
    <ConfirmDeletionModal
        v-if="noteToDelete"
        :object="'task'"
        @cancel="noteToDelete = null"
        @yes="handleConfirmDelete"
    />
    
</template>