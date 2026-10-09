import { type Category } from "@/features/category/types/category"

export interface Task {
    idTask: number
    title: string
    description: string | null
    priority: Priority | null
    category: Category | null
    startDate: string | null
    duration: boolean
    timeStart: string | null
    timeEnd: string | null
    repeated: boolean
    repeatedFrequency: RepeatedFrequency | null
    dates?: string[] | null
    progress: number
    finished: boolean
    subtasks: Subtask[]
}

export interface Subtask {
    idSubtask: number
    title: string
    completed: boolean
}

export interface TaskCreateRequest {
    title: string
    description: string | null
    priority: Priority | null
    idCategory: number | null
    startDate: string | null
    duration: boolean
    timeStart: string | null
    timeEnd: string | null
    repeated: boolean
    repeatedFrequency: RepeatedFrequency | null
    subtasks: string[]
}

export interface TaskCreateRequestQuick {
    title: string
    priority: Priority | null
    startDate: string | null
}

export interface SubtaskCompletedRequest {
    idSubtask: number
    completed: boolean
}

export interface TaskFinishedRequest {
    idTask: number
    finished: boolean
}

export interface TaskFilter {
    finished?: boolean
    idCategory?: number
    search?: string
}

export enum RepeatedFrequency {
    EVERYDAY ='Everyday',
    EVERY_WEEK = 'Every week',
    EVERY_TWO_WEEKS = 'Every two weeks',
    EVERY_MONTH = 'Every month',
}

export enum Priority {
    HIGH = 'HIGH',
    MEDIUM = 'MEDIUM',
    LOW = 'LOW'
}