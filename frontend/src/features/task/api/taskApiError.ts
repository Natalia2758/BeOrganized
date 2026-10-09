import { ApiError } from "@/common/api/apiError";

/**
 * Maps a task operation failure to a user-facing message.
 * @param error Error returned by the task request.
 */
export function getTaskErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            switch (error.code) {
                case 'NO_TASK_DATA':
                    return error.message ?? 'Please provide task data.'

                case 'TASK_TITLE_REQUIRED':
                    return error.message ?? 'Task title is required.'

                case 'TASK_DATE_IN_PAST':
                    return error.message ?? 'Date must be in the future.'

                case 'TASK_TIME_REQUIRED':
                    return error.message ?? 'Please provide start and end time.'

                case 'TASK_REPETITION_FREQUENCY_REQUIRED':
                    return error.message ?? 'Please choose the repetition frequency.'
                case 'VALIDATION_ERROR':
                    return error.message ?? 'The data is incorrect.'

                default:
                    return 'The request is invalid.'
            }
        
        case 401:
            return 'Please log in.'

        case 403:
            return 'Access denied.'
        
        case 404:
            return 'Task not found.'

        case 409:
            return 'The selected time block is already occupied.'

        case 500:
            return 'Something went wrong on the server.'

        default:
            return 'Failed to load task.'

    }
}

/**
 * Maps a task-list loading failure to a user-facing message.
 * @param error Error returned while loading tasks.
 */
export function getTaskListErrorMessage(error: unknown): string {
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
            return 'Tasks not found'

        case 500:
            return 'Something went wrong on the server.'
            
        default:
            return 'Failed to load tasks.'
    }
}