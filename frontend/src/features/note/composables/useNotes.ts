import { ref } from "vue";
import type { Note, NoteCreateRequest, NoteCreateRequestQuick, NoteFilter } from "../types/note";
import { createNote, createNoteQuick, deleteNote, getNotes, getNotesByDate, updateNote } from "../api/noteApi";
import { getNoteErrorMessage, getNoteListErrorMessage } from "../api/noteApiError";

/** Manages note collections and their loading and error state. */
export function useNotes() {
    const notes = ref<Note[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Loads notes matching the optional note filter.
     * @param params Optional note filters.
     */
    async function loadNotes(params?: NoteFilter) {
        loading.value = true
        error.value = null
        try {
            const result = await getNotes(params)
            notes.value = result ?? []
        } catch (e) {
            error.value = getNoteListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /**
     * Creates a note and appends it to the current collection.
     * @param request Note details to create.
     */
    async function addNote(request: NoteCreateRequest) {
        error.value = null

        try {
            const createdNote = await createNote(request)
            notes.value.push(createdNote)
            return createdNote
        } catch (e) {
            error.value = getNoteErrorMessage(e)
            throw e
        }
    }

    /**
     * Creates a quick note and appends it to the current collection.
     * @param request Note details to create.
     */
    async function addNoteQuick(request: NoteCreateRequestQuick) {
        error.value = null

        try {
            const createdNote = await createNoteQuick(request)
            notes.value.push(createdNote)
            return createdNote
        } catch (e) {
            error.value = getNoteErrorMessage(e)
        }
    }

    /**
     * Saves note changes and replaces the matching collection entry.
     * @param idNote ID of the note to update.
     * @param request Updated note details.
     */
    async function editNote(idNote: number, request: NoteCreateRequest) {
        error.value = null

        try {
            const updatedNote = await updateNote(idNote, request)
            const index = notes.value.findIndex(note => note.idNote === idNote)

            if (index !== -1) {
                notes.value[index] = updatedNote
            }
            return updatedNote
        } catch (e) {
            error.value = getNoteErrorMessage(e)
            throw e
        }
    }

    /**
     * Deletes a note and removes it from the current collection.
     * @param idNote ID of the note to remove.
     */
    async function removeNote(idNote: number) {
        error.value = null

        try {
            await deleteNote(idNote)

            notes.value = notes.value.filter(
                note => note.idNote !== idNote
            )
        } catch (e) {
            error.value = getNoteErrorMessage(e)
            throw e
        }
    }

    /**
     * Loads notes associated with the given date.
     * @param date Date whose notes should be loaded.
     */
    async function loadNotesByDate(date: Date) {
        error.value = null
        loading.value = true

        try {
            notes.value = await getNotesByDate(date)
        } catch (e) {
            error.value = getNoteListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    return {
        notes,
        loading,
        error,
        loadNotes,
        addNote,
        addNoteQuick,
        editNote,
        removeNote,
        loadNotesByDate
    }
}