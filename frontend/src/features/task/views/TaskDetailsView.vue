<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTask } from '../composables/useTask'
import { triggerTaskListReload, useTasks } from '../composables/useTasks'
import TaskContainer from '../components/TaskContainer.vue'
import type { SubtaskCompletedRequest } from '../types/task.ts'
import CategoryDot from '@/features/category/components/CategoryDot.vue'
import { useIsMobile } from '@/common/composables/useIsMobile.ts'
import ConfirmDeletionModal from '@/common/components/ui/ConfirmDeletionModal.vue'

const route = useRoute()
const router = useRouter()

const { isMobile } = useIsMobile()

const {
    task,
    subtasks,
    loading,
    error,
    progressPercentage,
    loadTask,
    changeSubtaskCompletion,
    invalidTaskId
} = useTask()

const {
    removeTask
} = useTasks()

const showDeletionModal = ref(false)

/** Reloads the requested task whenever its route ID changes. */
watch(
    () => route.params.idTask,
    async (idTaskParam) => {
        const idTask = Number(idTaskParam)

        if (Number.isNaN(idTask)) {
            invalidTaskId()
            return
        }

        await loadTask(idTask)
    },
    { immediate: true }
)

/** Deletes the displayed task and returns to the task list. */
async function handleConfirmDelete() {
    if (!task.value) return

    try {
        await removeTask(task.value.idTask)

        router.push({
            name: 'tasks'
        })
    } finally {
        showDeletionModal.value = false
    }
}

/** Saves a subtask state change and refreshes task progress and list state. */
async function handleCheck(request: SubtaskCompletedRequest) {
    await changeSubtaskCompletion(request)
    await loadTask(Number(route.params.idTask))
    triggerTaskListReload()
}

/** Opens the task deletion confirmation modal. */
function handleDeleteRequest() {
    showDeletionModal.value = true
}
</script>

<template>
    <!-- Loads and displays a task with its subtasks and deletion action. -->
    <div class="mt-5 mt-lg-3">
        <div class="d-flex flex-row align-items-center justify-content-between gap-3 my-lg-3">
            <RouterLink
                class="btn btn-subtle"
                :to="{ name: 'tasks'}"
            >
                <i class="bi bi-arrow-left"></i>
            </RouterLink> 
            <div v-if="loading">Loading task...</div>
            <div v-else class="task__title-container d-flex flex-row justify-content-center align-items-center gap-2">
                <div class="task__title" :class="isMobile ? ' h4' : ' h2'" >{{ task?.title }}</div>
                <CategoryDot
                    :color="task?.category?.color ?? null"
                    :size="'lg'"
                />
            </div>
            
            <div class="task__actions d-flex flex-column flex-md-row gap-3">
                <RouterLink
                    class="btn btn-secondary"
                    :to="{
                        name: 'task-update',
                        params: { 
                            idTask: task?.idTask
                        }
                    }"
                >
                    <span class="d-none d-md-inline">Edit</span>
                    <span class="d-inline d-md-none"><i class="bi bi-pencil-square"></i></span>
                </RouterLink>
                <button
                    type="button"
                    class="btn btn-danger"
                    @click="handleDeleteRequest"
                >
                    <span class="d-none d-md-inline">Delete</span>
                    <span class="d-inline d-md-none"><i class="bi bi-trash-fill"></i></span>
                </button>
            </div>
        </div>
        
        <div v-if="loading">Loading task...</div>
        <div class="alert alert-danger"  v-else-if="error">
            {{ error }}
        </div>

        <TaskContainer
            v-else-if="task"
            :task="task"
            :subtasks="subtasks"
            :progress="progressPercentage"
            @checked="handleCheck"
        />

        <div class="d-flex justify-content-end">
            <RouterLink
                :to="{
                    name: 'reminder-creation',
                    params: {
                        idEventTask: task?.idTask
                    },
                    query: {
                        source: 'task'
                    }
                }"
                class="btn btn-primary"
            >
                Add reminder
            </RouterLink>
        </div>
    </div>
    <ConfirmDeletionModal
        v-if="showDeletionModal"
        :object="'task'"
        @cancel="showDeletionModal = false"
        @yes="handleConfirmDelete"
    />
</template>
<style scoped>
.task__title-container {
    min-width: 0;
    flex: 1 1 auto;
}

.task__title {
    margin: 0;
    overflow-wrap: anywhere;
}

.task__actions {
    flex-shrink: 0;
}
</style>