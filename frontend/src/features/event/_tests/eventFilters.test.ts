import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import EventFilters from '../components/EventFilters.vue'
import { getCategories } from '@/features/category/api/categoryApi'
import type { Category } from '@/features/category/types/category'

vi.mock('@/features/category/api/categoryApi', () => ({
    getCategories: vi.fn(),
    createCategory: vi.fn(),
    deleteCategory: vi.fn()
}))

const categories: Category[] = [
    { idCategory: 5, name: 'Personal', color: '#00ff00' }
]

describe('EventFilters', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        vi.mocked(getCategories).mockResolvedValue(categories)
    })

    it('emits search text and numeric category ID', async () => {
        const wrapper = mount(EventFilters, {
            props: {
                search: '',
                catId: undefined
            }
        })
        await flushPromises()

        await wrapper.get('input').setValue('appointment')
        await wrapper.get('select').setValue('5')

        expect(wrapper.emitted('update:search')).toEqual([['appointment']])
        expect(wrapper.emitted('update:idCategory')).toEqual([[5]])
    })

    it('emits reset when reset is clicked', async () => {
        const wrapper = mount(EventFilters, {
            props: {
                search: 'old search',
                catId: 5
            }
        })

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('reset')).toHaveLength(1)
    })
})
