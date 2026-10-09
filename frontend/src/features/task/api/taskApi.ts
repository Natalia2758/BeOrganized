import { toApiError } from "@/common/api/apiError";
import http from "@/common/api/http";
import { type Task, type TaskCreateRequest, type TaskFilter, type TaskCreateRequestQuick, type SubtaskCompletedRequest, type Subtask, type TaskFinishedRequest } from "@/features/task/types/task"

/**
 * Retrieves tasks matching the optional filter.
 * @param params Optional task filters.
 */
export async function getTasks(params?: TaskFilter): Promise<Task[]> {
    try {
        const response = await http.get<Task[]>('/tasks', { params })

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves one task, including its subtasks.
 * @param idTask ID of the task to retrieve.
 */
export async function getTask(idTask: number): Promise<Task> {

    try {
        const response = await http.get<Task>(`/tasks/${idTask}`)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates a task using the full task form.
 * @param request Task details to create.
 */
export async function createTask(request: TaskCreateRequest): Promise<Task> {

    try {
        const response = await http.post<Task>('/tasks/create', request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates a task using the quick-create form.
 * @param request Task details to create.
 */
export async function createTaskQuick(request: TaskCreateRequestQuick): Promise<Task> {
    try {
        const response = await http.post<Task>('/tasks/create-quick', request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates an existing task and its submitted details.
 * @param idTask ID of the task to update.
 * @param request Updated task details.
 */
export async function updateTask(idTask: number, request: TaskCreateRequest): Promise<Task> {

    try {
        const response = await http.put<Task>(`/tasks/${idTask}`, request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates a subtask's completion state.
 * @param request Subtask ID and completion state.
 */
export async function updateSubtaskCompletion(request: SubtaskCompletedRequest): Promise<Subtask> {
    try {
        const response = await http.patch<Subtask>(`/tasks/subtask`, request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates a task's finished status.
 * @param request Task ID and finished state.
 */
export async function updateTaskFinishedStatus(request: TaskFinishedRequest): Promise<Task> {
    try {
        const response = await http.patch<Task>(`/tasks/task`, request)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Deletes a task by ID.
 * @param idTask ID of the task to delete.
 */
export async function deleteTask(idTask: number ): Promise<void> {
    try {
        await http.delete(`/tasks/${idTask}`)
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves tasks associated with a local calendar date.
 * @param date Local calendar date to query.
 */
export async function getTasksByDate(date: Date): Promise<Task[]> {
    try {
        const tasks = await http.get<Task[]>('/tasks/by-date', {
            params: {
                year: date.getFullYear(),
                month: date.getMonth() + 1,
                day: date.getDate()
            }
        })
        return tasks.data
    } catch (error) {
        throw toApiError(error)
    }
}

/** Retrieves future tasks for reminder target selection. */
export async function getTasksInFuture(): Promise<Task[]> {
    try {
        const response = await http.get<Task[]>('/tasks/future')

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
    
}
