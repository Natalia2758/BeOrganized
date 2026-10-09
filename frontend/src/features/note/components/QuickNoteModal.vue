<script setup lang="ts">
import type { NoteCreateRequest, NoteCreateRequestQuick } from '../types/note.ts';
import NoteForm from './NoteForm.vue';
import QuickNoteForm from './QuickNoteForm.vue';


const props = defineProps<{
    /** Error message from the parent creation action, if any. */
    'error': string | null
    /** Disables form actions while the request is in progress. */
    'isSubmitted': boolean
}>()

const emit = defineEmits<{
    /** Requests that the parent close the modal. */
    close: []
    /** Forwards validated quick-note data to the parent. */
    submit: [request: NoteCreateRequestQuick]
}>()
</script>

<template>
    <!-- Presents the quick-note form in a modal and forwards submit and close actions. -->
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h2 class="modal-title">Quick Note Addition</h2>
                    <button
                        type="button"
                        class="btn-close"
                        @click="emit('close')"
                    ></button>
                </div>
                <div class="modal-body">
                    <div
                        v-if="error"
                        class="alert alert-danger"
                    >
                        {{ error }}
                    </div>
                    <QuickNoteForm
                        :is-submitted="props.isSubmitted"
                        @submit="emit('submit', $event)"
                        @cancel="emit('close')"
                    />
                </div>
            </div>
        </div>
    </div>
    <div class="modal-backdrop fade show"></div>
</template>