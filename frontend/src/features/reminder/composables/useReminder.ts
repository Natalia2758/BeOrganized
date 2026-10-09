import { ref } from "vue";
import { ReminderStatus, type Reminder } from "../types/reminder";
import { changeStatus, getReminder } from "../api/reminderApi";
import { getReminderErrorMessage } from "../api/reminderApiError";


/** Manages the state and actions for a single reminder. */
export function useReminder() {

    const reminder = ref<Reminder | null>(null)
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Fetches reminder details and tracks loading and API errors.
     * @param idReminder ID of the reminder to load.
     */
    async function loadReminder(idReminder: number) {
        loading.value = true
        error.value = null

        try {
            reminder.value = await getReminder(idReminder)
        } catch (e) {
            
            error.value = getReminderErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /**
     * Marks the selected reminder as opened.
     * @param idReminder ID of the reminder to mark as opened.
     */
    async function markAsOpened(idReminder: number) {
        error.value = null

        try {
            reminder.value = await changeStatus({ idReminder: idReminder, status: ReminderStatus.OPENED })
            
        } catch (e) {
            error.value = getReminderErrorMessage(e)
            
        } 
    }

    return {
        reminder,
        loading,
        error,
        loadReminder,
        markAsOpened
    }
}