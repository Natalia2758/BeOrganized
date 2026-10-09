<script setup lang="ts">
import NoteForm from '../components/NoteForm.vue';
import { useNote } from '../composables/useNote';
import { useNotes } from '../composables/useNotes';
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCategories } from '@/features/category/composables/useCategories.ts';
import type { NoteCreateRequest } from '../types/note.ts';
import { ref } from 'vue';
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue';

const route = useRoute()
const router = useRouter()
const showModal = ref(false)

const {
    note,
    loading,
    error,
    loadNote
} = useNote()

const {
    editNote
} = useNotes()

const {
    categories,
    loading: categoriesLoading,
    error: categoriesError,
    loadCategories
} = useCategories()

onMounted(async () => {
    const idNote = Number(route.params.idNote)

    await loadNote(idNote)
    await loadCategories()
})

/** Saves the edited note and returns to its details page. */
async function updateCurrentNote(request: NoteCreateRequest) {
    if (!note.value) return

    try {
        await editNote(note.value.idNote, request)

        router.push({
            name: 'note-details',
            params: {
                idNote: note.value.idNote
            }
        })
    } catch { }
}

/** Opens confirmation before discarding note edits. */
function handleCancelRequest() {
    showModal.value = true
}

/** Returns to the previous page after cancellation is confirmed. */
function handleConfirmCancel() {
    router.back()
}

</script>

<template>
    <!-- Loads a note for editing and handles save or cancellation. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Edit note</h1>

        <div v-if="loading">
            Loading...
        </div>

        <div v-else-if="error">
            {{ error }}
        </div>

        <div v-if="categoriesLoading">
            Loading categories...
        </div>
        <div class="alert alert-danger"  v-else-if="categoriesError">
            {{ categoriesError }}
        </div>

        <NoteForm
            v-else-if="note"
            :categories="categories"
            :initial-note="note"
            submit-label="Save"
            @submit="updateCurrentNote"
            @cancel="handleCancelRequest"
        />

        <ConfirmCancellingModal
            v-if="showModal"
            :object="'task'"
            @yes="handleConfirmCancel"
            @no="showModal = false"
        />
    </div>
</template>