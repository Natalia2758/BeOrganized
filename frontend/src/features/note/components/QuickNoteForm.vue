<script setup lang="ts">
import { onMounted, reactive } from 'vue';
import type { NoteCreateRequestQuick } from '../types/note';
import { ref } from 'vue';

/** Holds the title and content collected by the quick-note form. */
const form = reactive<NoteCreateRequestQuick>({
    title: null,
    content: ''
})

const titleInput = ref<HTMLInputElement | null>(null)
const errors = ref<Record<string, string>>({})

const props = defineProps<{
    /** Disables form actions while the parent request is in progress. */
    isSubmitted: boolean
}>()

const emit = defineEmits<{
    /** Sends validated quick-note data to the parent. */
    submit: [data: NoteCreateRequestQuick]
    /** Requests that the parent close the form. */
    cancel: []
}>()

/** Focuses the title field when the form is mounted. */
onMounted(() => {
    titleInput.value?.focus()
})

/** Emits a copy of the form values after successful validation. */
function handleSubmit() {
    if (!validate()) return

    emit('submit', { ...form} )
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
    <!-- Collects and validates the fields for creating a note quickly. -->
    <form @submit.prevent="handleSubmit">
        <div>
            <label class="form-label" for="title">Title*</label>
            <input
                class="form-control"
                id="title"
                v-model="form.title"
                ref="titleInput"
                type="text"
            />
            <p v-if="errors.title">
                {{ errors.title }}
            </p>
        </div>
        <div class="mb-5">
            <label class="form-label" for="content"></label>
            <textarea
                class="form-control"
                id="content"
                v-model="form.content"
                placeholder="Note content*"
                rows="5"
            >
            </textarea>
            <p v-if="errors.content">
                {{ errors.content }}
            </p>
        </div>
        <div class="d-flex justify-content-between gap-3">
            <button
                    class="btn btn-danger w-100 w-md-auto"
                    type="button"
                    :disabled="props.isSubmitted"
                    @click="emit('cancel')"
                >
                Cancel
        </button>
            <CommonButton
                class="btn btn-primary w-100 w-md-auto"
                type="submit"
                :disabled="props.isSubmitted"
            >
                {{ props.isSubmitted ? 'Adding...' : 'Add Note'}}
            </CommonButton>
        </div>
    </form>
</template>