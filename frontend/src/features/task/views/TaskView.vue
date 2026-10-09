<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router';
import TaskList from '../components/TaskList.vue';
import { useTasks } from '../composables/useTasks.ts';
import { type Task, type TaskFinishedRequest } from "@/features/task/types/task"
import TaskFilters from '../components/TaskFilters.vue';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import { useTaskListReloadToken } from '../composables/useTasks.ts'
import { ApiError } from '@/common/api/apiError.ts';

const {
    tasks,
    loading,
    error,
    loadTasks,
    removeTask,
    setFinished
} = useTasks()

/** Current task-list filters, watched together to reload the list. */
const search = ref('')
const idCategory = ref<number | undefined>(undefined)
const isCompleted = ref<boolean | undefined>(undefined)
const taskListReloadToken = useTaskListReloadToken()
const taskToDelete = ref<Task | null>(null)

/** Reloads tasks whenever a filter or explicit reload token changes. */
watch(
    [search, idCategory, isCompleted, taskListReloadToken],
    async ([searchValue, categoryValue, completedValue]) => {
        await loadTasks({
            search: searchValue || undefined,
            idCategory: categoryValue,
            finished: completedValue
        })
    },
    { immediate: true }
)

/** Persists a task completion change. */
async function toggleTask(request: TaskFinishedRequest) {
    await setFinished(request)
}

/** Shows the empty state only after loading has completed. */
const isEmpty = computed(() => {
    return !loading.value && tasks.value.length === 0
})

/** Clears all active task filters. */
function handleResetRequest() {
    search.value = ''
    idCategory.value = undefined
    isCompleted.value = undefined
}

/** Opens the deletion confirmation for the selected task. */
function handleDeleteRequest(task: Task) {
    taskToDelete.value = task
}

/** Deletes the selected task and always clears the pending selection. */
async function handleConfirmDelete() {
    if(!taskToDelete.value) return

    try {
        await removeTask(taskToDelete.value.idTask)
    } finally {
        taskToDelete.value = null
    }
}

</script>
<template>
    <!-- Displays searchable, filterable tasks and handles completion and deletion. -->
    <div class="d-flex flex-row align-items-center justify-content-between">
        <h1 class="d-flex gap-4">
            <span>Tasks</span>
            <i class="bi bi-clipboard-check"></i>
        </h1>
        <RouterLink class="btn btn-primary" :to="{ name: 'task-creation'}">
            <span class="d-none d-md-inline">Add task</span>
            <span class="d-inline d-md-none"><i class="bi bi-plus-lg"></i></span>
        </RouterLink>
    </div>
    
    <div class="d-none d-md-block">
        <CommonContainer  :margin-bottom="true">
            <template #1>
                <TaskFilters
                    :search="search" 
                    :completed="isCompleted"
                    :catId="idCategory"

                    @update:search="search = $event"
                    @update:idCategory="idCategory = $event"
                    @update:isCompleted="isCompleted = $event"
                    @reset="handleResetRequest"
                >
                </TaskFilters>
            </template>
        </CommonContainer>
    </div>
    
    <CommonContainer :margin-bottom="true">
        <template #1>
            <div v-if="isEmpty">
                No tasks found.
            </div>

            <div v-else-if="loading">Loading tasks...</div>
            <div class="alert alert-danger my-2"  v-else-if="error">
                {{ error }}
            </div>
            
            <TaskList
                v-else
                :tasks="tasks"
                @toggle="toggleTask"
                @delete-request="handleDeleteRequest"
            />
        </template>
    </CommonContainer>

    <div v-if="error" class="alert alert-danger my-2">
        {{ error }}
    </div>

    <ConfirmDeletionModal
        v-if="taskToDelete"
        :object="'task'"
        @cancel="taskToDelete = null"
        @yes="handleConfirmDelete"
    />
</template>