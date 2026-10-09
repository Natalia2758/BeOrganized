import http from "@/common/api/http";
import type { Note, NoteCreateRequest, NoteCreateRequestQuick, NoteFilter } from "../types/note";
import { toApiError } from "@/common/api/apiError";

/**
 * Retrieves notes matching the optional filter.
 * @param params Optional note filters.
 */
export async function getNotes(params?: NoteFilter): Promise<Note[]> {
    try {
        const response = await http.get<Note[]>('/notes', { params })
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves one note by ID.
 * @param idNote ID of the note to retrieve.
 */
export async function getNote(idNote: number): Promise<Note> {
    try {
        const response = await http.get<Note>(`/notes/${idNote}`)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates a note using the full note form.
 * @param request Note details to create.
 */
export async function createNote(request: NoteCreateRequest): Promise<Note> {
    try {
        const response = await http.post<Note>('/notes/create', request)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates a note using the quick-create form.
 * @param request Note details to create.
 */
export async function createNoteQuick(request: NoteCreateRequestQuick): Promise<Note> {
    try {
        const response = await http.post<Note>('/notes/create-quick', request)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates an existing note.
 * @param idNote ID of the note to update.
 * @param request Updated note details.
 */
export async function updateNote(idNote: number, request: NoteCreateRequest): Promise<Note> {
    try {
        const response = await http.put<Note>(`/notes/${idNote}`, request)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Deletes a note by ID.
 * @param idNote ID of the note to delete.
 */
export async function deleteNote(idNote: number): Promise<void> {
    try {
        await http.delete(`/notes/${idNote}`)
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves notes associated with a local calendar date.
 * @param date Local calendar date to query.
 */
export async function getNotesByDate(date: Date): Promise<Note[]> {
    try {
        const notes = await http.get<Note[]>('/notes/calendar', {
            params: {
                year: date.getFullYear(),
                month: date.getMonth() + 1,
                day: date.getDate()
            }
        })
        return notes.data
    } catch (error) {
        throw toApiError(error)
    }
}