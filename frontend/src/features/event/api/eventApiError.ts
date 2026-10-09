import { ApiError } from "@/common/api/apiError";

/**
 * Maps an event operation failure to a user-facing message.
 * @param error Error returned by the event request.
 */
export function getEventErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            switch (error.code) {
                case 'NO_EVENT_DATA':
                    return error.message ?? 'Please provide event data.'

                case 'EVENT_TITLE_REQUIRED':
                    return error.message ?? 'Event title is required.'

                case 'EVENT_DATE_REQUIRED':
                    return error.message ?? 'Date is required.'

                case 'EVENT_DATE_IN_PAST':
                    return error.message ?? 'Date must be in the presence or future.'

                case 'EVENT_START_AFTER_END':
                    return error.message ?? 'Please choose the end date after or at the start date of event.'

                case 'EVENT_TIME_REQUIRED':
                    return error.message ?? 'Please choose start and end time.'

                case 'EVENT_TIME_START_AFTER_END':
                    return error.message ?? 'Start time must preceed end time.'

                case 'EVENT_TIME_IN_PAST':
                    return error.message ?? 'Time must be in the presence or future.'

                case 'EVENT_REPETITION_FREQUENCY_REQUIRED':
                    return error.message ?? 'Please choose the repetition frequency.'

                default:
                    return 'The request is invalid.'
            }
        
        case 401:
            return 'Please log in.'

        case 403:
            return 'Access denied.'
        
        case 404:
            return 'Event not found.'

        case 500:
            return 'Something went wrong on the server.'

        default:
            return 'Failed to load event.'

    }
}

/**
 * Maps an event-list loading failure to a user-facing message.
 * @param error Error returned while loading events.
 */
export function getEventListErrorMessage(error: unknown): string {
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
            return 'Events not found'

        case 500:
            return 'Something went wrong on the server.'
            
        default:
            return 'Failed to load events.'
    }
}