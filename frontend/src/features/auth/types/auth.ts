/** Public profile data returned for the authenticated user. */
export interface User {
    idUser: number
    email: string
    username: string | null
}

/** Credentials and profile fields required to create an account. */
export interface RegisterRequest {
    email: string
    username: string | null
    password: string
}

/** Credentials submitted when signing in. */
export interface LoginRequest {
    email: string
    password: string
}