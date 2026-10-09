import axios from 'axios'
import type { ApiErrorResponse } from './ApiErrorInterface'

/**
 * <b>ApiError>
 * Class that extends an Error interface and represents a type of an error sent from the server. It transfers information about the  error message, status, and code.
 */
export class ApiError extends Error {

    // HTTP status code (e.g., 401, 200, 500)
    status: number
    // "?" - optional
    code?: string

    constructor(
        message: string,
        status: number,
        code?: string
    ) {
        super(message)

        this.name = 'ApiError'
        this.status = status
        this.code = code
    }
}

/**
 * Transforms errors sent from the backend to the ApiError objects.
 * 
 * @param error an error that is to be converted into an ApiError object
 * @returns     an ApiError object with an error information
 */
export function toApiError(error: unknown): ApiError {

    if (!axios.isAxiosError(error)) {
        return new ApiError(
            'Network error',
            0
        )
    }
    const status = error.response?.status ?? 0
    const data = error.response?.data as | ApiErrorResponse | string | undefined
    const message = typeof data === 'string' ? data : data?.message
    const code = typeof data === 'string' ? undefined : data?.code

    return new ApiError(
        message ?? 'An unexpected error occurred.',
        status,
        code
    )
}