import { beforeEach, describe, expect, it, vi } from 'vitest'
import http from '@/common/api/http'
import { toApiError } from '@/common/api/apiError'
import {
  changeStatus,
  createReminder,
  deleteReminder,
  getReminder,
  getReminders,
  getRemindersByDate,
  getSentReminders,
  updateReminder,
} from '@/features/reminder/api/reminderApi'
import {
    ReminderStatus,
    ReminderType,
  Timing,
  type Reminder,
  type ReminderCreateRequest,
  type ReminderStatusChangeRequest,
} from '@/features/reminder/types/reminder'

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

beforeEach(() => {
	vi.clearAllMocks()
})

const testReminder: Reminder = {
	idReminder: 1,
	content: 'Reminder title',
	type: ReminderType.TASK as const,
	idEvent: null,
	idTask: 1,
	repeatForAll: false,
	remindAt: Timing.AT_TIME as const,
	remindDate: '2026-10-01',
	remindTime: '09:00',
	sentTime: null,
	status: ReminderStatus.PENDING as const,
}

const testReminderRequest: ReminderCreateRequest = {
	content: 'Reminder title',
	type: ReminderType.TASK as const,
	idEvent: null,
	idTask: 1,
	repeatForAll: false,
	remindAt: Timing.SET_MANUALLY as const,
	remindDate: '2026-10-01',
	remindTime: '09:00',
}

const testStatusChangeRequest: ReminderStatusChangeRequest = {
	idReminder: 1,
	status: ReminderStatus.OPENED as const,
}

describe('reminderApi', () => {
	it('gets reminders', async () => {

		const reminders = [testReminder]

		vi.mocked(http.get).mockResolvedValue({
			data: reminders
		})

		const result = await getReminders()

		expect(http.get).toHaveBeenCalledWith('/reminders')
		expect(result).toEqual(reminders)
	})

	it('gets a reminder by id', async () => {

		const reminder = testReminder

		vi.mocked(http.get).mockResolvedValue({
			data: reminder
		})

		const result = await getReminder(reminder.idReminder)

		expect(http.get).toHaveBeenCalledWith(`/reminders/${reminder.idReminder}`)
		expect(result).toEqual(reminder)
	})

	it('creates a reminder', async () => {
		const request = testReminderRequest
		const reminders = [testReminder]

		vi.mocked(http.post).mockResolvedValue({
			data: reminders
		})

		const result = await createReminder(request)

		expect(http.post).toHaveBeenCalledWith('/reminders/create', request)
		expect(result).toEqual(reminders)
	})

	it('updates a reminder', async () => {

		const request = testReminderRequest
		const updated = testReminder

		vi.mocked(http.put).mockResolvedValue({
			data: updated
		})

		const result = await updateReminder(updated.idReminder, request)

		expect(http.put).toHaveBeenCalledWith(`/reminders/${updated.idReminder}`, request)
		expect(result).toEqual(updated)
	})

	it('changes reminder status', async () => {

		const request = testStatusChangeRequest
		const updated = { ...testReminder, status: 'OPENED' as const }

		vi.mocked(http.patch).mockResolvedValue({
			data: updated
		})

		const result = await changeStatus(request)

		expect(http.patch).toHaveBeenCalledWith(`/reminders/${request.idReminder}/status`, request)
		expect(result).toEqual(updated)
	})

	it('deletes a reminder', async () => {
		vi.mocked(http.delete).mockResolvedValue({})

		await deleteReminder(1)

		expect(http.delete).toHaveBeenCalledWith('/reminders/1')
	})

	it('gets reminders by date', async () => {
		const reminders = [testReminder]
		const date = new Date(2026, 9, 1)

		vi.mocked(http.get).mockResolvedValue({
			data: reminders
		})

		const result = await getRemindersByDate(date)

		expect(http.get).toHaveBeenCalledWith('/reminders/by-date', {
			params: {
				year: date.getFullYear(),
				month: date.getMonth() + 1,
				day: date.getDate()
			}
		})
		expect(result).toEqual(reminders)
	})

	it('gets sent reminders', async () => {
		const reminders = [testReminder]

		vi.mocked(http.get).mockResolvedValue({
			data: reminders
		})

		const result = await getSentReminders()

		expect(http.get).toHaveBeenCalledWith('/reminders/sent')
		expect(result).toEqual(reminders)
	})

	it('converts API errors', async () => {
		const httpError = new Error('network error')
		const apiError = { message: 'API error', status: 1 } as unknown as import('@/common/api/apiError').ApiError

		vi.mocked(http.get).mockRejectedValue(httpError)
		vi.mocked(toApiError).mockReturnValue(apiError)

		await expect(getReminder(1)).rejects.toBe(apiError)
		expect(toApiError).toHaveBeenCalledWith(httpError)
	})
})
