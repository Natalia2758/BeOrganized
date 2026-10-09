import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ReminderListItem from '../components/ReminderListItem.vue'
import { ReminderStatus, ReminderType, Timing, type Reminder } from '../types/reminder'

const reminder: Reminder = {
    idReminder: 3,
    content: 'Review notes',
    type: ReminderType.NONE,
    idEvent: null,
    idTask: null,
    repeatForAll: false,
    remindAt: Timing.SET_MANUALLY,
    remindDate: '2026-10-12',
    remindTime: '09:30',
    sentTime: null,
    status: ReminderStatus.PENDING
}

describe('ReminderListItem', () => {
    it('emits the reminder ID and cancelled status when toggled off', async () => {
        const wrapper = mount(ReminderListItem, {
            props: { reminder },
            global: {
                stubs: {
                    RouterLink: {
                        props: ['to'],
                        template: '<a :data-to="JSON.stringify(to)"><slot /></a>'
                    }
                }
            }
        })

        await wrapper.get('input[type="checkbox"]').setValue(false)

        expect(wrapper.emitted('toggle')).toEqual([[
            { idReminder: 3, status: ReminderStatus.CANCELLED }
        ]])
        expect(JSON.parse(wrapper.get('a').attributes('data-to')!)).toEqual({
            name: 'reminder-update',
            params: { idReminder: 3 }
        })
    })

    it('emits the reminder when deletion is requested and hides delete in dashboard mode', async () => {
        const wrapper = mount(ReminderListItem, {
            props: { reminder },
            global: { stubs: { RouterLink: true } }
        })

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('deleteRequest')).toEqual([[reminder]])

        const dashboardWrapper = mount(ReminderListItem, {
            props: { reminder, forDashboard: true },
            global: { stubs: { RouterLink: true } }
        })

        expect(dashboardWrapper.find('button').exists()).toBe(false)
    })
})
