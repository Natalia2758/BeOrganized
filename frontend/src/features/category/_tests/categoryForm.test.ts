import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import CategoryForm from '../components/CategoryForm.vue'
import { colorPalette } from '../types/color'

describe('CategoryForm', () => {
    it('requires a category name', async () => {
        const wrapper = mount(CategoryForm)

        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Name is required')
        expect(wrapper.emitted('create')).toBeUndefined()
    })

    it('emits the name and selected color', async () => {
        const wrapper = mount(CategoryForm)

        await wrapper.get('#name').setValue('Study')
        await wrapper.get('.color-picker .btn-subtle').trigger('click')
        await wrapper.get('button[title="Purple"]').trigger('click')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('create')).toEqual([[
            { name: 'Study', color: colorPalette.purple.color }
        ]])
    })

    it('emits cancel without creating a category', async () => {
        const wrapper = mount(CategoryForm)

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('cancel')).toHaveLength(1)
        expect(wrapper.emitted('create')).toBeUndefined()
    })
})
