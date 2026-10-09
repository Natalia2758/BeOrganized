import { ApiError } from "@/common/api/apiError"

/**
 * Converts authentication API failures into user-facing messages.
 * @param error Error returned by the authentication request.
 */
export function getAuthErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            switch (error.code) {
                case 'DUPLICATE_EMAILS':
                    return error.message ?? 'The email already exists.'

                case 'EMAIL_REQUIRED':
                    return error.message ?? 'Email is required.'

                case 'PASSWORD_REQUIRED':
                    return error.message ?? 'Password is required..'

                case 'PASSWORD_TOO_SHORT':
                    return error.message ?? 'Password must be at least 8 characters.'

                default:
                    return 'The request is invalid.'
            }
        
        case 401:
            return 'Please log in.'

        case 403:
            return 'Access denied.'
        
        case 404:
            return 'User not found.'

        case 405:
            return 'Action is not allowed.'

        case 500:
            return 'Something went wrong on the server.'

        default:
            return 'Failed to load user.'

    }
}
