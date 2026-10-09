
import { getTask, updateSubtaskCompletion } from '../api/taskApi'
import type { Subtask, SubtaskCompletedRequest, Task } from '../types/task'
import { computed, ref } from 'vue'
import { getTaskErrorMessage } from '../api/taskApiError'

/** Loads a task and manages its subtask completion state. */
export function useTask() {

    const task = ref<Task | null>(null)
    const subtasks = ref<Subtask[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Fetches the task and initializes the displayed subtask list.
     * @param idTask ID of the task to load.
     */
    async function loadTask(idTask: number) {

        loading.value = true
        error.value = null

        try {
            task.value = await getTask(idTask)
            if (task.value.subtasks.length > 0) {
                subtasks.value = task.value.subtasks
            }
            
        } catch (e) {

            error.value = getTaskErrorMessage(e)

        } finally {
            loading.value = false
        }
    }

    /**
     * Saves a subtask status change and updates the local entry.
     * @param request Subtask ID and completion state.
     */
    async function changeSubtaskCompletion(request: SubtaskCompletedRequest) {

        const subtask = subtasks.value.find(subtask => subtask.idSubtask === request.idSubtask)
        if (!subtask) return

        try {
            const updatedSubtask = await updateSubtaskCompletion(request)

            const index = subtasks.value.findIndex(subtask => subtask.idSubtask === request.idSubtask)

            if (index !== -1) {
                subtasks.value[index] = updatedSubtask
            }

        } catch (e) {

            error.value = getTaskErrorMessage(e)
        }   
    }

    /** Exposes the task's current completion progress. */
    const progressPercentage = computed(() => {
        if (subtasks.value.length <= 0 || !task.value?.progress) return 0
        return task.value.progress
    })

    /** Resets task state when the route ID cannot be parsed. */
    function invalidTaskId() {
        error.value = 'Invalid task ID'
        task.value = null
        loading.value = false
    }

    return {
        task,
        subtasks,
        loading,
        error,
        progressPercentage,
        loadTask,
        changeSubtaskCompletion,
        invalidTaskId
    }
}