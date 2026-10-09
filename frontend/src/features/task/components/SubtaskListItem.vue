<script setup lang="ts">
import type { SubtaskCompletedRequest, Subtask } from '../types/task';

const props = defineProps<{
    /** Subtask whose title and completion state are displayed. */
    subtask: Subtask
}>()

const emit = defineEmits<{
    /** Emits the subtask ID and its new completion state. */
    checked: [request: SubtaskCompletedRequest]
}>()

/** Converts the checkbox change into a typed subtask completion request. */
function handleChange(event: Event) {
    const input = event.target as HTMLInputElement

    emit('checked', {
        idSubtask: props.subtask.idSubtask,
        completed: input.checked
    })
}

</script>
<template>
    <!-- Displays one subtask and its completion control. -->
    <li class="list-group-item d-flex flex-row gap-3">
        <input
            class="form-ckeck"
            :checked="subtask.completed"
            type="checkbox"
            @change="handleChange"
        />
        <div>{{ subtask.title }}</div>
    </li>
</template>