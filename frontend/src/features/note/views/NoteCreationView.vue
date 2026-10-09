<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import NoteForm from '../components/NoteForm.vue';
import type { NoteCreateRequest } from '../types/note.ts';
import { onMounted, ref } from 'vue';
import { useNotes } from '../composables/useNotes.ts';
import { useCategories } from '@/features/category/composables/useCategories.ts';
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue';

const {
    error,
    addNote
} = useNotes()

const {
    categories,
    loading: categoriesLoading,
    error: categoriesError,
    loadCategories
} = useCategories()

const router = useRouter()
const route = useRoute()
const showModal = ref(false)

const initialDate = typeof route.query.date === 'string' ? route.query.date : undefined

onMounted(async () => {
    await loadCategories()
    
})

/** Creates a note and navigates back after the request succeeds. */
async function createNote(request: NoteCreateRequest) {

    try {
        const createdNote = await addNote(request)

        router.push({
            name: 'note-details',
            params: {
                idNote: createdNote.idNote
            }
        })
    } catch { }
}

/** Opens confirmation before discarding the new note. */
function handleCancelRequest() {
    showModal.value = true
}

/** Returns to the previous page after cancellation is confirmed. */
function handleConfirmCancel() {
    router.back()
}

</script>

<template>
    <!-- Loads note form data and handles note creation and cancellation. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Add note</h1>
        <div v-if="categoriesLoading">
            Loading categories...
        </div>
        <div class="alert alert-danger"  v-else-if="categoriesError">
            {{ categoriesError }}
        </div>
        <NoteForm
            :submit-label="'Add Note'"
            :categories="categories"
            :initial-date="initialDate"
            @submit="createNote"
            @cancel="handleCancelRequest"
        />
        <div class="alert alert-danger my-2" v-if="error">
            {{ error }}
        </div>
    </div>
    
    <ConfirmCancellingModal
        v-if="showModal"
        :object="'note'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />
</template>