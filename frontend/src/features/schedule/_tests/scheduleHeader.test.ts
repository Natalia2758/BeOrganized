import { describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { mount } from '@vue/test-utils'
import ScheduleHeader from '../components/ScheduleHeader.vue'

const isMobile = ref(false)

vi.mock('@/common/composables/useIsMobile', () => ({
    useIsMobile: () => ({
        isMobile
    })
}))

describe('ScheduleHeader', () => {
    it('emits week navigation on desktop', async () => {
        isMobile.value = false

        const wrapper = mount(ScheduleHeader, {
            props: {
                weekStart: new Date(2026, 9, 5),
                weekEnd: new Date(2026, 9, 11),
                today: new Date(2026, 9, 8)
            }
        })

        const buttons = wrapper.findAll('button')
        await buttons[0]!.trigger('click')
        await buttons[1]!.trigger('click')

        expect(wrapper.emitted('previousWeek')).toHaveLength(1)
        expect(wrapper.emitted('nextWeek')).toHaveLength(1)
        expect(wrapper.emitted('previousDay')).toBeUndefined()
        expect(wrapper.emitted('nextDay')).toBeUndefined()
    })

    it('emits day navigation on mobile', async () => {
        isMobile.value = true

        const wrapper = mount(ScheduleHeader, {
            props: {
                weekStart: new Date(2026, 9, 5),
                weekEnd: new Date(2026, 9, 11),
                today: new Date(2026, 9, 8)
            }
        })

        const buttons = wrapper.findAll('button')
        await buttons[0]!.trigger('click')
        await buttons[1]!.trigger('click')

        expect(wrapper.emitted('previousDay')).toHaveLength(1)
        expect(wrapper.emitted('nextDay')).toHaveLength(1)
        expect(wrapper.emitted('previousWeek')).toBeUndefined()
        expect(wrapper.emitted('nextWeek')).toBeUndefined()
    })
})
