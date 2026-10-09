export interface Reminder {
    idReminder: number
    content: string
    type: ReminderType
    idEvent: number | null
    idTask: number | null
    repeatForAll: boolean
    remindAt: Timing
    remindDate: string
    remindTime: string
    sentTime: string | null
    status: ReminderStatus
}

export interface ReminderCreateRequest {
    content: string
    type: ReminderType
    idEvent: number | null
    idTask: number | null
    repeatForAll: boolean
    remindAt: Timing
    remindDate: string | null
    remindTime: string | null
}

export interface ReminderStatusChangeRequest {
    idReminder: number
    status: ReminderStatus
}

export enum Timing {
    AT_TIME = 'AT_TIME',
    QUARTER_BEFORE = 'QUARTER_BEFORE',
    HOUR_BEFORE = 'HOUR_BEFORE',
    DAY_BEFORE = 'DAY_BEFORE',
    SET_MANUALLY = 'SET_MANUALLY'
}

export enum ReminderType {
    TASK = 'TASK',
    EVENT = 'EVENT',
    NONE = 'NONE'
}

export enum ReminderStatus {
    PENDING = 'PENDING',
    SENT = 'SENT',
    OPENED = 'OPENED',
    CANCELLED = 'CANCELLED'
}

export const timingLabels: Record<Timing, string> = {
    [Timing.AT_TIME]: 'At time',
    [Timing.QUARTER_BEFORE]: 'A quarter before',
    [Timing.HOUR_BEFORE]: 'An hour before',
    [Timing.DAY_BEFORE]: 'A day before',
    [Timing.SET_MANUALLY]: 'Custom timing (Set manually)',
}

export const reminderTypeLabels: Record<ReminderType, string> = {
    [ReminderType.TASK]: 'Task',
    [ReminderType.EVENT]: 'Event',
    [ReminderType.NONE]: 'Other',
}