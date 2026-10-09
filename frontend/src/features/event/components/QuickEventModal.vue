<script setup lang="ts">
import type { EventCreateQuickRequest } from '../types/event';
import QuickEventForm from './QuickEventForm.vue';

const props = defineProps<{
    /** Disables form actions while creation is in progress. */
    isSubmitted: boolean
    /** Error message from the parent event-creation action, if any. */
    error: string | null
}>()

const emit = defineEmits<{
    /** Forwards validated quick-event data to the parent. */
    submit: [request: EventCreateQuickRequest]
    /** Requests that the parent close the modal. */
    close: []
}>()

</script>

<template>
    <!-- Presents the quick-event form in a modal and forwards submit and close actions. -->
    <div
        class="modal d-block"
        tabindex="-1"
    >
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <h2 class="modal-title">Quick Event Addition</h2>
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

                    <QuickEventForm
                        :isSubmitted="props.isSubmitted"
                        @submit="emit('submit', $event)"
                        @cancel="emit('close')"
                    />
                </div>
            </div>

        </div>
    </div>
    <div class="modal-backdrop fade show"></div>
</template>