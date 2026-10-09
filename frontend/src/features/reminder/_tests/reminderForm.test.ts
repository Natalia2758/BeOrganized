import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ReminderForm from '../components/ReminderForm.vue'
import { ReminderStatus, ReminderType, Timing, type Reminder } from '../types/reminder'

vi.mock('bootstrap/js/dist/tooltip', () => ({
    default: class Tooltip {}
}))

const emptyRelations = {
    events: [],
    tasks: [],
    loading: false,
    error: null
}

describe('ReminderForm', () => {
    it('requires reminder content and manual date/time', async () => {
        const wrapper = mount(ReminderForm, {
            props: emptyRelations
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Reminder message must not be null')
        expect(wrapper.text()).toContain('Please set the date and time of the reminder')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('requires an associated task or event for that reminder type', async () => {
        const wrapper = mount(ReminderForm, {
            props: emptyRelations
        })

        await wrapper.get('#content').setValue('Submit the report')
        await wrapper.get('#type').setValue(ReminderType.TASK)
        await wrapper.get('#remindDate').setValue('2099-10-01')
        await wrapper.get('#remindTime').setValue('09:30')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Please choose a task to be reminded about')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('submits a valid manually scheduled reminder', async () => {
        const wrapper = mount(ReminderForm, {
            props: emptyRelations
        })

        await wrapper.get('#content').setValue('Review notes')
        await wrapper.get('#remindDate').setValue('2099-10-01')
        await wrapper.get('#remindTime').setValue('09:30')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            {
                content: 'Review notes',
                type: ReminderType.NONE,
                idEvent: null,
                idTask: null,
                repeatForAll: false,
                remindAt: Timing.SET_MANUALLY,
                remindDate: '2099-10-01',
                remindTime: '09:30'
            }
        ]])
    })

    it('loads existing reminder data and emits cancel', async () => {
        const initialReminder: Reminder = {
            idReminder: 2,
            content: 'Review notes',
            type: ReminderType.NONE,
            idEvent: null,
            idTask: null,
            repeatForAll: false,
            remindAt: Timing.SET_MANUALLY,
            remindDate: '2099-10-01',
            remindTime: '09:30:00',
            sentTime: null,
            status: ReminderStatus.PENDING
        }
        const wrapper = mount(ReminderForm, {
            props: {
                ...emptyRelations,
                initialReminder
            }
        })

        expect((wrapper.get('#content').element as HTMLInputElement).value).toBe('Review notes')
        expect((wrapper.get('#remindDate').element as HTMLInputElement).value).toBe('2099-10-01')

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('cancel')).toHaveLength(1)
    })

    it('preserves the associated task when editing a reminder', async () => {
        const initialReminder: Reminder = {
            idReminder: 3,
            content: 'Submit the report',
            type: ReminderType.TASK,
            idEvent: null,
            idTask: 11,
            repeatForAll: false,
            remindAt: Timing.SET_MANUALLY,
            remindDate: '2099-10-01',
            remindTime: '09:30',
            sentTime: null,
            status: ReminderStatus.PENDING
        }
        const wrapper = mount(ReminderForm, {
            props: {
                ...emptyRelations,
                initialReminder
            }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')?.[0]?.[0]).toMatchObject({
            type: ReminderType.TASK,
            idTask: 11
        })
    })
})
