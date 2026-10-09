import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
	changeStatus,
	createReminder,
	deleteReminder,
	getReminders,
	getRemindersByDate,
	updateReminder,
} from '@/features/reminder/api/reminderApi'
import { getReminderErrorMessage, getReminderListErrorMessage } from '@/features/reminder/api/reminderApiError'
import { useReminders } from '../composables/useReminders'
import {
    ReminderType,
	Timing,
	type Reminder,
	type ReminderCreateRequest,
	type ReminderStatus,
	type ReminderStatusChangeRequest,
} from '../types/reminder'

vi.mock('@/features/reminder/api/reminderApi', () => ({
	getReminders: vi.fn(),
	createReminder: vi.fn(),
	updateReminder: vi.fn(),
	changeStatus: vi.fn(),
	deleteReminder: vi.fn(),
	getRemindersByDate: vi.fn(),
}))

vi.mock('@/features/reminder/api/reminderApiError', () => ({
	getReminderErrorMessage: vi.fn(),
	getReminderListErrorMessage: vi.fn(),
}))

const testReminders: Reminder[] = [
	{
		idReminder: 1,
		content: 'Reminder 1',
		type: ReminderType.TASK as const,
		idEvent: null,
		idTask: 1,
		repeatForAll: false,
		remindAt: Timing.AT_TIME as const,
		remindDate: '2026-10-01',
		remindTime: '09:00',
		sentTime: null,
		status: 'PENDING' as ReminderStatus,
	},
	{
		idReminder: 2,
		content: 'Reminder 2',
		type: ReminderType.EVENT as const,
		idEvent: 2,
		idTask: null,
		repeatForAll: false,
		remindAt: Timing.DAY_BEFORE as const,
		remindDate: '2026-10-02',
		remindTime: '10:00',
		sentTime: null,
		status: 'SENT' as ReminderStatus,
	},
]

const testReminder: Reminder = {
	idReminder: 3,
	content: 'Reminder 3',
	type: ReminderType.NONE as const,
	idEvent: null,
	idTask: null,
	repeatForAll: true,
	remindAt: Timing.SET_MANUALLY as const,
	remindDate: '2026-10-03',
	remindTime: '11:00',
	sentTime: null,
	status: 'PENDING' as ReminderStatus,
}

const testRequest: ReminderCreateRequest = {
	content: 'Reminder 3',
	type: ReminderType.NONE as const,
	idEvent: null,
	idTask: null,
	repeatForAll: true,
	remindAt: Timing.SET_MANUALLY as const,
	remindDate: '2026-10-03',
	remindTime: '11:00',
}

const statusChangeRequest: ReminderStatusChangeRequest = {
	idReminder: 3,
	status: 'CANCELLED' as ReminderStatus,
}

beforeEach(() => {
	vi.clearAllMocks()
})

describe('useReminders', () => {
	it('loads reminders', async () => {
		const reminders = testReminders

		vi.mocked(getReminders).mockResolvedValue(reminders)

		const {
			reminders: reminderList,
			loading,
			error,
			loadReminders,
		} = useReminders()

		await loadReminders()

		expect(getReminders).toHaveBeenCalledWith()
		expect(reminderList.value).toEqual(reminders)
		expect(loading.value).toBe(false)
		expect(error.value).toBeNull()
	})

	it('adds a reminder', async () => {
		const createdReminders = [testReminder]
		const request = testRequest

		vi.mocked(createReminder).mockResolvedValue(createdReminders)

		const {
			reminders,
			addReminder,
		} = useReminders()

		await addReminder(request)

		expect(createReminder).toHaveBeenCalledWith(request)
		expect(reminders.value).toEqual([testReminder])
	})

	it('edits a reminder', async () => {
		const previous = {
			...testReminder,
			content: 'To be updated',
		}
		const updated = testReminder

		vi.mocked(updateReminder).mockResolvedValue(updated)

		const {
			reminders,
			editReminder,
		} = useReminders()

		reminders.value.push(previous)

		const result = await editReminder(3, testRequest)

		expect(updateReminder).toHaveBeenCalledWith(3, testRequest)
		expect(reminders.value[0]).toEqual(updated)
		expect(result).toEqual(updated)
	})

	it('updates reminder status', async () => {
		const previous = testReminder
		const updated = {
			...testReminder,
			status: 'CANCELLED' as ReminderStatus,
		}

		vi.mocked(changeStatus).mockResolvedValue(updated)

		const {
			reminders,
			updateStatus,
		} = useReminders()

		reminders.value.push(previous)

		const result = await updateStatus(statusChangeRequest)

		expect(changeStatus).toHaveBeenCalledWith(statusChangeRequest)
		expect(reminders.value[0]).toEqual(updated)
		expect(result).toEqual(updated)
	})

	it('removes a reminder', async () => {
		vi.mocked(deleteReminder).mockResolvedValue(undefined)

		const {
			reminders,
			removeReminder,
		} = useReminders()

		reminders.value = testReminders

		await removeReminder(1)

		expect(deleteReminder).toHaveBeenCalledWith(1)
		expect(reminders.value).toEqual([testReminders[1]])
	})

	it('loads reminders by date', async () => {
		const reminders = testReminders
		const date = new Date(2026, 9, 1)

		vi.mocked(getRemindersByDate).mockResolvedValue(reminders)

		const {
			reminders: reminderList,
			loadRemindersByDate,
		} = useReminders()

		await loadRemindersByDate(date)

		expect(getRemindersByDate).toHaveBeenCalledWith(date)
		expect(reminderList.value).toEqual(reminders)
	})

	it('sets an error when loading reminders fails', async () => {
		vi.mocked(getReminders).mockRejectedValue(new Error('Network error'))
		vi.mocked(getReminderListErrorMessage).mockReturnValue('Could not load reminders')

		const {
			error,
			loading,
			loadReminders,
		} = useReminders()

		await loadReminders()

		expect(error.value).toBe('Could not load reminders')
		expect(loading.value).toBe(false)
	})

	it('sets an error when adding a reminder fails', async () => {
		vi.mocked(createReminder).mockRejectedValue(new Error('Network error'))
		vi.mocked(getReminderErrorMessage).mockReturnValue('Could not add reminder')

		const {
			error,
			reminders,
			addReminder,
		} = useReminders()

		await expect(addReminder(testRequest)).rejects.toThrow('Network error')

		expect(error.value).toBe('Could not add reminder')
		expect(reminders.value).toEqual([])
	})

	it('sets an error when editing a reminder fails', async () => {
		vi.mocked(updateReminder).mockRejectedValue(new Error('Network error'))
		vi.mocked(getReminderErrorMessage).mockReturnValue('Could not edit reminder')

		const {
			error,
			reminders,
			editReminder,
		} = useReminders()

		reminders.value.push(testReminder)

		await expect(editReminder(3, testRequest)).rejects.toThrow('Network error')

		expect(error.value).toBe('Could not edit reminder')
	})

	it('sets an error when updating reminder status fails', async () => {
		vi.mocked(changeStatus).mockRejectedValue(new Error('Network error'))
		vi.mocked(getReminderErrorMessage).mockReturnValue('Could not update reminder status')

		const {
			error,
			reminders,
			updateStatus,
		} = useReminders()

		reminders.value.push(testReminder)

		await updateStatus(statusChangeRequest)

		expect(error.value).toBe('Could not update reminder status')
		expect(reminders.value).toEqual([testReminder])
	})

	it('sets an error when removing a reminder fails', async () => {
		vi.mocked(deleteReminder).mockRejectedValue(new Error('Network error'))
		vi.mocked(getReminderErrorMessage).mockReturnValue('Could not remove reminder')

		const {
			error,
			reminders,
			removeReminder,
		} = useReminders()

		reminders.value.push(testReminder)

		await expect(removeReminder(3)).rejects.toThrow('Network error')

		expect(error.value).toBe('Could not remove reminder')
		expect(reminders.value).toEqual([testReminder])
	})

	it('sets an error when loading reminders by date fails', async () => {
		const date = new Date(2026, 9, 1)

		vi.mocked(getRemindersByDate).mockRejectedValue(new Error('Network error'))
		vi.mocked(getReminderListErrorMessage).mockReturnValue('Could not load reminders by date')

		const {
			error,
			loading,
			loadRemindersByDate,
		} = useReminders()

		await loadRemindersByDate(date)

		expect(error.value).toBe('Could not load reminders by date')
		expect(loading.value).toBe(false)
	})
})
