import http from "@/common/api/http";
import { type Reminder, type ReminderCreateRequest, type ReminderStatusChangeRequest } from "../types/reminder";
import { toApiError } from "@/common/api/apiError";


/** Retrieves all reminders belonging to the current user. */
export async function getReminders() {
    try {
        const response = await http.get<Reminder[]>('/reminders')
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves one reminder by ID.
 * @param idReminder ID of the reminder to retrieve.
 */
export async function getReminder(idReminder: number): Promise<Reminder> {
    try {
        const response = await http.get<Reminder>(`/reminders/${idReminder}`)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates a reminder and returns every reminder created for repetitions.
 * @param request Reminder details to create.
 */
export async function createReminder(request: ReminderCreateRequest): Promise<Reminder[]> {

    try {
        const response = await http.post<Reminder[]>('/reminders/create', request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates an existing reminder.
 * @param idReminder ID of the reminder to update.
 * @param request Updated reminder details.
 */
export async function updateReminder(idReminder: number, request: ReminderCreateRequest): Promise<Reminder> {
    try {
        const response = await http.put<Reminder>(`/reminders/${idReminder}`, request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Changes the status of a reminder.
 * @param request Reminder ID and new status.
 */
export async function changeStatus(request: ReminderStatusChangeRequest): Promise<Reminder> {
    try {
        const response = await http.patch<Reminder>(`/reminders/${request.idReminder}/status`, request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Deletes a reminder by ID.
 * @param idReminder ID of the reminder to delete.
 */
export async function deleteReminder(idReminder: number): Promise<void> {
    try {
        await http.delete(`/reminders/${idReminder}`)
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves reminders scheduled for a local calendar date.
 * @param date Local calendar date to query.
 */
export async function getRemindersByDate(date: Date): Promise<Reminder[]> {
    try {
        const response = await http.get<Reminder[]>('/reminders/by-date', {
            params: {
                year: date.getFullYear(),
                month: date.getMonth() + 1,
                day: date.getDate()
            }
        })
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/** Retrieves reminders already sent and available for notification history. */
export async function getSentReminders(): Promise<Reminder[]> {
    try {
        const response = await http.get<Reminder[]>('/reminders/sent')

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
} 