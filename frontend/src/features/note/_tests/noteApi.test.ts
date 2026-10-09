import { beforeEach, describe, expect, it, vi } from 'vitest'
import http from '@/common/api/http'
import { ApiError, toApiError } from '@/common/api/apiError'
import {
	createNote,
	createNoteQuick,
	deleteNote,
	getNote,
	getNotes,
	getNotesByDate,
	updateNote,
} from '@/features/note/api/noteApi'
import type {
	Note,
	NoteCreateRequest,
	NoteCreateRequestQuick,
} from '@/features/note/types/note'

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

const testNote: Note = {
	idNote: 1,
	title: 'Note title',
	content: 'Note content',
	date: '2026-10-01',
	category: undefined,
}

const testNoteRequest: NoteCreateRequest = {
	title: 'Note title',
	content: 'Note content',
	date: '2026-10-01',
	idCategory: null,
}

const testNoteQuickRequest: NoteCreateRequestQuick = {
	title: 'Quick note',
	content: 'Quick content',
}

const testNotes: Note[] = [testNote]

beforeEach(() => {
	vi.clearAllMocks()
})

describe('noteApi', () => {
	it('gets notes', async () => {
		vi.mocked(http.get).mockResolvedValue({
			data: testNotes,
		})

		const result = await getNotes()

		expect(http.get).toHaveBeenCalledWith('/notes', {
			params: undefined,
		})
		expect(result).toEqual(testNotes)
	})

	it('gets a note by id', async () => {
		vi.mocked(http.get).mockResolvedValue({
			data: testNote,
		})

		const result = await getNote(testNote.idNote)

		expect(http.get).toHaveBeenCalledWith(`/notes/${testNote.idNote}`)
		expect(result).toEqual(testNote)
	})

	it('creates a note', async () => {
		vi.mocked(http.post).mockResolvedValue({
			data: testNote,
		})

		const result = await createNote(testNoteRequest)

		expect(http.post).toHaveBeenCalledWith('/notes/create', testNoteRequest)
		expect(result).toEqual(testNote)
	})

	it('creates a quick note', async () => {
		vi.mocked(http.post).mockResolvedValue({
			data: testNote,
		})

		const result = await createNoteQuick(testNoteQuickRequest)

		expect(http.post).toHaveBeenCalledWith('/notes/create-quick', testNoteQuickRequest)
		expect(result).toEqual(testNote)
	})

	it('updates a note', async () => {
		const updatedRequest: NoteCreateRequest = {
			...testNoteRequest,
			title: 'Updated note title',
		}
		const updatedNote: Note = {
			...testNote,
			title: 'Updated note title',
		}

		vi.mocked(http.put).mockResolvedValue({
			data: updatedNote,
		})

		const result = await updateNote(testNote.idNote, updatedRequest)

		expect(http.put).toHaveBeenCalledWith(`/notes/${testNote.idNote}`, updatedRequest)
		expect(result).toEqual(updatedNote)
	})

	it('deletes a note', async () => {
		vi.mocked(http.delete).mockResolvedValue({})

		await deleteNote(testNote.idNote)

		expect(http.delete).toHaveBeenCalledWith(`/notes/${testNote.idNote}`)
	})

	it('gets notes by date', async () => {
		const date = new Date(2026, 9, 1)

		vi.mocked(http.get).mockResolvedValue({
			data: testNotes,
		})

		const result = await getNotesByDate(date)

		expect(http.get).toHaveBeenCalledWith('/notes/calendar', {
			params: {
				year: date.getFullYear(),
				month: date.getMonth() + 1,
				day: date.getDate(),
			},
		})
		expect(result).toEqual(testNotes)
	})

	it('converts API errors', async () => {
		const httpError = new Error('network error')
		const apiError = new ApiError('API error', 0)

		vi.mocked(http.get).mockRejectedValue(httpError)
		vi.mocked(toApiError).mockReturnValue(apiError)

		await expect(getNote(1)).rejects.toBe(apiError)
		expect(toApiError).toHaveBeenCalledWith(httpError)
	})
})
