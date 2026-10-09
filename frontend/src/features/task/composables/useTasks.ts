import { ref } from 'vue'
import { getTasks, createTask, updateTask, deleteTask, createTaskQuick, getTasksByDate, updateTaskFinishedStatus, getTasksInFuture } from '../api/taskApi'
import { type Task, type TaskCreateRequest, type TaskFilter, type TaskCreateRequestQuick, type TaskFinishedRequest } from '@/features/task/types/task'
import { getTaskErrorMessage, getTaskListErrorMessage } from '../api/taskApiError'

/** Signals task-list consumers to reload after task mutations. */
const taskListReloadToken = ref(0)


/** Exposes the shared task-list reload signal. */
export function useTaskListReloadToken() {
    return taskListReloadToken
}

/** Notifies task-list consumers that task data has changed. */
export function triggerTaskListReload() {
    taskListReloadToken.value += 1
}


/** Manages task collections and their loading and error state. */
export function useTasks() {

    const tasks = ref<Task[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Loads tasks matching the optional task filter.
     * @param params Optional task filters.
     */
    async function loadTasks(params?: TaskFilter) {
        loading.value = true
        error.value = null

        try {
            const result = await getTasks(params)
            tasks.value = result ?? []
        } catch (e) {
            error.value = getTaskListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /**
     * Creates a task, updates the collection, and signals list consumers.
     * @param request Task details to create.
     */
    async function addTask(request: TaskCreateRequest) {
        error.value = null;

        try {
            const createdTask = await createTask(request)

            tasks.value.push(createdTask)
            triggerTaskListReload()

            return createdTask
        } catch (e) {
            error.value = getTaskErrorMessage(e)
            throw e
        }
    } 

    /**
     * Creates a quick task and signals task-list consumers.
     * @param request Task details to create.
     */
    async function addTaskQuick(request: TaskCreateRequestQuick) {
        error.value = null

        try {
            const createdTask = await createTaskQuick(request)

            tasks.value.push(createdTask)
            triggerTaskListReload()

            return createdTask
        } catch (e) {
            error.value = getTaskErrorMessage(e)
        }
    }

    /**
     * Saves task and subtask changes, then refreshes task-list consumers.
     * @param idTask ID of the task to update.
     * @param request Updated task details.
     * @param subtasks Updated subtask titles.
     */
    async function editTask(idTask: number, request: TaskCreateRequest, subtasks: string[]) {
        error.value = null

        request.subtasks = subtasks

        try {
            const updatedTask = await updateTask(idTask, request)

            const index = tasks.value.findIndex(task => task.idTask === idTask)

            if (index !== -1) {
                tasks.value[index] = updatedTask
            }
            triggerTaskListReload()
            return updatedTask
        } catch (e) {
            error.value = getTaskErrorMessage(e)
        }
    }

    /**
     * Deletes a task and signals task-list consumers.
     * @param idTask ID of the task to remove.
     */
    async function removeTask(idTask: number) {
        error.value = null

        try {
            await deleteTask(idTask)

            tasks.value = tasks.value.filter(
                task => task.idTask !== idTask
            )
            triggerTaskListReload()
        } catch (e) {
            error.value = getTaskErrorMessage(e)
            throw e
        }
    }

    /**
     * Updates a task's finished state and signals task-list consumers.
     * @param request Task ID and finished state.
     */
    async function setFinished(request: TaskFinishedRequest) {
        error.value = null
        
        try {
            const updatedTask = await updateTaskFinishedStatus(request)

            const index = tasks.value.findIndex(task => task.idTask === request.idTask)

            if (index !== -1) {
                tasks.value[index] = updatedTask
            }
            triggerTaskListReload()
            return updatedTask
        } catch (e) {
            error.value = getTaskErrorMessage(e)
        }
    }

    /**
     * Loads tasks associated with the given date.
     * @param date Date whose tasks should be loaded.
     */
    async function loadTasksByDate(date: Date) {
        error.value = null
        loading.value = true

        try {
            tasks.value = await getTasksByDate(date)
        } catch (e) {
            error.value = getTaskListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /** Loads future tasks for reminder target selection. */
    async function loadTasksInFuture() {
        loading.value = true
        error.value = null

        try {
            const result = await getTasksInFuture()
            tasks.value = result ?? []
        } catch (e) {
            error.value = getTaskListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }


    return {
        tasks,
        loading,
        error,
        loadTasks,
        addTask,
        addTaskQuick,
        editTask,
        removeTask,
        setFinished,
        loadTasksByDate,
        loadTasksInFuture
    }

    
}