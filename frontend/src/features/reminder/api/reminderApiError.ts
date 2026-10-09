import { ApiError } from "@/common/api/apiError";

/**
 * Maps a reminder operation failure to a user-facing message.
 * @param error Error returned by the reminder request.
 */
export function getReminderErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            switch (error.code) {
                case 'NO_REMINDER_DATA':
                    return error.message ?? 'Please provide reminder data.'

                case 'REMINDER_TITLE_REQUIRED':
                    return error.message ?? 'Reminder content is required.'

                case 'REMINDER_TYPE_REQUIRED':
                    return error.message ?? 'Reminder type is required.'

                case 'REMINDER_TIMING_REQUIRED':
                    return error.message ?? 'Reminder timing option is required.'

                case 'OBJECT_ID_REQUIRED':
                    return error.message ?? 'Choose an event or task or change type to "Other".'

                case 'REMINDER_DATE_TIME_REQUIRED': 
                    return error.message ?? 'Please choose date and time.'
                case 'PAST_EVENT_DATE': 
                    return error.message ?? 'Date of the chosen event is in the past.'
                case 'PAST_TASK_DATE':
                    return error.message ?? 'Date of the chosen task is in the past.'
                case 'DATE_AFTER_EVENT':
                    return error.message ?? 'Reminder date must be before an event.'
                case 'DATE_AFTER_TASK':
                    return error.message ?? 'Reminder date must be before a task.'
                case 'OBJECT_DATE_TIME_REQUIRED':
                    return error.message ?? 'A future target date and time are required.'

                default:
                    return 'The request is invalid.'
            }
        
        case 401:
            return 'Please log in.'

        case 403:
            return 'Access denied. You do not have permission for this action.'
        
        case 404:
            return 'Reminder not found.'

        case 500:
            return 'Something went wrong on the server.'

        default:
            return 'Failed to load reminder.'

    }
}

/**
 * Maps a reminder-list loading failure to a user-facing message.
 * @param error Error returned while loading reminders.
 */
export function getReminderListErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            return 'The request is invalid.'
           
        case 401:
            return 'Session expiered. Please log in again.'

        case 403:
            return 'Access denied. You do not have permission for this action.'
        
        case 404:
            return 'Reminders not found'

        case 500:
            return 'Something went wrong on the server.'
            
        default:
            return 'Failed to load reminders.'
    }
}