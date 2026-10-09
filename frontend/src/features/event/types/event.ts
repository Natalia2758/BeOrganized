import type { Category } from "@/features/category/types/category"

export interface Event {
    idEvent: number
    title: string
    details: string | null
    category: Category | null
    dateStart: string
    dateEnd: string
    duration: boolean
    timeStart: string | null
    timeEnd: string | null
    repeated: boolean
    repeatedFrequency: RepeatedFrequency | null
    dates?: string[] | null
}

export interface EventCreateQuickRequest {
    title: string
    dateStart: string
    dateEnd: string
}

export interface EventCreateRequest {
    title: string
    details: string | null
    idCategory: number | null
    dateStart: string
    dateEnd: string
    duration: boolean
    timeStart: string | null
    timeEnd: string | null
    repeated: boolean
    repeatedFrequency: RepeatedFrequency | null
}

export interface EventFilter {
    idCategory?: number
    search?: string
}

export enum RepeatedFrequency {
    EVERYDAY ='Everyday',
    EVERY_WEEK = 'Every week',
    EVERY_TWO_WEEKS = 'Every two weeks',
    EVERY_MONTH = 'Every month',
}