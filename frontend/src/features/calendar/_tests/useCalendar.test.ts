import { describe, expect, it, vi, beforeEach } from 'vitest'
import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'

import { useEvents } from '@/features/event/composables/useEvents'
import { useCalendar } from '../composables/useCalendar'

vi.mock('@/features/event/composables/useEvents', () => ({
    useEvents: vi.fn()
}))

function setupComposable() {
    let result: ReturnType<typeof useCalendar>

    const component = defineComponent({
        setup() {
            result = useCalendar()
            return () => h('div')
        }
    })

    mount(component)

    return result!
}

describe('useCalendar', () => {
    const loadEvents = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()

        vi.mocked(useEvents).mockReturnValue({
            events: { value: [] },
            error: { value: null },
            loading: { value: false },
            loadEvents
        } as unknown as ReturnType<typeof useEvents>)
    })

    it('loads events when mounted', () => {
        setupComposable()

        expect(loadEvents).toHaveBeenCalled()
    })

    it('moves to the next month', () => {
        const calendar = setupComposable()

        calendar.currentDate.value = new Date(2026, 4, 15)

        calendar.nextMonth()

        expect(calendar.currentDate.value).toEqual(
            new Date(2026, 5, 1)
        )
    })

    it('moves to the previous month', () => {
        const calendar = setupComposable()

        calendar.currentDate.value = new Date(2026, 4, 15)

        calendar.previousMonth()

        expect(calendar.currentDate.value).toEqual(
            new Date(2026, 3, 1)
        )
    })

    it('moves to the next day', () => {
        const calendar = setupComposable()

        calendar.currentDate.value = new Date(2026, 4, 15)

        calendar.nextDay()

        expect(calendar.currentDate.value).toEqual(
            new Date(2026, 4, 16)
        )
    })

    it('moves to the previous day', () => {
        const calendar = setupComposable()

        calendar.currentDate.value = new Date(2026, 4, 15)

        calendar.previousDay()

        expect(calendar.currentDate.value).toEqual(
            new Date(2026, 4, 14)
        )
    })

    it('sets the current date to today', () => {
        const calendar = setupComposable()

        const before = new Date()

        calendar.currentDate.value = new Date(2020, 0, 1)

        calendar.today()

        const after = new Date()

        expect(calendar.currentDate.value.getTime()).toBeGreaterThanOrEqual(
            before.getTime()
        )
        expect(calendar.currentDate.value.getTime()).toBeLessThanOrEqual(
            after.getTime()
        )
    })
})