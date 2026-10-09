import type { Reminder } from "@/features/reminder/types/reminder"

export interface Notification {
    idNotification: string
    reminder: Reminder
    type: NotificationType
    status: NotificationStatus
    sentTime: Date
}

export enum NotificationType {
    NAVBAR = 'NAVBAR',
    BROWSER = 'BROWSER'
}

export enum NotificationStatus {
    SENT = 'SENT',
    OPENED = 'OPENED'
}
