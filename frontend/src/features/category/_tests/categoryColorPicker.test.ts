import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import CategoryColorPicker from '../components/CategoryColorPicker.vue'
import { colorPalette } from '../types/color'

describe('CategoryColorPicker', () => {
    it('opens the palette, emits the selected color, and closes', async () => {
        const wrapper = mount(CategoryColorPicker, {
            props: { modelValue: colorPalette.blue.color }
        })

        expect(wrapper.find('.color-picker__popover').exists()).toBe(false)

        await wrapper.get('.btn-subtle').trigger('click')

        expect(wrapper.find('.color-picker__popover').exists()).toBe(true)

        await wrapper.get('button[title="Green"]').trigger('click')

        expect(wrapper.emitted('update:modelValue')).toEqual([[colorPalette.green.color]])
        expect(wrapper.find('.color-picker__popover').exists()).toBe(false)
    })
})
