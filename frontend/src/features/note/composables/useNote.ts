import { ref } from "vue";
import type { Note } from "../types/note";
import { getNote } from "../api/noteApi";
import { getNoteErrorMessage } from "../api/noteApiError";

/** Loads and exposes the state of a single note. */
export function useNote() {
    const note = ref<Note | null>(null)
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Fetches note details and tracks loading and API errors.
     * @param idNote ID of the note to load.
     */
    async function loadNote(idNote: number) {
        loading.value = true
        error.value = null

        try {
            note.value = await getNote(idNote)
        } catch (e) {

            error.value = getNoteErrorMessage(e)

        } finally {
            loading.value = false
        }
    }

    /** Resets note state when the route ID cannot be parsed. */
    function invalidNoteId() {
        error.value = 'Invalid note ID'
        note.value = null
        loading.value = false
    }

    return {
        note,
        loading,
        error,
        invalidNoteId,
        loadNote
    }
}