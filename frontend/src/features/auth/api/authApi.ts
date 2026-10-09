import http from "@/common/api/http";
import { toApiError } from "@/common/api/apiError";
import type { LoginRequest, RegisterRequest, User } from "../types/auth";

/**
 * Registers an account and converts transport failures to application API errors.
 * @param request Registration credentials and account details.
 */
export async function register(request: RegisterRequest): Promise<void> {
    
    try {
        await http.post('/auth/register', request)
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Authenticates with form-encoded credentials and returns the signed-in user.
 * @param request Login credentials.
 */
export async function login(request: LoginRequest): Promise<User> {
    
    try {
        const params = new URLSearchParams()

        params.append("email", request.email)
        params.append("password", request.password)

        const user = await http.post<User>(
            '/auth/login',
            params,
            {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            }
        )
        return user.data
        
    } catch (error) {
        throw toApiError(error)
    }
    
}

/** Ends the current authenticated session. */
export async function logout(): Promise<void> {
    try {
        await http.post('/auth/logout', {})
    } catch (error) {
        throw toApiError(error)
    }
    
}

/** Retrieves the current user's profile. */
export async function getUser(): Promise<User> {
    try {
        const response = await http.get('/auth/user')
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/** Deletes the authenticated user's account and returns the HTTP status. */
export async function deleteAccount(): Promise<number> {
    try {
        const response = await http.delete('auth/delete-account')
        return response.status
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Updates the authenticated user's username and returns the updated profile.
 * @param username New username for the authenticated account.
 */
export async function updateUsername(username: string): Promise<User> {
    try {
        const response = await http.patch('auth/user/update-username', username)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}
