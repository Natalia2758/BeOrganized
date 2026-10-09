import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import SubtaskFormContainer from '../components/SubtaskFormContainer.vue'

describe('SubtaskFormContainer', () => {
    it('emits the updated subtask list when a row changes', async () => {
        const wrapper = mount(SubtaskFormContainer, {
            props: {
                clicks: 2,
                modelValue: ['Plan', 'Review']
            }
        })

        await wrapper.findAll('input')[1]!.setValue('Publish')

        expect(wrapper.emitted('update:modelValue')).toEqual([
            [['Plan', 'Publish']]
        ])
    })

    it('emits the removed row index', async () => {
        const wrapper = mount(SubtaskFormContainer, {
            props: {
                clicks: 2,
                modelValue: ['Plan', 'Review']
            }
        })

        await wrapper.findAll('button')[1]!.trigger('click')

        expect(wrapper.emitted('remove')).toEqual([[1]])
    })

    it('renders no rows when there are no subtasks', () => {
        const wrapper = mount(SubtaskFormContainer, {
            props: {
                clicks: 0,
                modelValue: []
            }
        })

        expect(wrapper.findAll('input')).toHaveLength(0)
        expect(wrapper.findAll('button')).toHaveLength(0)
    })
})
