import { describe, expect, it, vi } from "vitest";
import { getTask, updateSubtaskCompletion } from "../api/taskApi";
import type { Subtask, SubtaskCompletedRequest, Task } from "../types/task";
import { useTask } from "../composables/useTask";
import { ApiError } from "@/common/api/apiError";
import { getTaskErrorMessage } from "../api/taskApiError";

vi.mock('@/features/task/api/taskApi', () => ({
    getTask: vi.fn(),
    updateSubtaskCompletion: vi.fn()
}))

vi.mock('@/features/task/utils/taskError', () => ({
  getTaskErrorMessage: vi.fn(),
}))

const testTask: Task = {
    idTask: 1,
    title: 'Task title',
    description: null,
    priority: null,
    category: null,
    startDate: null,
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: false,
    repeatedFrequency: null,
    progress: 0,
    finished: false,
    subtasks: [
        {
            idSubtask: 10,
            title: 'Subtask title',
            completed: false
        }
    ]
}

const testTaskWithSubtasks: Task = {
    idTask: 1,
    title: 'Task title',
    description: null,
    priority: null,
    category: null,
    startDate: null,
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: false,
    repeatedFrequency: null,
    progress: 0,
    finished: false,
    subtasks: [
        {
            idSubtask: 10,
            title: 'Subtask1',
            completed: false
        },
        {
            idSubtask: 15,
            title: 'Subtask2',
            completed: true
        },
        {
            idSubtask: 16,
            title: 'Subtask3',
            completed: true
        },
        {
            idSubtask: 18,
            title: 'Subtask4',
            completed: false
        }
            
    ]
}

describe('useTask', () => {
    it('loads a task', async () => {

        const task = testTask

        vi.mocked(getTask).mockResolvedValue(task)

        const {
            task: taskState,
            subtasks,
            loading,
            error,
            loadTask
        } = useTask()

        await loadTask(task.idTask)

        expect(getTask).toHaveBeenCalledWith(task.idTask)
        expect(taskState.value).toEqual(task)
        expect(subtasks.value).toEqual(task.subtasks)
        expect(loading.value).toBe(false)
        expect(error.value).toBeNull
    })

    it('sets an error when failed', async () => {
        const apiError = new ApiError('Request failed', 403)

        vi.mocked(getTask).mockRejectedValue(apiError)
        vi.mocked(getTaskErrorMessage).mockReturnValue('Access denied.')

        const {
            loading,
            error,
            loadTask
        } = useTask()

        await loadTask(1)

        expect(error.value).toBe('Access denied.')
        expect(loading.value).toBe(false)
    })

    it('updates a subtask completion', async () => {
        const task = testTaskWithSubtasks
        const subtaskRequest: SubtaskCompletedRequest = { idSubtask: 10, completed: true}
        const updatedSubtask = { idSubtask: 10, title: 'Subtask1', completed: true}

        vi.mocked(getTask).mockResolvedValue(task)
        vi.mocked(updateSubtaskCompletion).mockResolvedValue(updatedSubtask)

        const {
            subtasks,
            loadTask,
            changeSubtaskCompletion
        } = useTask()

        await loadTask(task.idTask)

        await changeSubtaskCompletion(subtaskRequest)

        expect(updateSubtaskCompletion).toHaveBeenCalledWith(subtaskRequest)
        expect(subtasks.value[0]).toEqual(updatedSubtask)
    })

    it('does nothing when the subtask does not exist', async () => {
        const {
            changeSubtaskCompletion,
        } = useTask()

        await changeSubtaskCompletion({
            idSubtask: 999,
            completed: true,
        })

        expect(updateSubtaskCompletion).not.toHaveBeenCalled()
    })

    it('handles an invalid task id', () => {
        const {
            task,
            loading,
            error,
            invalidTaskId,
        } = useTask()

        invalidTaskId()

        expect(error.value).toBe('Invalid task ID')
        expect(task.value).toBeNull()
        expect(loading.value).toBe(false)
    })
})