import type { Reminder } from "@/features/reminder/types/reminder";
import { NotificationStatus, NotificationType, type Notification } from "../types/notification";

export function createNotification(reminder: Reminder): Notification {
    return {
        idNotification: crypto.randomUUID(),
        reminder: reminder,
        type: NotificationType.NAVBAR,
        status: NotificationStatus.SENT,
        sentTime: new Date()
    }
}