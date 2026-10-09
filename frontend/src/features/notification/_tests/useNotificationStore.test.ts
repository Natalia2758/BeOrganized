import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import {
    NotificationStatus,
    NotificationType,
    type Notification
} from '../types/notification'
import { useNotificationStore } from '../stores/useNotificationStore'
import { ReminderStatus, ReminderType, Timing } from '@/features/reminder/types/reminder'

describe('useNotificationStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
    })

    function createNotification(
        idNotification: string,
        idReminder: number
    ): Notification {
        return {
            idNotification,
            reminder: {
                idReminder: 1,
                content: 'Reminder 1',
                type: ReminderType.NONE as const,
                idEvent: null,
                idTask: null,
                repeatForAll: false,
                remindAt: Timing.SET_MANUALLY,
                remindDate: "2026-10-10",
                remindTime: "10:20",
                sentTime: null,
                status: ReminderStatus.PENDING
            },
            type: NotificationType.NAVBAR,
            status: NotificationStatus.SENT,
            sentTime: new Date()
        }
    }

    it('adds a notification', () => {
        const store = useNotificationStore()

        const notification = createNotification('1', 10)

        store.addNotification(notification)

        expect(store.notifications).toEqual([notification])
    })

    it('does not add a notification with an existing reminder', () => {
        const store = useNotificationStore()

        const notification1 = createNotification('1', 10)
        const notification2 = createNotification('2', 10)

        store.addNotification(notification1)
        store.addNotification(notification2)

        expect(store.notifications).toHaveLength(1)
        expect(store.notifications[0]).toEqual(notification1)
    })

    it('marks a notification as opened', () => {
        const store = useNotificationStore()

        const notification = createNotification('1', 10)

        store.addNotification(notification)
        store.markAsOpened('1')

        expect(store.notifications[0]!.status).toBe(
            NotificationStatus.OPENED
        )
    })

    it('removes a notification', () => {
        const store = useNotificationStore()

        const notification = createNotification('1', 10)

        store.addNotification(notification)
        store.removeNotification('1')

        expect(store.notifications).toHaveLength(0)
    })

    it('clears all notifications', () => {
        const store = useNotificationStore()

        store.addNotification(createNotification('1', 10))
        store.addNotification(createNotification('2', 20))

        store.clearNotifications()

        expect(store.notifications).toEqual([])
    })
})