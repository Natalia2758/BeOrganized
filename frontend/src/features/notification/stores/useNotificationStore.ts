import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { NotificationStatus, type Notification } from "../types/notification";

export const useNotificationStore = defineStore('notiications', () => {
    const notifications = ref<Notification[]>([])

    const error = ref<string | null>(null)


    function addNotification(notification: Notification) {

        const exists = notifications.value.some(
            existing => existing.reminder.idReminder === notification.reminder.idReminder
        )

        if (exists) return

        notifications.value.push(notification)
    }

    function markAsOpened(idNotification: string) {
        const notification = notifications.value.find(
            notification => notification.idNotification === idNotification
        )

        if (notification) {
            notification.status = NotificationStatus.OPENED
        }

    }

    function removeNotification(idNotification: string) {
        notifications.value = notifications.value.filter(
            notification => notification.idNotification !== idNotification
        )
    }

    function clearNotifications() {
        notifications.value = []
    }

    return {
        notifications,
        addNotification,
        markAsOpened,
        removeNotification,
        clearNotifications
    }


})