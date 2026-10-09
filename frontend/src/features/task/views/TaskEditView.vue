<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTask } from '../composables/useTask'
import { useTasks } from '../composables/useTasks'
import type { TaskCreateRequest } from '../types/task'
import { ApiError } from '@/common/api/apiError'
import { useCategories } from '@/features/category/composables/useCategories'
import TaskForm from '../components/TaskForm.vue'
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue'

const route = useRoute()

const router = useRouter()
const showModal = ref(false)

const {
    task,
    loading,
    error,
    loadTask
} = useTask()

const {
    editTask
} = useTasks()

const {
    categories,
    loading: categoriesLoading,
    error: categoriesError,
    loadCategories
} = useCategories()

onMounted(async () => {
    const idTask = Number(route.params.idTask)

    await loadTask(idTask)
    await loadCategories()

})

async function updateCurrentTask(request: TaskCreateRequest) {
    if (!task.value) return

    try {
        await editTask(
            task.value.idTask,
            request,
            request.subtasks.map(subtask => subtask.trim()).filter(Boolean)
        )

        router.push({
            name: 'task-details',
            params: {
                idTask: task.value.idTask
            }
        })
    } catch (requestError) {
        if (requestError instanceof ApiError) {
            error.value = requestError.message
        } else {
            error.value = 'Unexpected error happened while updating task'
        }
    }
}

function handleCancelRequest() {
    showModal.value = true
}

function handleConfirmCancel() {
    router.back()
}

</script>
<template>
    <!-- Loads a task for editing and handles save or cancellation. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Edit Task</h1>

        <div v-if="loading">
            Loading...
        </div>

        <div class="alert alert-danger my-2" v-else-if="error">
            {{ error }}
        </div>

        <div v-else-if="categoriesLoading">
            Loading categories...
        </div>

        <div class="alert alert-danger"  v-else-if="categoriesError">
            {{ categoriesError }}
        </div>

        <TaskForm 
            v-if="categories && task"
            :categories="categories"
            :initial-task="task"
            :submit-label="'Save'"
            @submit="updateCurrentTask"
            @cancel="handleCancelRequest()"
        />
    </div>

    <ConfirmCancellingModal
        v-if="showModal"
        :object="'task'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />
</template>
