import type { Event } from "@/features/event/types/event";

export interface CalendarDay {
    date: Date;
    dayNumber: number;
    weekDay: number;

    isCurrentMonth: boolean;
    isToday: boolean;

    events: Event[];
}
