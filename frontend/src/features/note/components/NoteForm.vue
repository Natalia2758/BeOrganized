<script setup lang="ts">
import { onMounted, reactive, watch } from 'vue';
import type { Note, NoteCreateRequest } from '../types/note';
import { ref } from 'vue';
import type { Category } from '@/features/category/types/category';

/** Holds the editable note title, content, date, and category. */
const form = reactive<NoteCreateRequest>({
    title: null,
    content: '',
    date: null,
    idCategory: null
})

const titleInput = ref<HTMLInputElement | null>(null)
const errors = ref<Record<string, string>>({})

const props = defineProps<{
    /** Existing note to populate when editing. */
    initialNote?: Note
    /** Optional label for the submit button. */
    submitLabel?: string
    /** Categories available for note assignment. */
    categories: Category[]
    /** Optional date used to initialize a new note. */
    initialDate?: string
}>()

const emit = defineEmits<{
    /** Sends validated note data to the parent view. */
    submit: [data: NoteCreateRequest]
    /** Requests that the parent view cancel editing. */
    cancel: []
}>()

/** Focuses the title field and applies an optional initial date. */
onMounted(() => {
    titleInput.value?.focus()
    if (props.initialDate) form.date = props.initialDate
})

/** Populates the form when an existing note is provided for editing. */
watch(
    () => props.initialNote,
    note => {
        if (!note) return

        form.title = note.title ?? null
        form.content = note.content
        form.date = note.date ?? null
        form.idCategory = note.category?.idCategory ?? null
    },
    { immediate: true }
)

/** Emits a copy of the current form values after validation. */
function handleSubmit() {
    if (!validate()) return

    emit('submit', { ...form} )
}

/** Delegates cancellation to the parent view. */
function handleCancel() {
    emit('cancel')
}

/** Checks title length and requires note content. */
function validate() {
    errors.value = {}

    if (form.title && form.title.length > 200) {
        errors.value.title = 'Title is too long'
    }
    if (!form.content) {
        errors.value.content = 'Note cannot be empty'
    }

    return Object.keys(errors.value).length === 0
}
</script>

<template>
    <!-- Collects and validates the fields for creating or editing a note. -->
    <form @submit.prevent="handleSubmit">
        <div class="row">
            <div class="col-12 col-md-6 mb-4 mb-md-0">
                <label class="form-label" for="title">Title</label>
                <input
                    class="form-control"
                    id="title"
                    v-model="form.title"
                    ref="titleInput"
                    type="text"
                />
                <div v-if="errors.title" class="alert alert-danger mt-3">
                    {{ errors.title }}
                </div>
            </div>
            <div class="col-6 col-md-3">
                <label class="form-label" for="category">Category</label>
                <select
                    class="form-select"
                    v-model="form.idCategory"
                >
                    <option disabled v-if="categories.length === 0">
                        No categories available
                    </option>
                    <option
                        v-else
                        v-for="cat in categories"
                        :key="cat.idCategory"
                        :value="cat.idCategory"
                    >
                        {{ cat.name }}
                    </option>
                </select>
            </div>
            <div class="col-6 col-md-3">
                <label class="form-label" for="date">Date</label>
                <input
                    class="form-control"
                    id="date"
                    v-model="form.date"
                    type="date"
                />
            </div>
        </div>
        <div class="row mb-5">
            <div class="col col-12">
                <label class="form-label" for="content"></label>
                <textarea
                    class="form-control"
                    id="content"
                    v-model="form.content"
                    placeholder="Note content"
                    rows="5"
                >
                </textarea>
                <div v-if="errors.content" class="alert alert-danger mt-3">
                    {{ errors.content }}
                </div>
            </div>
        </div>
        <div class="row justify-content-between">
            <div class="col-6 col-md-3">
                <button
                    class="btn btn-danger w-100"
                    @click="handleCancel"
                    type="button"
                >
                    Cancel
                </button>
            </div>
            <div class="col-6 col-md-3">
                <button
                    type="submit"
                    class="btn btn-primary w-100"
                >
                    {{ props.submitLabel ?? 'Save' }}
                </button>
            </div>
        </div>
        
        
    </form>
</template>