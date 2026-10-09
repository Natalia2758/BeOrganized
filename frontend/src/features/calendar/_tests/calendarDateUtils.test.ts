import { describe, expect, it } from 'vitest'
import type { Event } from '@/features/event/types/event'
import { generateCalendarDates, getCalendarStartDate, getEndDateOfMonth, getStartDateOfMonth, toCalendarDaysType } from '../utils/calendarDateUtils'


describe('calendarUtils', () => {
    it('gets the start of the month', () => {
        const date = new Date(2026, 4, 15)

        const result = getStartDateOfMonth(date)

        expect(result).toEqual(new Date(2026, 4, 1))
    })

    it('gets the end of the month', () => {
        const date = new Date(2026, 4, 15)

        const result = getEndDateOfMonth(date)

        expect(result).toEqual(new Date(2026, 4, 31))
    })

    it('gets the calendar start date on Monday', () => {
        // May 1, 2026 is a Friday
        const date = new Date(2026, 4, 15)

        const result = getCalendarStartDate(date)

        // Monday, April 27
        expect(result).toEqual(new Date(2026, 3, 27))
    })

    it('generates 42 calendar dates', () => {
        const date = new Date(2026, 4, 15)

        const result = generateCalendarDates(date)

        expect(result).toHaveLength(42)
    })

    it('assigns events to the correct calendar days', () => {
        const date = new Date(2026, 4, 15)

        const event: Event = {
            idEvent: 1,
            title: 'Event',
            details: null,
            category: null,
            dateStart: '2026-05-10',
            dateEnd: '2026-05-12',
            duration: false,
            timeStart: null,
            timeEnd: null,
            repeated: false,
            repeatedFrequency: null
        }

        const result = toCalendarDaysType(date, [event])

        const daysWithEvent = result.filter(
            day => day.events.length > 0
        )

        expect(daysWithEvent).toHaveLength(3)

        expect(
            daysWithEvent.map(day => day.date.getDate())
        ).toEqual([10, 11, 12])
    })
})