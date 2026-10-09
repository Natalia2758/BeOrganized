import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskListItem from '../components/TaskListItem.vue'
import type { Task } from '../types/task'

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
    repeated: false,
    repeatedFrequency: null,
    progress: 0,
    finished: false,
    subtasks: []
}

describe('TaskListItem', () => {
    it('emits the task ID and new completion state', async () => {
        const wrapper = mount(TaskListItem, {
            props: { task },
            global: {
                stubs: {
                    RouterLink: {
                        props: ['to'],
                        template: '<a :data-to="JSON.stringify(to)"><slot /></a>'
                    }
                }
            }
        })

        await wrapper.get('input[type="checkbox"]').setValue(true)

        expect(wrapper.emitted('toggle')).toEqual([[
            { idTask: 4, finished: true }
        ]])
        expect(JSON.parse(wrapper.get('a').attributes('data-to')!)).toEqual({
            name: 'task-details',
            params: { idTask: 4 }
        })
    })

    it('emits the selected task when deletion is requested', async () => {
        const wrapper = mount(TaskListItem, {
            props: { task },
            global: { stubs: { RouterLink: true } }
        })

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('deleteRequest')).toEqual([[task]])
    })

    it('does not show the completion checkbox when the task has subtasks', () => {
        const wrapper = mount(TaskListItem, {
            props: {
                task: {
                    ...task,
                    subtasks: [{ idSubtask: 1, title: 'Research', completed: false }]
                }
            },
            global: { stubs: { RouterLink: true } }
        })

        expect(wrapper.find('input[type="checkbox"]').exists()).toBe(false)
    })
})
