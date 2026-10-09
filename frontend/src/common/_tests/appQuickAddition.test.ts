import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { defineComponent, h, ref } from 'vue'
import AppQuickAddition from '../components/ui/AppQuickAddition.vue'
import { useTasks } from '@/features/task/composables/useTasks'
import { useEvents } from '@/features/event/composables/useEvents'
import { useNotes } from '@/features/note/composables/useNotes'
import QuickTaskModal from '@/features/task/components/QuickTaskModal.vue'
import QuickEventModal from '@/features/event/components/QuickEventModal.vue'
import QuickNoteModal from '@/features/note/components/QuickNoteModal.vue'
import type { Task } from '@/features/task/types/task'
import type { Event } from '@/features/event/types/event'
import type { Note } from '@/features/note/types/note'

vi.mock('@/features/task/composables/useTasks', () => ({
    useTasks: vi.fn()
}))
vi.mock('@/features/event/composables/useEvents', () => ({
    useEvents: vi.fn()
}))
vi.mock('@/features/note/composables/useNotes', () => ({
    useNotes: vi.fn()
}))

const taskRequest = { title: 'Prepare report', priority: 'MEDIUM', startDate: null }
const eventRequest = { title: 'Conference', dateStart: '2026-10-12', dateEnd: '2026-10-12' }
const noteRequest = { title: 'Ideas', content: 'Plan the trip' }

function modalStub(className: string, request: object) {
    return defineComponent({
        props: ['isSubmitted', 'error'],
        emits: ['submit', 'close'],
        setup(_, { emit }) {
            return () => h('div', { class: className }, [
                h('button', {
                    class: 'stub-submit',
                    onClick: () => emit('submit', request)
                }, 'Submit'),
                h('button', {
                    class: 'stub-close',
                    onClick: () => emit('close')
                }, 'Close')
            ])
        }
    })
}

const stubs = {
    QuickTaskModal: modalStub('quick-task-modal', taskRequest),
    QuickEventModal: modalStub('quick-event-modal', eventRequest),
    QuickNoteModal: modalStub('quick-note-modal', noteRequest)
}

const addTaskQuick = vi.fn()
const addEventQuick = vi.fn()
const addNoteQuick = vi.fn()

describe('AppQuickAddition', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        addTaskQuick.mockResolvedValue(undefined)
        addEventQuick.mockResolvedValue(undefined)
        addNoteQuick.mockResolvedValue(undefined)

        vi.mocked(useTasks).mockReturnValue({
            tasks: ref<Task[]>([]),
            loading: ref(false),
            error: ref<string | null>(null),
            loadTasks: vi.fn(),
            addTask: vi.fn(),
            addTaskQuick,
            editTask: vi.fn(),
            removeTask: vi.fn(),
            setFinished: vi.fn(),
            loadTasksByDate: vi.fn(),
            loadTasksInFuture: vi.fn()
        })
        vi.mocked(useEvents).mockReturnValue({
            events: ref<Event[]>([]),
            loading: ref(false),
            error: ref<string | null>(null),
            loadEvents: vi.fn(),
            addEvent: vi.fn(),
            addEventQuick,
            editEvent: vi.fn(),
            removeEvent: vi.fn(),
            loadEventsByDate: vi.fn(),
            loadEventsInFuture: vi.fn()
        })
        vi.mocked(useNotes).mockReturnValue({
            notes: ref<Note[]>([]),
            loading: ref(false),
            error: ref<string | null>(null),
            loadNotes: vi.fn(),
            addNote: vi.fn(),
            addNoteQuick,
            editNote: vi.fn(),
            removeNote: vi.fn(),
            loadNotesByDate: vi.fn()
        })
    })

    function mountApp() {
        return mount(AppQuickAddition, {
            global: { stubs }
        })
    }

    it('opens the matching modal from each quick-add menu action', async () => {
        const taskWrapper = mountApp()
        await taskWrapper.get('.btn-addition').trigger('click')
        await taskWrapper.get('.quick-addition__menu__item').trigger('click')
        expect(taskWrapper.findComponent(QuickTaskModal).exists()).toBe(true)
        expect(taskWrapper.find('.quick-addition__menu').exists()).toBe(false)

        const eventWrapper = mountApp()
        await eventWrapper.get('.btn-addition').trigger('click')
        await eventWrapper.findAll('.quick-addition__menu__item')[1]!.trigger('click')
        expect(eventWrapper.findComponent(QuickEventModal).exists()).toBe(true)

        const noteWrapper = mountApp()
        await noteWrapper.get('.btn-addition').trigger('click')
        await noteWrapper.findAll('.quick-addition__menu__item')[2]!.trigger('click')
        expect(noteWrapper.findComponent(QuickNoteModal).exists()).toBe(true)
    })

    it('submits a quick task and closes its modal on success', async () => {
        const wrapper = mountApp()
        await wrapper.get('.btn-addition').trigger('click')
        await wrapper.get('.quick-addition__menu__item').trigger('click')

        await wrapper.get('.quick-task-modal .stub-submit').trigger('click')
        await flushPromises()

        expect(addTaskQuick).toHaveBeenCalledWith(taskRequest)
        expect(wrapper.find('.quick-task-modal').exists()).toBe(false)
    })

    it('keeps the quick task modal open when submission fails', async () => {
        addTaskQuick.mockRejectedValueOnce(new Error('Network error'))
        const wrapper = mountApp()
        wrapper.vm.$.appContext.app.config.errorHandler = vi.fn()

        await wrapper.get('.btn-addition').trigger('click')
        await wrapper.get('.quick-addition__menu__item').trigger('click')
        await wrapper.get('.quick-task-modal .stub-submit').trigger('click')
        await flushPromises()

        expect(addTaskQuick).toHaveBeenCalledWith(taskRequest)
        expect(wrapper.find('.quick-task-modal').exists()).toBe(true)
    })
})
