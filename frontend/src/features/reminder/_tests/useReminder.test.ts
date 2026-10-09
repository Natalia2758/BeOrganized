import { describe, expect, it, vi } from "vitest";
import { changeStatus, getReminder } from "../api/reminderApi";
import { ReminderStatus, ReminderType, Timing, type Reminder } from "../types/reminder";
import { useReminder } from "../composables/useReminder";
import { ApiError } from "@/common/api/apiError";
import { getReminderErrorMessage } from "../api/reminderApiError";

vi.mock('@/features/reminder/api/reminderApi', () => ({
	getReminder: vi.fn(),
	changeStatus: vi.fn()
}))

vi.mock('@/features/reminder/api/reminderApiError', () => ({
  getReminderErrorMessage: vi.fn(),
}))

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

describe('useReminder', () => {
	it('loads a reminder', async () => {

		const reminder = testReminder

		vi.mocked(getReminder).mockResolvedValue(reminder)

		const {
			reminder: reminderState,
			loading,
			error,
			loadReminder
		} = useReminder()

		await loadReminder(reminder.idReminder)

		expect(getReminder).toHaveBeenCalledWith(reminder.idReminder)
		expect(reminderState.value).toEqual(reminder)
		expect(loading.value).toBe(false)
		expect(error.value).toBeNull()
	})

	it('sets an error when failed', async () => {
		const apiError = new ApiError('Request failed', 403)

		vi.mocked(getReminder).mockRejectedValue(apiError)
		vi.mocked(getReminderErrorMessage).mockReturnValue('Access denied.')

		const {
			loading,
			error,
			loadReminder
		} = useReminder()

		await loadReminder(1)

		expect(error.value).toBe('Access denied.')
		expect(loading.value).toBe(false)
	})

	it('marks a reminder as opened', async () => {
		const reminder = testReminder
		const updatedReminder = { ...testReminder, status: ReminderStatus.OPENED as const }

		vi.mocked(changeStatus).mockResolvedValue(updatedReminder)

		const {
			reminder: reminderState,
			markAsOpened,
		} = useReminder()

		reminderState.value = reminder

		await markAsOpened(reminder.idReminder)

		expect(changeStatus).toHaveBeenCalledWith({
			idReminder: reminder.idReminder,
			status: 'OPENED',
		})
		expect(reminderState.value).toEqual(updatedReminder)
	})

	it('sets an error when mark as opened fails', async () => {
		const apiError = new ApiError('Request failed', 403)

		vi.mocked(changeStatus).mockRejectedValue(apiError)
		vi.mocked(getReminderErrorMessage).mockReturnValue('Access denied.')

		const {
			reminder: reminderState,
			error,
			markAsOpened,
		} = useReminder()

		reminderState.value = testReminder

		await markAsOpened(testReminder.idReminder)

		expect(error.value).toBe('Access denied.')
		expect(reminderState.value).toEqual(testReminder)
	})
})
