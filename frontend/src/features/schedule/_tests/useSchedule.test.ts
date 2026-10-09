import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, ref } from 'vue'
import { mount } from '@vue/test-utils'

import { useSchedule } from '../composables/useSchedule'
import { useEvents } from '@/features/event/composables/useEvents'
import { useTaskListReloadToken, useTasks } from '@/features/task/composables/useTasks'

vi.mock('@/features/event/composables/useEvents', () => ({
    useEvents: vi.fn()
}))

vi.mock('@/features/task/composables/useTasks', () => ({
    useTasks: vi.fn()
}))

vi.mock('@/features/task/composables/useTaskListReloadToken', () => ({
    useTaskListReloadToken: vi.fn()
}))

function setupComposable() {
    let result: ReturnType<typeof useSchedule>

    const component = defineComponent({
        setup() {
            result = useSchedule()
            return () => h('div')
        }
    })

    mount(component)

    return result!
}

describe('useSchedule', () => {
    const loadEvents = vi.fn()
    const loadTasks = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()

        vi.mocked(useEvents).mockReturnValue({
            events: ref([]),
            loadEvents
        } as unknown as ReturnType<typeof useEvents>)

        vi.mocked(useTasks).mockReturnValue({
            tasks: ref([]),
            loadTasks
        } as unknown as ReturnType<typeof useTasks>)

        vi.mocked(useTaskListReloadToken).mockReturnValue(ref(0))
    })

    it('loads events and tasks on mount', () => {
        setupComposable()

        expect(loadEvents).toHaveBeenCalled()
        expect(loadTasks).toHaveBeenCalled()
    })

    it('moves to the next week', () => {
        const schedule = setupComposable()

        schedule.currentDate.value = new Date(2026, 4, 15)

        schedule.nextWeek()

        expect(schedule.currentDate.value).toEqual(
            new Date(2026, 4, 22)
        )
    })

    it('moves to the previous week', () => {
        const schedule = setupComposable()

        schedule.currentDate.value = new Date(2026, 4, 15)

        schedule.previousWeek()

        expect(schedule.currentDate.value).toEqual(
            new Date(2026, 4, 8)
        )
    })

    it('moves to the next day', () => {
        const schedule = setupComposable()

        schedule.currentDate.value = new Date(2026, 4, 15)

        schedule.nextDay()

        expect(schedule.currentDate.value).toEqual(
            new Date(2026, 4, 16)
        )
    })

    it('moves to the previous day', () => {
        const schedule = setupComposable()

        schedule.currentDate.value = new Date(2026, 4, 15)

        schedule.previousDay()

        expect(schedule.currentDate.value).toEqual(
            new Date(2026, 4, 14)
        )
    })

    it('goes to today', () => {
        const schedule = setupComposable()

        schedule.currentDate.value = new Date(2020, 0, 1)

        const before = new Date()

        schedule.goToToday()

        const after = new Date()

        expect(schedule.currentDate.value.getTime()).toBeGreaterThanOrEqual(
            before.getTime()
        )
        expect(schedule.currentDate.value.getTime()).toBeLessThanOrEqual(
            after.getTime()
        )
    })

    it('loads tasks when the reload token changes', async () => {
        const token = ref(0)

        vi.mocked(useTaskListReloadToken).mockReturnValue(token)

        setupComposable()

        loadTasks.mockClear()

        token.value++

        await Promise.resolve()

        expect(loadTasks).toHaveBeenCalled()
    })
})