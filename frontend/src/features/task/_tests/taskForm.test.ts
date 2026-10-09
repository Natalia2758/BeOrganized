import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskForm from '../components/TaskForm.vue'
import type { Task } from '../types/task'

vi.mock('bootstrap/js/dist/tooltip', () => ({
    default: class Tooltip {}
}))

describe('TaskForm', () => {
    it('requires a non-empty title', async () => {
        const wrapper = mount(TaskForm, {
            props: { categories: [] }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Title is required')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('requires start and end times for timed tasks', async () => {
        const wrapper = mount(TaskForm, {
            props: { categories: [] }
        })

        await wrapper.get('#title').setValue('Prepare report')
        await wrapper.get('#date').setValue('2026-10-05')
        await wrapper.get('#duration').setValue(true)
        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Please specify start and end time of the task')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('submits task data including initial subtasks in edit mode', async () => {
        const initialTask: Task = {
            idTask: 9,
            title: 'Prepare report',
            description: 'Quarterly numbers',
            priority: null,
            category: null,
            startDate: '2026-10-05',
            duration: false,
            timeStart: null,
            timeEnd: null,
            repeated: false,
            repeatedFrequency: null,
            progress: 0,
            finished: false,
            subtasks: [
                { idSubtask: 1, title: 'Gather data', completed: false }
            ]
        }
        const wrapper = mount(TaskForm, {
            props: { categories: [], initialTask }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            {
                title: 'Prepare report',
                description: 'Quarterly numbers',
                priority: null,
                idCategory: null,
                startDate: '2026-10-05',
                duration: false,
                timeStart: null,
                timeEnd: null,
                repeated: false,
                repeatedFrequency: null,
                subtasks: ['Gather data']
            }
        ]])
    })

    it('emits cancel', async () => {
        const wrapper = mount(TaskForm, {
            props: { categories: [] }
        })

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('cancel')).toHaveLength(1)
    })
})
