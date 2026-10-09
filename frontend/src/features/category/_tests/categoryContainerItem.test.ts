import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import CategoryContainerItem from '../components/CategoryContainerItem.vue'
import type { Category } from '../types/category'

describe('CategoryContainerItem', () => {
    it('renders the category and emits its ID when delete is clicked', async () => {
        const category: Category = {
            idCategory: 42,
            name: 'Work',
            color: '#ff0000'
        }
        const wrapper = mount(CategoryContainerItem, {
            props: { category }
        })

        expect(wrapper.text()).toContain('Work')

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('delete')).toEqual([[42]])
    })
})
