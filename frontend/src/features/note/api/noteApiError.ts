import { ApiError } from "@/common/api/apiError";

/**
 * Maps a note operation failure to a user-facing message.
 * @param error Error returned by the note request.
 */
export function getNoteErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            switch (error.code) {
                case 'NO_NOTE_DATA':
                    return error.message ?? 'Please provide note data.'

                case 'NOTE_CONTENT_REQUIRED':
                    return error.message ?? 'Note content is required.'

                case 'NOTE_DATE_IN_PAST':
                    return error.message ?? 'Date must be in the future.'

                default:
                    return 'The request is invalid.'
            }
        
        case 401:
            return 'Please log in.'

        case 403:
            return 'Access denied.'
        
        case 404:
            return 'Note not found.'

        case 500:
            return 'Something went wrong on the server.'

        default:
            return 'Failed to load note.'

    }
}

/**
 * Maps a note-list loading failure to a user-facing message.
 * @param error Error returned while loading notes.
 */
export function getNoteListErrorMessage(error: unknown): string {
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
            return 'Notes not found'

        case 500:
            return 'Something went wrong on the server.'
            
        default:
            return 'Failed to load notes.'
    }
}