import type { Category } from "@/features/category/types/category"

export interface Note {
    idNote: number
    title?: string
    content: string
    date?: string
    category?: Category
}

export interface NoteCreateRequest {
    title: string | null
    content: string
    date: string | null
    idCategory: number | null
}

export interface NoteCreateRequestQuick {
    title: string | null
    content: string
}

export interface NoteFilter {
    idCategory?: number
}