import type { Reminder } from "@/features/reminder/types/reminder";
import { useNotificationStore } from "../stores/useNotificationStore";
import { onUnmounted } from "vue"
import { createNotification } from "../utils/notificationMapper";
import { toApiError } from "@/common/api/apiError";
import { getSentReminders } from "@/features/reminder/api/reminderApi";

/** Manages notification history and the live reminder event stream. */
export function useNotifications() {
    const notificationStore = useNotificationStore()

    let eventSource: EventSource | null = null

    /** Loads sent reminders and subscribes to new reminder events. */
    async function connect() {

        if (eventSource) return

        const reminders = await getSentReminders()

        reminders.forEach(reminder => {
            const notification = createNotification(reminder)
            notificationStore.addNotification(notification)
        })

        eventSource = new EventSource('/api/reminders/stream')

        eventSource.addEventListener('reminder', (event) => {
                const reminder = JSON.parse(event.data) as Reminder

                const notification = createNotification(reminder)

                notificationStore.addNotification(notification)
            }
        )

        eventSource.onerror = (error) => {
            throw toApiError(error)
        }
    }

    /** Closes the live stream and clears its connection reference. */
    function disconnect() {
        eventSource?.close()
        eventSource = null
    }

    /** Releases the stream when its owning component is unmounted. */
    onUnmounted(disconnect)

    return {
        connect,
        disconnect
    }

}