import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import QuickTaskForm from '../components/QuickTaskForm.vue'
import { Priority } from '../types/task'

describe('QuickTaskForm', () => {
    it('requires a non-blank title', async () => {
        const wrapper = mount(QuickTaskForm, {
            props: { isSubmitted: false }
        })

        await wrapper.get('#title').setValue('   ')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Title is required')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('emits title, priority, and date', async () => {
        const wrapper = mount(QuickTaskForm, {
            props: { isSubmitted: false }
        })

        await wrapper.get('#title').setValue('Prepare report')
        await wrapper.get('#priority').setValue(Priority.HIGH)
        await wrapper.get('#date').setValue('2026-10-12')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            {
                title: 'Prepare report',
                priority: Priority.HIGH,
                startDate: '2026-10-12'
            }
        ]])
    })

    it('disables submission and emits cancel while a request is pending', async () => {
        const wrapper = mount(QuickTaskForm, {
            props: { isSubmitted: true }
        })

        expect(wrapper.get('.btn-primary').attributes('disabled')).toBeDefined()

        const readyWrapper = mount(QuickTaskForm, {
            props: { isSubmitted: false }
        })
        await readyWrapper.get('.btn-danger').trigger('click')

        expect(readyWrapper.emitted('cancel')).toHaveLength(1)
    })
})
