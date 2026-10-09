import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
	createNote,
	createNoteQuick,
	deleteNote,
	getNotes,
	getNotesByDate,
	updateNote,
} from '@/features/note/api/noteApi'
import { getNoteErrorMessage, getNoteListErrorMessage } from '@/features/note/api/noteApiError'
import { useNotes } from '../composables/useNotes'
import type { Note, NoteCreateRequest, NoteCreateRequestQuick } from '../types/note'

vi.mock('@/features/note/api/noteApi', () => ({
	getNotes: vi.fn(),
	createNote: vi.fn(),
	createNoteQuick: vi.fn(),
	updateNote: vi.fn(),
	deleteNote: vi.fn(),
	getNotesByDate: vi.fn(),
}))

vi.mock('@/features/note/api/noteApiError', () => ({
	getNoteErrorMessage: vi.fn(),
	getNoteListErrorMessage: vi.fn(),
}))

const testNotes: Note[] = [
	{
		idNote: 1,
		title: 'Note 1',
		content: 'Content 1',
		date: '2026-10-01',
		category: undefined,
	},
	{
		idNote: 2,
		title: 'Note 2',
		content: 'Content 2',
		date: '2026-10-02',
		category: undefined,
	},
	{
		idNote: 3,
		title: 'Note 3',
		content: 'Content 3',
		date: '2026-10-03',
		category: undefined,
	},
]

const testNote: Note = {
	idNote: 4,
	title: 'Note 4',
	content: 'Content 4',
	date: '2026-10-04',
	category: undefined,
}

const testRequest: NoteCreateRequest = {
	title: 'Note 4',
	content: 'Content 4',
	date: '2026-10-04',
	idCategory: null,
}

const testQuickRequest: NoteCreateRequestQuick = {
	title: 'Quick note',
	content: 'Quick content',
}

beforeEach(() => {
	vi.clearAllMocks()
})

describe('useNotes', () => {
	it('loads notes', async () => {
		const notes = testNotes

		vi.mocked(getNotes).mockResolvedValue(notes)

		const {
			notes: noteList,
			loading,
			error,
			loadNotes,
		} = useNotes()

		await loadNotes()

		expect(getNotes).toHaveBeenCalledWith(undefined)
		expect(noteList.value).toEqual(notes)
		expect(loading.value).toBe(false)
		expect(error.value).toBeNull()
	})

	it('adds a note', async () => {
		const createdNote = testNote
		const request = testRequest

		vi.mocked(createNote).mockResolvedValue(createdNote)

		const {
			notes,
			addNote,
		} = useNotes()

		const result = await addNote(request)

		expect(createNote).toHaveBeenCalledWith(request)
		expect(notes.value).toContainEqual(createdNote)
		expect(result).toEqual(createdNote)
	})

	it('adds a quick note', async () => {
		const createdNote = testNote
		const request = testQuickRequest

		vi.mocked(createNoteQuick).mockResolvedValue(createdNote)

		const {
			notes,
			addNoteQuick,
		} = useNotes()

		const result = await addNoteQuick(request)

		expect(createNoteQuick).toHaveBeenCalledWith(request)
		expect(notes.value).toContainEqual(createdNote)
		expect(result).toEqual(createdNote)
	})

	it('edits a note', async () => {
		const previous = {
			idNote: 4,
			title: 'To be updated',
			content: 'Old content',
			date: '2026-10-04',
			category: undefined,
		}

		const updated = testNote
		const request = testRequest

		vi.mocked(updateNote).mockResolvedValue(updated)

		const {
			notes,
			editNote,
		} = useNotes()

		notes.value.push(previous)

		await editNote(4, request)

		expect(updateNote).toHaveBeenCalledWith(4, request)
		expect(notes.value[0]).toEqual(updated)
	})

	it('removes a note', async () => {
		vi.mocked(deleteNote).mockResolvedValue(undefined)

		const {
			notes,
			removeNote,
		} = useNotes()

		notes.value = testNotes

		await removeNote(1)

		expect(deleteNote).toHaveBeenCalledWith(1)
		expect(notes.value).toEqual(testNotes.filter(note => note.idNote !== 1))
	})

	it('loads notes by date', async () => {
		const notes = testNotes
		const date = new Date(2026, 9, 1)

		vi.mocked(getNotesByDate).mockResolvedValue(notes)

		const {
			notes: noteList,
			loadNotesByDate,
		} = useNotes()

		await loadNotesByDate(date)

		expect(getNotesByDate).toHaveBeenCalledWith(date)
		expect(noteList.value).toEqual(notes)
	})

	it('sets an error when loading notes fails', async () => {
		vi.mocked(getNotes).mockRejectedValue(new Error('Network error'))
		vi.mocked(getNoteListErrorMessage).mockReturnValue('Could not load notes')

		const {
			error,
			loading,
			loadNotes,
		} = useNotes()

		await loadNotes()

		expect(error.value).toBe('Could not load notes')
		expect(loading.value).toBe(false)
	})

	it('sets an error when adding a note fails', async () => {
		vi.mocked(createNote).mockRejectedValue(new Error('Network error'))
		vi.mocked(getNoteErrorMessage).mockReturnValue('Could not add note')

		const {
			error,
			notes,
			addNote,
		} = useNotes()

		await expect(addNote(testRequest)).rejects.toThrow('Network error')

		expect(error.value).toBe('Could not add note')
		expect(notes.value).toEqual([])
	})

	it('sets an error when adding a quick note fails', async () => {
		vi.mocked(createNoteQuick).mockRejectedValue(new Error('Network error'))
		vi.mocked(getNoteErrorMessage).mockReturnValue('Could not add quick note')

		const {
			error,
			notes,
			addNoteQuick,
		} = useNotes()

		await addNoteQuick(testQuickRequest)

		expect(error.value).toBe('Could not add quick note')
		expect(notes.value).toEqual([])
	})

	it('sets an error when editing a note fails', async () => {
		vi.mocked(updateNote).mockRejectedValue(new Error('Network error'))
		vi.mocked(getNoteErrorMessage).mockReturnValue('Could not edit note')

		const {
			error,
			notes,
			editNote,
		} = useNotes()

		notes.value.push(testNote)

		await expect(editNote(4, testRequest)).rejects.toThrow('Network error')

		expect(error.value).toBe('Could not edit note')
	})

	it('sets an error when removing a note fails', async () => {
		vi.mocked(deleteNote).mockRejectedValue(new Error('Network error'))
		vi.mocked(getNoteErrorMessage).mockReturnValue('Could not remove note')

		const {
			error,
			notes,
			removeNote,
		} = useNotes()

		notes.value.push(testNote)

		await expect(removeNote(4)).rejects.toThrow('Network error')

		expect(error.value).toBe('Could not remove note')
		expect(notes.value).toEqual([testNote])
	})

	it('sets an error when loading notes by date fails', async () => {
		const date = new Date(2026, 9, 1)

		vi.mocked(getNotesByDate).mockRejectedValue(new Error('Network error'))
		vi.mocked(getNoteListErrorMessage).mockReturnValue('Could not load notes by date')

		const {
			error,
			loading,
			loadNotesByDate,
		} = useNotes()

		await loadNotesByDate(date)

		expect(error.value).toBe('Could not load notes by date')
		expect(loading.value).toBe(false)
	})
})
