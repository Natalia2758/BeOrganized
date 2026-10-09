import { describe, expect, it, vi } from "vitest";
import { getNote } from "../api/noteApi";
import type { Note } from "../types/note";
import { useNote } from "../composables/useNote";
import { ApiError } from "@/common/api/apiError";
import { getNoteErrorMessage } from "../api/noteApiError";

vi.mock('@/features/note/api/noteApi', () => ({
	getNote: vi.fn()
}))

vi.mock('../api/noteApiError', () => ({
  getNoteErrorMessage: vi.fn(),
}))

const testNote: Note = {
	idNote: 1,
	title: 'Note title',
	content: 'Note content',
	date: '2026-10-01',
	category: undefined,
}

describe('useNote', () => {
	it('loads a note', async () => {

		const note = testNote

		vi.mocked(getNote).mockResolvedValue(note)

		const {
			note: noteState,
			loading,
			error,
			loadNote
		} = useNote()

		await loadNote(note.idNote)

		expect(getNote).toHaveBeenCalledWith(note.idNote)
		expect(noteState.value).toEqual(note)
		expect(loading.value).toBe(false)
		expect(error.value).toBeNull()
	})

	it('sets an error when failed', async () => {
		const apiError = new ApiError('Request failed', 403)

		vi.mocked(getNote).mockRejectedValue(apiError)
		vi.mocked(getNoteErrorMessage).mockReturnValue('Access denied.')

		const {
			loading,
			error,
			loadNote
		} = useNote()

		await loadNote(1)

		expect(error.value).toBe('Access denied.')
		expect(loading.value).toBe(false)
	})

	it('handles an invalid note id', () => {
		const {
			note,
			loading,
			error,
			invalidNoteId,
		} = useNote()

		invalidNoteId()

		expect(error.value).toBe('Invalid note ID')
		expect(note.value).toBeNull()
		expect(loading.value).toBe(false)
	})
})
