import { beforeEach, describe, expect, it, vi } from 'vitest'
import http from '@/common/api/http'
import { ApiError, toApiError } from '@/common/api/apiError'
import {
  createTask,
  deleteTask,
  getTask,
  getTasks,
  getTasksByDate,
  updateTask,
} from '@/features/task/api/taskApi'
import {
  type Task,
  type TaskCreateRequest,
} from '@/features/task/types/task'

vi.mock('@/common/api/http', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

vi.mock('@/common/api/apiError', () => ({
  toApiError: vi.fn(),
}))

beforeEach(() => {
	vi.clearAllMocks()
})

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
    subtasks: []
}

const testTaskRequest: TaskCreateRequest = {
    title: 'Task title',
    description: null,
    priority: null,
    idCategory: null,
    startDate: null,
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: false,
    repeatedFrequency: null,
    subtasks: []
}

describe('taskApi', () => {
    it('gets tasks', async () => {

        const tasks = [ testTask ]

        vi.mocked(http.get).mockResolvedValue({
            data: tasks 
        })

        const result = await getTasks()

        expect(http.get).toHaveBeenCalledWith('/tasks', {
            params: undefined
        })
        expect(result).toEqual(tasks)
    })

    it('gets a task by id', async () => {
        
        const task = testTask

        vi.mocked(http.get).mockResolvedValue({
            data: task
        })

        const result = await getTask(task.idTask)

        expect(http.get).toHaveBeenCalledWith(`tasks/${task.idTask}`)
        expect(result).toEqual(task)
    })

    it('creates a task', async () => {
        const request = testTaskRequest
        const task = testTask

        vi.mocked(http.post).mockResolvedValue({
            data: task
        })

        const result = await createTask(request)

        expect(http.post).toHaveBeenCalledWith('tasks/create', request)
        expect(result).toEqual(task)
    })

    it('updates a task', async () => {

        const request = testTaskRequest
        request.title = 'Updated task'
        const updated = testTask
        updated.title = 'Updated task'

        vi.mocked(http.put).mockResolvedValue({
            data: updated
        })

        const result = await updateTask(updated.idTask, request)

        expect(http.put).toHaveBeenCalledWith(`/tasks/${updated.idTask}`, request)
        expect(result).toEqual(updated)
    })

    it('deletes a task', async () => {
        vi.mocked(http.delete).mockResolvedValue({})
        await deleteTask(1)

        expect(http.delete).toHaveBeenCalledWith('/tasks/1')
    })

    it('gets tasks by date', async () => {
        const tasks = [ testTask ]
        const date = new Date(2026, 9, 1)

        vi.mocked(http.get).mockResolvedValue({
            data: tasks
        })

        const result = await getTasksByDate(date)

        expect(http.get).toHaveBeenCalledWith('/tasks/by-date', {
            params: {
                year: date.getFullYear(),
                month: date.getMonth() + 1,
                day: date.getDate()
            }
        })
        expect(result).toEqual(tasks)
    })

    it('converts API errors', async () => {
        const httpError = new Error('network error')
        const apiError = new ApiError('API error', 1)

        vi.mocked(http.get).mockRejectedValue(httpError)
        vi.mocked(toApiError).mockReturnValue(apiError)

        await expect(getTask(1)).rejects.toBe(apiError)
        expect(toApiError).toHaveBeenCalledWith(httpError)
    })
})