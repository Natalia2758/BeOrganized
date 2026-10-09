import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ReminderAssignEventTaskModal from '../components/ReminderAssignEventTaskModal.vue'
import type { Event } from '@/features/event/types/event'
import type { Task } from '@/features/task/types/task'

const event: Event = {
    idEvent: 8,
    title: 'Conference',
    details: null,
    category: null,
    dateStart: '2026-10-12',
    dateEnd: '2026-10-12',
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: true,
    repeatedFrequency: null
}

const task: Task = {
    idTask: 4,
    title: 'Prepare report',
    description: null,
    priority: null,
    category: null,
    startDate: null,
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: true,
    repeatedFrequency: null,
    progress: 0,
    finished: false,
    subtasks: []
}

describe('ReminderAssignEventTaskModal', () => {
    it('emits validation feedback and stays open when nothing is selected', async () => {
        const wrapper = mount(ReminderAssignEventTaskModal, {
            props: {
                type: 'task',
                tasks: [task],
                error: null
            }
        })

        await wrapper.get('.btn-primary').trigger('click')

        expect(wrapper.text()).toContain('Please choose task')
        expect(wrapper.emitted('close')).toBeUndefined()
        expect(wrapper.emitted('repeatForAll')).toBeUndefined()
    })

    it('emits selected event ID and closes after selection', async () => {
        const wrapper = mount(ReminderAssignEventTaskModal, {
            props: {
                type: 'event',
                events: [event],
                error: null
            }
        })

        await wrapper.get('select').setValue('8')
        await wrapper.get('.btn-primary').trigger('click')

        expect(wrapper.emitted('idEvent')).toEqual([[null], [8]])
        expect(wrapper.emitted('repeatForAll')).toEqual([[false]])
        expect(wrapper.emitted('close')).toHaveLength(1)
    })

    it('emits task ID and repeat-for-all choice for a repeated task', async () => {
        const wrapper = mount(ReminderAssignEventTaskModal, {
            props: {
                type: 'task',
                tasks: [task],
                task,
                error: null
            }
        })

        await wrapper.get('select').setValue('4')
        await wrapper.get('#repeatForAll').setValue(true)
        await wrapper.get('.btn-primary').trigger('click')

        expect(wrapper.emitted('idTask')).toEqual([[null], [4]])
        expect(wrapper.emitted('repeatForAll')).toEqual([[true]])
        expect(wrapper.emitted('close')).toHaveLength(1)
    })
})
