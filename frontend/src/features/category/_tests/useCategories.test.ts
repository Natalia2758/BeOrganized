import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
	createCategory,
	deleteCategory,
	getCategories,
} from '@/features/category/api/categoryApi'
import {
	getCategoryErrorMessage,
	getCategoryListErrorMessage,
} from '@/features/category/api/categoryApiError'
import { useCategories } from '../composables/useCategories'
import type { Category, CategoryRequest } from '../types/category'

vi.mock('@/features/category/api/categoryApi', () => ({
	getCategories: vi.fn(),
	createCategory: vi.fn(),
	deleteCategory: vi.fn(),
}))

vi.mock('@/features/category/api/categoryApiError', () => ({
	getCategoryErrorMessage: vi.fn(),
	getCategoryListErrorMessage: vi.fn(),
}))

const testCategories: Category[] = [
	{
		idCategory: 1,
		name: 'Work',
		color: '#ff0000',
	},
	{
		idCategory: 2,
		name: 'Home',
		color: '#00ff00',
	},
]

const testCategory: Category = {
	idCategory: 3,
	name: 'Study',
	color: '#0000ff',
}

const testRequest: CategoryRequest = {
	name: 'Study',
	color: '#0000ff',
}

beforeEach(() => {
	vi.clearAllMocks()
})

describe('useCategories', () => {
	it('loads categories', async () => {
		vi.mocked(getCategories).mockResolvedValue(testCategories)

		const {
			categories,
			loading,
			error,
			loadCategories,
		} = useCategories()

		await loadCategories()

		expect(getCategories).toHaveBeenCalled()
		expect(categories.value).toEqual(testCategories)
		expect(loading.value).toBe(false)
		expect(error.value).toBeNull()
	})

	it('adds a category', async () => {
		vi.mocked(createCategory).mockResolvedValue(testCategory)

		const {
			categories,
			addCategory,
		} = useCategories()

		await addCategory(testRequest)

		expect(createCategory).toHaveBeenCalledWith(testRequest)
		expect(categories.value).toEqual([testCategory])
	})

	it('removes a category', async () => {
		vi.mocked(deleteCategory).mockResolvedValue(undefined)

		const {
			categories,
			removeCategory,
		} = useCategories()

		categories.value = testCategories

		await removeCategory(1)

		expect(deleteCategory).toHaveBeenCalledWith(1)
		expect(categories.value).toEqual([testCategories[1]])
	})

	it('sets an error when loading categories fails', async () => {
		vi.mocked(getCategories).mockRejectedValue(new Error('Network error'))
		vi.mocked(getCategoryListErrorMessage).mockReturnValue('Could not load categories')

		const {
			error,
			loading,
			loadCategories,
		} = useCategories()

		await loadCategories()

		expect(getCategoryListErrorMessage).toHaveBeenCalled()
		expect(error.value).toBe('Could not load categories')
		expect(loading.value).toBe(false)
	})

	it('sets an error when adding a category fails', async () => {
		const request = testRequest

		vi.mocked(createCategory).mockRejectedValue(new Error('Network error'))
		vi.mocked(getCategoryErrorMessage).mockReturnValue('Could not add category')

		const {
			error,
			categories,
			addCategory,
		} = useCategories()

		await expect(addCategory(request)).rejects.toThrow('Network error')

		expect(getCategoryErrorMessage).toHaveBeenCalled()
		expect(error.value).toBe('Could not add category')
		expect(categories.value).toEqual([])
	})

	it('sets an error when removing a category fails', async () => {
		vi.mocked(deleteCategory).mockRejectedValue(new Error('Network error'))
		vi.mocked(getCategoryErrorMessage).mockReturnValue('Could not remove category')

		const {
			error,
			categories,
			removeCategory,
		} = useCategories()

		categories.value = testCategories

		await removeCategory(1)

		expect(getCategoryErrorMessage).toHaveBeenCalled()
		expect(error.value).toBe('Could not remove category')
		expect(categories.value).toEqual(testCategories)
	})
})
