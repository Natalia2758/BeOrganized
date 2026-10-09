import { ref } from 'vue';
import type { Reminder, ReminderCreateRequest, ReminderStatusChangeRequest } from '../types/reminder';
import { changeStatus, createReminder, deleteReminder, getReminders, getRemindersByDate, updateReminder } from '../api/reminderApi';
import { getReminderErrorMessage, getReminderListErrorMessage } from '../api/reminderApiError';

/** Manages reminder collections and their loading and error state. */
export function useReminders() {

    const reminders = ref<Reminder[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    /** Loads all reminders for the current user. */
    async function loadReminders() {
        loading.value = true
        error.value = null

        try {
            const result = await getReminders()
            reminders.value = result ?? []
        } catch (e) {
            error.value = getReminderListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /**
     * Creates reminders and adds each returned repetition to the collection.
     * @param request Reminder details to create.
     */
    async function addReminder(request: ReminderCreateRequest) {
        error.value = null;

        try {
            const createdReminders = await createReminder(request)

            if (createdReminders.length === 1 && createdReminders[0]) {
                reminders.value.push(createdReminders[0])
            } else {
                createdReminders.forEach(reminder => {
                    reminders.value.push(reminder)
                })
            }

        } catch (e) {
            error.value = getReminderErrorMessage(e)
            throw e
        }
    } 

    /**
     * Saves reminder changes and replaces the matching collection entry.
     * @param idReminder ID of the reminder to update.
     * @param request Updated reminder details.
     */
    async function editReminder(idReminder: number, request: ReminderCreateRequest) {
        error.value = null

        try {
            const updatedReminder = await updateReminder(idReminder, request)

            const index = reminders.value.findIndex(reminder => reminder.idReminder === idReminder)

            if (index !== -1) {
                reminders.value[index] = updatedReminder
            }
            return updatedReminder
        } catch (e) {
            error.value = getReminderErrorMessage(e)
            throw e
        }
    }

    /**
     * Updates a reminder's status and replaces its local collection entry.
     * @param request Reminder ID and new status.
     */
    async function updateStatus(request: ReminderStatusChangeRequest) {
        error.value = null;

        const reminder = reminders.value.find(reminder => reminder.idReminder === request.idReminder)
        if (!reminder) return

        try {
            const changedReminder = await changeStatus(request)

            const index = reminders.value.findIndex(reminder => reminder.idReminder === request.idReminder)

            if (index !== -1) {
                reminders.value[index] = changedReminder
            }
            return changedReminder
        } catch (e) {
            error.value = getReminderErrorMessage(e)
        }
    }

    /**
     * Deletes a reminder and removes it from the collection.
     * @param idReminder ID of the reminder to remove.
     */
    async function removeReminder(idReminder: number) {
        error.value = null

        try {
            await deleteReminder(idReminder)

            reminders.value = reminders.value.filter(
                reminder => reminder.idReminder !== idReminder
            )
        } catch (e) {
            error.value = getReminderErrorMessage(e)
            throw e
        }
    }

    /**
     * Loads reminders scheduled on the given date.
     * @param date Date whose reminders should be loaded.
     */
    async function loadRemindersByDate(date: Date) {
        error.value = null
        loading.value = true

        try {
            reminders.value = await getRemindersByDate(date)
        } catch (e) {
            error.value = getReminderListErrorMessage(e)
            
        } finally {
            loading.value = false
        }
    }

    return {
        reminders,
        loading,
        error,
        loadReminders,
        addReminder,
        editReminder,
        updateStatus,
        removeReminder,
        loadRemindersByDate
    }


}