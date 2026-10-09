import { ApiError } from "@/common/api/apiError";

/**
 * Maps a category operation failure to a user-facing message.
 * @param error Error returned by the category request.
 */
export function getCategoryErrorMessage(error: unknown): string {
    if (!(error instanceof ApiError)) {
        return 'An unexpected error occurred.'
    }

    switch (error.status) {
        case 400: 
            switch (error.code) {
                case 'NO_CATEGORY_DATA':
                    return error.message ?? 'Please provide category data.'

                case 'CATEGORY_NAME_REQUIRED':
                    return error.message ?? 'Category name is required.'

                case 'CATEGORY_COLOR_REQUIRED':
                    return error.message ?? 'Category color is required.'

                default:
                    return 'The request is invalid.'
            }
        
        case 401:
            return 'Please log in.'

        case 403:
            return 'Access denied.'
        
        case 404:
            return 'Category not found.'

        case 409:
            return error.message ?? 'Category with this name already exists.'

        case 500:
            return 'Something went wrong on the server.'

        default:
            return 'Failed to load category.'

    }
}

/**
 * Maps a category-list loading failure to a user-facing message.
 * @param error Error returned while loading categories.
 */
export function getCategoryListErrorMessage(error: unknown): string {
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
            return 'Categories not found'

        case 500:
            return 'Something went wrong on the server.'
            
        default:
            return 'Failed to load categories.'
    }
}