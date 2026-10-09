<script setup lang="ts">
import { type TaskCreateRequest } from "@/features/task/types/task"
import { useRouter } from 'vue-router'
import { useTasks } from '@/features/task/composables/useTasks'
import { useCategories } from "@/features/category/composables/useCategories"
import { onMounted } from "vue"
import { ref } from "vue"
import TaskForm from "../components/TaskForm.vue"
import ConfirmCancellingModal from "@/common/components/ui/ConfirmCancellingModal.vue"

const router = useRouter()
const showModal = ref(false)

const {
    error,
    addTask
} = useTasks()

const {
    categories,
    loading: categoriesLoading,
    error: categoriesError,
    loadCategories
} = useCategories()

onMounted(async () => {
    await loadCategories()
})

async function createTask(request: TaskCreateRequest) {

    try {
        const createdTask = await addTask(request)

        router.push({
            name: 'task-details',
            params: {
                idTask: createdTask.idTask
            }
        })
    } catch { }
}

function handleCancelRequest() {
    showModal.value = true
}

function handleConfirmCancel() {
    router.back()
}

</script>
<template>
    <!-- Loads task form data and handles task creation and cancellation. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Create Task</h1>
        <div v-if="categoriesLoading">
            Loading categories...
        </div>
        <div v-else-if="categoriesError" class="alert alert-danger my-2">
            {{ categoriesError }}
        </div>
        
        <TaskForm
            :categories="categories"
            :submit-label="'Add Task'"
            @submit="createTask"
            @cancel="handleCancelRequest"
        />

        <div class="alert alert-danger my-2" v-if="error">
            {{ error }}
        </div>
    </div>
    
    <ConfirmCancellingModal
        v-if="showModal"
        :object="'task'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />          
</template>