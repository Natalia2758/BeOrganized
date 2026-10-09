import type { Event } from "@/features/event/types/event"
import type { CalendarDay } from "../types/calendar"
import { isSameDay } from "@/common/utils/dateTimeUtils"
import { generateEventsOccurences, getDateKey } from "@/common/utils/repeatedItemsUtils"

/**
 * Returns a Date object with the first date of month.
 * @param date  a reference date
 * @returns     a first day of month
 */
export function getStartDateOfMonth(date: Date): Date {
    return new Date(
        date.getFullYear(),
        date.getMonth(),
        1
    )
}

/**
 * Returns a Date object with the last date of month.
 * Setting day to 0 to get the last day of the previous month.
 * @param date  a reference date
 * @returns     a last day of month
 */
export function getEndDateOfMonth(date: Date): Date {
    return new Date(
        date.getFullYear(),
        date.getMonth() + 1,
        0
    )
}

/**
 * Calculates the actual start date of the calendar grid.
 * The date is the last Monday of the previous month.
 * 
 * @param currentDate   a reference date
 * @returns             a first day that will be displayed in the calendar
 */
export function getCalendarStartDate(currentDate: Date): Date {
    const firstDayOfMonth = getStartDateOfMonth(currentDate)

    const day = firstDayOfMonth.getDay()

    // Week starts on Monday
    const daysFromMonday = day === 0 ? 6 : day - 1

    const firstDateToDisplay = new Date(firstDayOfMonth)

    // Sets date the amount of daysFromMonday days before the 1st day of month
    firstDateToDisplay.setDate(
        firstDayOfMonth.getDate() - daysFromMonday
    )

    return firstDateToDisplay
}

// Date[] with single Date per day cell
/**
 * Generates dates array for the month of the given date, that represent calendar dates.
 * The array contains dates from the previous and the next month, so that the calendar grid is entirely filled.
 * 
 * @param currentDate   a reference date
 * @returns             an array of the Date objects
 */
export function generateCalendarDates(currentDate: Date): Date[]{

    // First date to be displayed on the calendar
    const startDate = getCalendarStartDate(currentDate)

    // Array of days to be displayed
    const days: Date[] = []

    // 42 -> all days (day cells) that the calendar grid is built from
    for (let i = 0; i < 42; i++) {
        const date = new Date(startDate)

        // Next date, starting from the startDate
        date.setDate(startDate.getDate() + i)

        // Add date to the days array
        days.push(date)
    }

    return days
}


/**
 * Maps events and dates to the CalendarDay array.
 * 
 * @param currentDate   a reference date
 * @param events        an array of the Event objects
 * @returns             an array of CalendarDay objects, containing dates and events assigned to them
 */
export function toCalendarDaysType(currentDate: Date, events: Event[]): CalendarDay[] {
    const days = generateCalendarDates(currentDate)
    const eventOccurences = generateEventsOccurences(events)

    return days.map((day, dayNumber) => {

        const dayKey = getDateKey(day)

        return {
            date: day,
            dayNumber,

            weekDay:
                day.getDay() === 0
                    ? 6
                    : day.getDay() - 1,

            isCurrentMonth:
                day.getMonth() === currentDate.getMonth() &&
                day.getFullYear() === currentDate.getFullYear(),

            isToday:
                isSameDay(day, new Date()),

            events: eventOccurences.filter(event => {

                const startKey = event.dateStart.slice(0, 10)
                const endKey = event.dateEnd.slice(0, 10)

                return (
                    startKey <= dayKey &&
                    dayKey <= endKey
                )
            })
        }
    })
}


