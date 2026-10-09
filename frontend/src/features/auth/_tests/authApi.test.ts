import { beforeEach, describe, expect, it, vi } from 'vitest'
import http from '@/common/api/http'
import { ApiError, toApiError } from '@/common/api/apiError'
import { deleteAccount, getUser, login, logout, register, updateUsername } from '../api/authApi'
import type { LoginRequest, RegisterRequest, User } from '../types/auth'

vi.mock('@/common/api/http', () => ({
	default: {
		get: vi.fn(),
		post: vi.fn(),
		put: vi.fn(),
		patch: vi.fn(),
		delete: vi.fn(),
	},
}))

vi.mock('@/common/api/apiError', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@/common/api/apiError')>()

    return {
        ...actual,
        toApiError: vi.fn()
    }
})

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

beforeEach(() => {
	vi.clearAllMocks()
})

describe('authApi', () => {
    it('sends user registartion data', async () => {
        const request = testRegisterRequest
        vi.mocked(http.post).mockResolvedValue({})

        await register(request)

        expect(http.post).toHaveBeenCalledWith('/auth/register', request)
    })

    it('logs in user', async () => {
        const request = testLoginRequest
        const user = testUser

        vi.mocked(http.post).mockResolvedValue({
            data: user
        })

        const result = await login(request)

        expect(result).toEqual(user)
        expect(http.post).toHaveBeenCalledWith(
            '/auth/login',
            expect.any(URLSearchParams),
            {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }
        )
    })

    it('logs out user', async () => {
        vi.mocked(http.post).mockResolvedValue({})

        await logout()

        expect(http.post).toHaveBeenCalledWith('/auth/logout', {})
    })

    it('gets user', async () => {
        const user = testUser

        vi.mocked(http.get).mockResolvedValue({
            data: user
        })

        const result = await getUser()

        expect(http.get).toHaveBeenCalledWith('/auth/user')
        expect(result).toEqual(user)
    })

    it('deletes account', async () => {
        vi.mocked(http.delete).mockResolvedValue({
            status: 204
        })

        const result = await deleteAccount()

        expect(http.delete).toHaveBeenCalledWith('auth/delete-account')
        expect(result).toBe(204)
    })

    it('updates username', async () => {
        const user = testUser

        vi.mocked(http.patch).mockResolvedValue({
            data: user
        })

        const result = await updateUsername('Updated username')

        expect(http.patch).toHaveBeenCalledWith('auth/user/update-username', 'Updated username')
        expect(result).toEqual(user)
    })

    it('converts API errors', async () => {
        const httpError = new Error('network error')
        const apiError = new ApiError('API error', 1)

        vi.mocked(http.post).mockRejectedValue(httpError)
        vi.mocked(toApiError).mockReturnValue(apiError)

        await expect(login(testLoginRequest)).rejects.toBe(apiError)
        expect(toApiError).toHaveBeenCalledWith(httpError)
    })
})