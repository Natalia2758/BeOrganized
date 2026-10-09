import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import EventForm from '../components/EventForm.vue'
import { RepeatedFrequency, type Event } from '../types/event'

vi.mock('bootstrap/js/dist/tooltip', () => ({
    default: class Tooltip {}
}))

describe('EventForm', () => {
    it('requires an event date', async () => {
        const wrapper = mount(EventForm, {
            props: { categories: [] }
        })

        await wrapper.get('#title').setValue('Appointment')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('requires start and end times for timed events', async () => {
        const wrapper = mount(EventForm, {
            props: { categories: [] }
        })

        await wrapper.get('#title').setValue('Appointment')
        await wrapper.get('#dateFrom').setValue('2026-10-05')
        await wrapper.get('#duration').setValue(true)
        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Please specify start and end time of the event')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('submits event data populated from the initial event', async () => {
        const initialEvent: Event = {
            idEvent: 4,
            title: 'Appointment',
            details: 'Bring documents',
            category: null,
            dateStart: '2026-10-05',
            dateEnd: '2026-10-06',
            duration: true,
            timeStart: '14:30:00',
            timeEnd: '15:30:00',
            repeated: true,
            repeatedFrequency: RepeatedFrequency.EVERY_WEEK
        }
        const wrapper = mount(EventForm, {
            props: { categories: [], initialEvent }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            {
                title: 'Appointment',
                details: 'Bring documents',
                idCategory: null,
                dateStart: '2026-10-05',
                dateEnd: '2026-10-06',
                duration: true,
                timeStart: '14:30',
                timeEnd: '15:30',
                repeated: true,
                repeatedFrequency: RepeatedFrequency.EVERY_WEEK
            }
        ]])
    })

    it('emits cancel', async () => {
        const wrapper = mount(EventForm, {
            props: { categories: [] }
        })

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('cancel')).toHaveLength(1)
    })
})
