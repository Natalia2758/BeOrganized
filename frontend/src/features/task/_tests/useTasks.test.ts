import { describe, expect, it, vi } from 'vitest'

import {
  getTasks,
  createTask,
  updateTask,
  deleteTask,
  updateTaskFinishedStatus,
  getTasksByDate,
  getTasksInFuture,
} from '@/features/task/api/taskApi'
import { triggerTaskListReload, useTaskListReloadToken, useTasks } from '../composables/useTasks'
import type { Task, TaskCreateRequest } from '../types/task'
import { getTaskListErrorMessage } from "../api/taskApiError";

vi.mock('@/features/task/api/taskApi', () => ({
  getTasks: vi.fn(),
  createTask: vi.fn(),
  createTaskQuick: vi.fn(),
  updateTask: vi.fn(),
  deleteTask: vi.fn(),
  updateTaskFinishedStatus: vi.fn(),
  getTasksByDate: vi.fn(),
  getTasksInFuture: vi.fn(),
}))

vi.mock('@/features/task/utils/taskError', () => ({
  getTaskListErrorMessage: vi.fn(),
}))

const testTasks: Task[] = [
    {
        idTask: 1,
        title: 'Task 1',
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
    },
    {
        idTask: 2,
        title: 'Task 2',
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
    },
    {
        idTask: 3,
        title: 'Task 3',
        description: null,
        priority: null,
        category: null,
        startDate: null,
        duration: false,
        timeStart: null,
        timeEnd: null,
        repeated: false,
        repeatedFrequency: null,
        progress: 100,
        finished: true,
        subtasks: []
    }
]

const testTask: Task = {
    idTask: 4,
    title: 'Task 4',
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

const testRequest: TaskCreateRequest = {
    title: 'Task 4',
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

describe('useTasks', () => {
    it('loads tasks', async () => {
        const tasks = testTasks

        vi.mocked(getTasks).mockResolvedValue(tasks)

        const {
            tasks: taskList,
            loading,
            error,
            loadTasks,
        } = useTasks()

        await loadTasks()

        expect(getTasks).toHaveBeenCalledWith(undefined)
        expect(taskList.value).toEqual(tasks)
        expect(loading.value).toBe(false)
        expect(error.value).toBeNull()
    })

    it('adds a task', async () => {
        const createdTask = testTask
        const taskRequest = testRequest

        vi.mocked(createTask).mockResolvedValue(createdTask)

        const {
            tasks,
            addTask,
        } = useTasks()

        const result = await addTask(taskRequest)

        expect(createTask).toHaveBeenCalledWith(taskRequest)
        expect(tasks.value).toContainEqual(createdTask)
        expect(result).toEqual(createdTask)
    })

    it('triggers a task list reload', () => {
        const token = useTaskListReloadToken()

        const before = token.value

        triggerTaskListReload()

        expect(token.value).toBe(before + 1)
    })

    it('edits a task', async () => {
        const previous = {
            idTask: 4,
            title: 'To be updated',
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

        const updated = testTask
        const request = testRequest

        vi.mocked(updateTask).mockResolvedValue(updated)

        const {
            tasks,
            editTask,
        } = useTasks()

        tasks.value.push(previous)
        
        await editTask(1, request, [])

        expect(updateTask).toHaveBeenCalledWith(
            1,
            request,
        )

        expect(tasks.value[0]).toEqual(updated)
    })

    it('removes a task', async () => {

        vi.mocked(deleteTask).mockResolvedValue(undefined)

        const {
            tasks,
            removeTask,
        } = useTasks()

        tasks.value = testTasks

        await removeTask(1)

        expect(deleteTask).toHaveBeenCalledWith(1)
        expect(tasks.value).toEqual(() => testTasks.splice(0, 1))
    })

    it('updates the finished task', async () => {
        
        const previous = testTask
        const updated = {
            idTask: 4,
            title: 'To be updated',
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
            finished: true,
            subtasks: []
        }

        vi.mocked(updateTaskFinishedStatus).mockResolvedValue(updated)

        const {
            tasks,
            setFinished,
        } = useTasks()

        tasks.value.push(previous)

        await setFinished({
            idTask: 4,
            finished: true,
        })

        expect(tasks.value[0]).toEqual(updated)
    })

    it('loads tasks by date', async () => {
        const tasks = testTasks

        const date = new Date(2026, 9, 1)

        vi.mocked(getTasksByDate).mockResolvedValue(tasks)

        const {
            tasks: taskList,
            loadTasksByDate,
        } = useTasks()

        await loadTasksByDate(date)

        expect(getTasksByDate).toHaveBeenCalledWith(date)
        expect(taskList.value).toEqual(tasks)
    })

    it('loads future tasks', async () => {
        const tasks = testTasks

        vi.mocked(getTasksInFuture).mockResolvedValue(tasks)

        const {
            tasks: taskList,
            loadTasksInFuture,
        } = useTasks()

        await loadTasksInFuture()

        expect(getTasksInFuture).toHaveBeenCalled()
        expect(taskList.value).toEqual(tasks)
    })

    it('sets an error when loading tasks fails', async () => {
        vi.mocked(getTasks).mockRejectedValue(new Error('Network error'))

        vi.mocked(getTaskListErrorMessage).mockReturnValue('Could not load tasks')

        const {
            error,
            loading,
            loadTasks,
        } = useTasks()

        await loadTasks()

        expect(error.value).toBe('Could not load tasks')
        expect(loading.value).toBe(false)
    })
})