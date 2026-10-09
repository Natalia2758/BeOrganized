import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { LoginRequest, RegisterRequest, User } from "../types/auth";

import { deleteAccount, getUser, login, logout, register, updateUsername } from "../api/authApi";
import { getAuthErrorMessage } from "../api/authApiError";

/** Holds the signed-in user state and coordinates authentication API actions. */
export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const error = ref<string | null>(null)

    const isAuthenticated = computed(() => !!user.value)
    const username = computed(() => user.value?.username ?? '')

    /** Registers a user and stores a readable error before rethrowing failures. */
    async function addUser(request: RegisterRequest): Promise<void> {
        error.value = null;

        try {
            await register(request)

        } catch (e) {
            error.value = getAuthErrorMessage(e)
            throw e
        }
    } 

    /** Authenticates a user and updates the current-user state. */
    async function authenticateUser(request: LoginRequest): Promise<void> {
            error.value = null;

        try {
            const authenticatedUser = await login(request)
            user.value = authenticatedUser

        } catch (e) {
            error.value = getAuthErrorMessage(e)
            throw e
        }
    }

    /** Restores the current user from the active session, clearing state if unavailable. */
    async function loadCurrentUser(): Promise<User | null> {
        try {
            const fetchedUser = await getUser()
            user.value = fetchedUser
            return fetchedUser
        } catch (e) {

            error.value = getAuthErrorMessage(e)
            user.value = null
            return null
        }
    }

    /** Ends the session and clears the current user after a successful logout. */
    async function logoutUser(): Promise<void> {
        error.value = null

        try {
            await logout()
            user.value = null
        } catch (e) {
            error.value = getAuthErrorMessage(e) + 'Failed to log out. Please try again'
            throw e
        }
    }

    /** Deletes the current user's account. */
    async function removeAccount(): Promise<void> {
        error.value = null

        try {
            await deleteAccount()
            
        } catch (e) {
            error.value = getAuthErrorMessage(e) + 'Failed to delete account. Please try again'
            throw e
        }
    }

    /** Updates the username and replaces the stored profile with the API response. */
    async function editUsername(username: string): Promise<void> {
        error.value = null

        try {
            const updated = await updateUsername(username)
            user.value = updated
        } catch (e) {
            error.value = getAuthErrorMessage(e) + 'Failed to update username. Please try again'
            throw e
        }
    }

    return {
        user,
        error,
        username,
        isAuthenticated,
        authenticateUser,
        addUser,
        loadCurrentUser,
        logoutUser,
        removeAccount,
        editUsername
    }
})