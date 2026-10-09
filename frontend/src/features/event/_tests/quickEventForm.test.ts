import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import QuickEventForm from '../components/QuickEventForm.vue'

describe('QuickEventForm', () => {
    it('requires a title and start date', async () => {
        const wrapper = mount(QuickEventForm, {
            props: { isSubmitted: false }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Title is required')
        expect(wrapper.text()).toContain('Start date of the event is required')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('emits the quick event data', async () => {
        const wrapper = mount(QuickEventForm, {
            props: { isSubmitted: false }
        })

        await wrapper.get('#title').setValue('Appointment')
        await wrapper.get('#start-date').setValue('2026-10-12')
        await wrapper.get('#end-date').setValue('2026-10-13')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            {
                title: 'Appointment',
                dateStart: '2026-10-12',
                dateEnd: '2026-10-13'
            }
        ]])
    })

    it('disables controls while submitting and emits cancel', async () => {
        const wrapper = mount(QuickEventForm, {
            props: { isSubmitted: true }
        })

        expect(wrapper.get('.btn-primary').attributes('disabled')).toBeDefined()
        expect(wrapper.get('.btn-danger').attributes('disabled')).toBeDefined()

        const readyWrapper = mount(QuickEventForm, {
            props: { isSubmitted: false }
        })
        await readyWrapper.get('.btn-danger').trigger('click')

        expect(readyWrapper.emitted('cancel')).toHaveLength(1)
    })
})
