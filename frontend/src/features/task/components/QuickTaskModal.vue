<script setup lang="ts">
import type { TaskCreateRequestQuick } from '../types/task.ts';
import QuickTaskForm from './QuickTaskForm.vue';

const props = defineProps<{
    /** Disables form actions while the request is being submitted. */
    isSubmitted: boolean
    /** Error message from the parent task-creation action, if any. */
    error: string | null
}>()

const emit = defineEmits<{
  /** Forwards validated quick-task data to the parent. */
  submit: [request: TaskCreateRequestQuick]
  /** Requests that the parent close the modal. */
  close: []
}>()

</script>
<template>
    <!-- Presents the quick-task form in a modal and forwards submit and close actions. -->
    <div 
        class="modal d-block"
        tabindex="-1"
    >
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h2 class="modal-title">Quick Task Addition</h2>
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

                    <QuickTaskForm
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