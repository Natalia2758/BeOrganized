import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import TaskFilters from '../components/TaskFilters.vue'
import { getCategories } from '@/features/category/api/categoryApi'
import type { Category } from '@/features/category/types/category'

vi.mock('@/features/category/api/categoryApi', () => ({
    getCategories: vi.fn(),
    createCategory: vi.fn(),
    deleteCategory: vi.fn()
}))

const categories: Category[] = [
    { idCategory: 12, name: 'Work', color: '#ff0000' }
]

describe('TaskFilters', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        vi.mocked(getCategories).mockResolvedValue(categories)
    })

    it('emits search text, numeric category ID, and boolean completion status', async () => {
        const wrapper = mount(TaskFilters, {
            props: {
                search: '',
                catId: undefined,
                completed: undefined
            }
        })
        await flushPromises()

        await wrapper.get('input').setValue('report')
        await wrapper.findAll('select')[0]!.setValue('12')
        await wrapper.findAll('select')[1]!.setValue('true')

        expect(wrapper.emitted('update:search')).toEqual([['report']])
        expect(wrapper.emitted('update:idCategory')).toEqual([[12]])
        expect(wrapper.emitted('update:isCompleted')).toEqual([[true]])
    })

    it('emits undefined for all completion statuses and resets filters', async () => {
        const wrapper = mount(TaskFilters, {
            props: {
                search: '',
                catId: undefined,
                completed: undefined
            }
        })

        await wrapper.findAll('select')[1]!.setValue('undefined')
        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('update:isCompleted')).toEqual([[undefined]])
        expect(wrapper.emitted('reset')).toHaveLength(1)
    })
})
