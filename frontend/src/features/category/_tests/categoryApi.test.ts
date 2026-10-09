import { beforeEach, describe, expect, it, vi } from 'vitest'
import http from '@/common/api/http'
import { ApiError, toApiError } from '@/common/api/apiError'
import {
	createCategory,
	deleteCategory,
	getCategories,
	getCategory,
	updateCategory,
} from '@/features/category/api/categoryApi'
import type {
	Category,
	CategoryRequest,
} from '@/features/category/types/category'

vi.mock('@/common/api/http', () => ({
	default: {
		get: vi.fn(),
		post: vi.fn(),
		put: vi.fn(),
		patch: vi.fn(),
		delete: vi.fn(),
	},
}))

vi.mock('@/common/api/apiError', () => ({
	toApiError: vi.fn(),
}))

const testCategory: Category = {
	idCategory: 1,
	name: 'Work',
	color: '#ff0000',
}

const testCategoryRequest: CategoryRequest = {
	name: 'Work',
	color: '#ff0000',
}

beforeEach(() => {
	vi.clearAllMocks()
})

describe('categoryApi', () => {
	it('gets categories', async () => {
		const categories = [testCategory]

		vi.mocked(http.get).mockResolvedValue({
			data: categories,
		})

		const result = await getCategories()

		expect(http.get).toHaveBeenCalledWith('/categories')
		expect(result).toEqual(categories)
	})

	it('gets one category by id', async () => {
		const category = testCategory

		vi.mocked(http.get).mockResolvedValue({
			data: category,
		})

		const result = await getCategory(category.idCategory)

		expect(http.get).toHaveBeenCalledWith(`/categories/${category.idCategory}`)
		expect(result).toEqual(category)
	})

	it('creates a category', async () => {
		const request = testCategoryRequest
		const category = testCategory

		vi.mocked(http.post).mockResolvedValue({
			data: category,
		})

		const result = await createCategory(request)

		expect(http.post).toHaveBeenCalledWith('/categories/create', request)
		expect(result).toEqual(category)
	})

	it('updates a category', async () => {
		const request = testCategoryRequest
		const updated = { ...testCategory, name: 'Updated work' }

		vi.mocked(http.put).mockResolvedValue({
			data: updated,
		})

		const result = await updateCategory(updated.idCategory, request)

		expect(http.put).toHaveBeenCalledWith(`/categories/${updated.idCategory}`, request)
		expect(result).toEqual(updated)
	})

	it('deletes a category', async () => {
		vi.mocked(http.delete).mockResolvedValue({})

		await deleteCategory(1)

		expect(http.delete).toHaveBeenCalledWith('/categories/1')
	})

	it('converts API errors', async () => {
		const httpError = new Error('network error')
		const apiError = new ApiError('API error', 400)

		vi.mocked(http.get).mockRejectedValue(httpError)
		vi.mocked(toApiError).mockReturnValue(apiError)

		await expect(getCategory(1)).rejects.toBe(apiError)
		expect(toApiError).toHaveBeenCalledWith(httpError)
	})
})
