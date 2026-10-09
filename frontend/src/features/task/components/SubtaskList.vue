<script setup lang="ts">
import type { Subtask, SubtaskCompletedRequest } from '../types/task';
import SubtaskListItem from './SubtaskListItem.vue';

const props = defineProps<{
    /** Subtasks to display for the current task. */
    subtasks: Subtask[]
}>()

const emit = defineEmits<{
    /** Forwards a completion change to the parent task view. */
    checked: [subtask: SubtaskCompletedRequest]
}>()

</script>
<template>
    <!-- Renders the subtasks belonging to a task. -->
    <ul class="list-group list-group-flush">
        <SubtaskListItem
            v-for="subtask in subtasks"
            :key="subtask.idSubtask"
            :subtask="subtask"
            @checked="emit('checked', $event)"
        />
    </ul>   
</template>