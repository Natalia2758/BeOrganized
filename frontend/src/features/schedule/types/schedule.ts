
export interface ScheduleDay {
    date: Date
    weekday: string
    isToday: boolean
    items: ScheduleItem[]
}

export interface ScheduleItem {
    idItem: string
    title: string
    type: string
    startDate: Date
    endDate: Date | null
    timeStart: Date | null
    timeEnd: Date | null
    catColor: string
    style: string
}