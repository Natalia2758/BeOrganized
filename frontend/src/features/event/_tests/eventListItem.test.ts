import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import EventListItem from '../components/EventListItem.vue'
import type { Event } from '../types/event'

const event: Event = {
    idEvent: 8,
    title: 'Conference',
    details: null,
    category: null,
    dateStart: '2026-10-12',
    dateEnd: '2026-10-13',
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: false,
    repeatedFrequency: null
}

describe('EventListItem', () => {
    it('links to event details and displays a multi-day date range', () => {
        const wrapper = mount(EventListItem, {
            props: { event },
            global: {
                stubs: {
                    RouterLink: {
                        props: ['to'],
                        template: '<a :data-to="JSON.stringify(to)"><slot /></a>'
                    }
                }
            }
        })

        expect(JSON.parse(wrapper.get('a').attributes('data-to')!)).toEqual({
            name: 'event-details',
            params: { idEvent: 8 }
        })
        expect(wrapper.text()).toContain('12-10-2026 - 13-10-2026')
    })

    it('emits the selected event when deletion is requested', async () => {
        const wrapper = mount(EventListItem, {
            props: { event },
            global: { stubs: { RouterLink: true } }
        })

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('deleteRequest')).toEqual([[event]])
    })
})
