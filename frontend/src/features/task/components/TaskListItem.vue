<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { type Task, type TaskFinishedRequest } from "@/features/task/types/task"
import CategoryDot from '@/features/category/components/CategoryDot.vue';

const props = defineProps<{
    /** Task represented by this list row. */
    task: Task
    /** Uses the compact presentation when shown on the dashboard. */
    forDashboard?: boolean
}>()

const emit = defineEmits<{
    /** Emits the task's updated finished state. */
    toggle: [task: TaskFinishedRequest]
    /** Requests deletion of this task from the parent view. */
    deleteRequest: [task: Task]
}>()

/** Converts the checkbox state into a task completion request. */
function handleChange(event: Event) {
    const input = event.target as HTMLInputElement

    emit('toggle', {
        idTask: props.task.idTask,
        finished: input.checked
    })
}

</script>

<template>
    <!-- Displays a task row and emits completion and deletion actions. -->
    <li class="list-group-item d-flex align-items-center justify-content-between py-1 py-lg-2 gap-1 gap-lg-2"
        :class="props.forDashboard ? 'task--for-dashboard' : 'task'"
    >
        <!-- :checked (controlled by the "task" prop)
                => if task.completed is true - the checkbox is checked
                => if task.completed is false - the checkbox is unchecked -->
        <div class="task__checkbox">
            <input
                v-if="props.task.subtasks.length === 0"
                
                type="checkbox" 
                :checked="task.finished"
                @change="handleChange"
            />
        </div>
        
        <div class="task__title d-flex flex-row align-items-center gap-1 justify-content-center">
            <RouterLink
                class="link-offset-3 link-opacity-75-hover link-underline link-underline-opacity-0 link-underline-opacity-50-hover text-center d-flex mt-1"
                :class="props.task.finished ? 'link-opacity-50' : ''"
                :to="{
                    name: 'task-details',
                    params: {
                        idTask: task.idTask
                    }
                }"
            >
                <h5>{{ task.title }}</h5>
            </RouterLink>
            <CategoryDot
                :color="task.category?.color ?? null"
                :size="'md'"
            />
        </div>
        
        <div class="task__button d-flex justify-content-end">
            <button 
                type="button"
                class="btn btn-lg link-danger link-opacity-75-hover"
                @click="emit('deleteRequest', props.task)"
            >
                <i class="bi bi-trash-fill"></i>
            </button>
        </div>
    </li>

</template>
<style lang="css" scoped>

.task__checkbox {
    flex: 0 0 10%;
}
.task__title {
    flex: 1 0 80%;
}

.task__button {
    flex: 0 0 10%;
}

.task--for-dashboard {
    font-size: medium;
}
</style>