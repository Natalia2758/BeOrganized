<script setup lang="ts">
import { computed } from 'vue';
import TaskListItem from './TaskListItem.vue';
import { type Task, type TaskFinishedRequest } from "@/features/task/types/task"

/** Tasks to display, optionally limited to the dashboard preview. */
const props = defineProps<{
    tasks: Task[]
    forDashboard?: boolean
}>()

const emit = defineEmits<{
    /** Forwards a task completion change to the parent view. */
    toggle: [task: TaskFinishedRequest]
    /** Forwards a request to delete a task. */
    deleteRequest: [task: Task]
}>()

/** Limits dashboard previews while showing the full list on the task page. */
const displayedTasks = computed(() => {
    return props.forDashboard ? props.tasks.slice(0, 5) : props.tasks
})

</script>

<template>
    <!-- Renders the task collection and forwards item actions. -->
    <div class="d-flex align-items-stretch">
        <ul class="list-group list-group-flush w-100">
            <TaskListItem 
                v-for="task in displayedTasks" 
                :key="task.idTask" 
                :task="task"
                :for-dashboard="props.forDashboard"
                @toggle="emit('toggle', $event)"
                @delete-request="emit('deleteRequest', $event)"
            />    
        </ul>
        <div
            v-if="props.forDashboard && displayedTasks.length > 5"
            class="tasks__more align-self-end"
        >
            + {{ props.tasks.length - displayedTasks.length }}
        </div>
    </div>
</template>

<style scoped>
.tasks__more {
    color: var(--app-text-muted);
}
</style>