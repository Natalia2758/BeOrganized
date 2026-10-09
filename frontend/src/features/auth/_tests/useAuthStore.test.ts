
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { deleteAccount, getUser, login, logout, register, updateUsername } from "../api/authApi";
import type { LoginRequest, RegisterRequest, User } from "../types/auth";
import { useAuthStore } from "../stores/useAuthStore";
import { ApiError } from "@/common/api/apiError";
import { getAuthErrorMessage } from "../api/authApiError";


vi.mock('@/features/auth/api/authApi', () => ({
    register: vi.fn(),
    login: vi.fn(),
    getUser: vi.fn(),
    logout: vi.fn(),
    deleteAccount: vi.fn(),
    updateUsername: vi.fn()
}))

vi.mock('@/features/auth/api/authApiError', () => ({
    getAuthErrorMessage: vi.fn()
}))

const testUser: User = {
    idUser: 2,
    username: 'Username',
    email: 'example@email.com'
}

const testRegisterRequest: RegisterRequest = {
    username: 'Username',
    email: 'example@email.com',
    password: 'password123'
}

const testLoginRequest: LoginRequest = {
    email: 'example@email.com',
    password: 'password123'
}

describe('useAuthStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
    })

    it('registers user', async () => {
        const request = testRegisterRequest
        const store = useAuthStore()

        vi.mocked(register).mockResolvedValue(undefined)

        await store.addUser(request)        

        expect(register).toHaveBeenCalledWith(request)
        expect(store.error).toBeNull()
    })

    it('authenticates user', async () => {
        const request = testLoginRequest
        const user = testUser
        const store = useAuthStore()

        vi.mocked(login).mockResolvedValue(user)

        const result = await store.authenticateUser(request)

        expect(result).toEqual(store.user)
        expect(login).toHaveBeenCalledWith(request)
        expect(store.error).toBeNull()
        expect(store.isAuthenticated).toBeTruthy()
        expect(store.username).toBe('Username')
       
    })

    it('loads current user', async () => {
        const user = testUser
        const store = useAuthStore()

        vi.mocked(getUser).mockResolvedValue(user)

        const result = await store.loadCurrentUser()

        expect(result).toEqual(user)
        expect(getUser).toHaveBeenCalled()
        expect(store.error).toBeNull()
        expect(store.isAuthenticated).toBeTruthy()
        expect(store.username).toBe('Username')

    })

    it('sets an error when authentication fails', async () => {
        const error = new ApiError('API error', 0)

        vi.mocked(login).mockRejectedValue(error)
        vi.mocked(getAuthErrorMessage).mockReturnValue('Could not authenticate user')

        const store = useAuthStore()
        const request = testLoginRequest

        await expect(store.authenticateUser(request)).rejects.toThrow(error)

        expect(getAuthErrorMessage).toHaveBeenCalled()
        expect(store.error).toBe('Could not authenticate user')
        expect(store.isAuthenticated).toBeFalsy()
        expect(store.username).toBe('')


    })

    it('logs out user', async () => {
        vi.mocked(logout).mockResolvedValue(undefined)

        const store = useAuthStore()
        store.user = testUser

        await store.logoutUser()

        expect(logout).toHaveBeenCalled()
        expect(store.user).toBeNull()
    })

    it('updates username', async () => {
        const username = 'Updated username'
        const updated: User = {
            idUser: 1,
            email: 'example@email.com',
            username: 'Updated username'
        }
        const store = useAuthStore()
        store.user = testUser

        vi.mocked(updateUsername).mockResolvedValue(updated)

        const result = await store.editUsername(username)

        expect(updateUsername).toHaveBeenCalledWith(username)
        expect(result).toEqual(updated)
        expect(store.error).toBeNull()
        expect(store.username).toBe(username)
    })
})