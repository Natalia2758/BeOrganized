import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import SubtaskListItem from '../components/SubtaskListItem.vue'
import type { Subtask } from '../types/task'

describe('SubtaskListItem', () => {
    it('renders the subtask and emits its ID with the new checked state', async () => {
        const subtask: Subtask = {
            idSubtask: 7,
            title: 'Prepare notes',
            completed: false
        }
        const wrapper = mount(SubtaskListItem, {
            props: { subtask }
        })

        expect(wrapper.text()).toContain('Prepare notes')
        expect((wrapper.get('input').element as HTMLInputElement).checked).toBe(false)

        await wrapper.get('input').setValue(true)

        expect(wrapper.emitted('checked')).toEqual([[
            { idSubtask: 7, completed: true }
        ]])
    })

    it('emits false when a completed subtask is unchecked', async () => {
        const wrapper = mount(SubtaskListItem, {
            props: {
                subtask: {
                    idSubtask: 7,
                    title: 'Prepare notes',
                    completed: true
                }
            }
        })

        await wrapper.get('input').setValue(false)

        expect(wrapper.emitted('checked')).toEqual([[
            { idSubtask: 7, completed: false }
        ]])
    })
})
