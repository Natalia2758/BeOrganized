import type { Event } from "@/features/event/types/event";
import type { Task } from "@/features/task/types/task";
import type { ScheduleDay, ScheduleItem } from "../types/schedule";
import { isSameDay } from "@/common/utils/dateTimeUtils";
import { generateEventsOccurences, generateTasksOccurences } from "@/common/utils/repeatedItemsUtils";

/**
 * Returns the Date object from time string and date string.
 * 
 * @param dateValue             a date value in the form of string
 * @param timeValue             a time value in the form of string, can be null or undefined
 * @param useEndOfDayFallback   boolean value used in cases when time is not specified
 * @returns                     a date object of the type Date
 */
function buildDateFromDateAndTime(dateValue: string, timeValue: string | null | undefined, useEndOfDayFallback = false) {
    const date = new Date(`${dateValue.slice(0, 10)}T00:00:00`)

    if (!timeValue) {
        if (useEndOfDayFallback) {
            date.setHours(23, 59, 59, 999)
        } else {
            date.setHours(0, 0, 0, 0)
        }

        return date
    }

    const [hours = "0", minutes = "0", seconds = "0"] = timeValue.split(":")

    date.setHours(
        Number(hours),
        Number(minutes),
        Number(seconds),
        0
    )

    return date
}

/**
 * Finds the monday of the week that comprises a given date
 * 
 * @param date  a Date object
 * @returns     a date of monday of the week with a given date
 */
export function getFirstDayOfWeek(date: Date) {
    const monday = new Date(date)
    const day = date.getDay()
    const diffDateForMonday = date.getDate() - day + (day === 0 ? -6 : 1);
    monday.setDate(diffDateForMonday)
    return monday
}

/**
 * Converts a time of Date into number of minutes since midnight (00:00)
 * 
 * @param date  a Date object
 * @returns     a number of minutes from midnight to the given date
 */
export function getMinutesSinceMidnight(date: Date) {
    return date.getHours() * 60 + date.getMinutes() + date.getSeconds() / 60
}

/**
 * Calculates the item position in pixels.
 * 
 * @param timeStart a time when an item starts
 * @param timeEnd   a time when an item ends
 * @returns         an items position from the top and its height   
 */
export function calculateItemPosition(timeStart: Date, timeEnd: Date) {
    const CELL_HEIGHT = 60
    const MINUTE_HEIGHT = CELL_HEIGHT / 60

    const startMinutes = getMinutesSinceMidnight(timeStart)

    const durationMinutes = (timeEnd.getTime() - timeStart.getTime()) / (1000 * 60)

    const itemTop = startMinutes * MINUTE_HEIGHT

    const itemHeight = Math.max(0, durationMinutes) * MINUTE_HEIGHT

    return {
        itemTop,
        itemHeight
    }
}

/**
 * Establishes an item style, including its position from the top and height.
 * 
 * @param timeStart a time when an item starts
 * @param timeEnd   a time when an item ends
 * @returns         a string with specified item's style
 */
export function defineItemStyle(catColor: string, timeStart?: Date, timeEnd?: Date, ) {

    if (timeStart && timeEnd) {
        const {
            itemTop,
            itemHeight
        } = calculateItemPosition(timeStart, timeEnd)

        return 'top: ' + itemTop + 'px; height: ' + itemHeight + 'px; background-color: ' + catColor + ';'
    } else {
        return 'background-color: ' + catColor + ';'
    }
    
}

function hasTime(item: ScheduleItem) {
    return item.timeStart !== null && item.timeEnd !== null
}

/**
 * Splits  items lasting multiple days into one ScheduleItem per calendar day.
 * 
 * @param item  a ScheduleItem object
 * @returns     a ScheduleItem array with item segments spread over multiple days
 */
function splitItemByDay(item: ScheduleItem): ScheduleItem[] {
    if (!item.timeStart || !item.timeEnd) return [item]

    const start = new Date(item.timeStart)
    const end = new Date(item.timeEnd)

    if (isSameDay(start, end) || end === null) {
        return [{
            ...item,
            style: defineItemStyle(item.catColor, start, end)
        }]
    }

    const result: ScheduleItem[] = []

    // Starts at midnight on the first day
    const current = new Date(start)
    current.setHours(0, 0, 0, 0)
    
    let segmentIndex = 0

    while (current < end) {
        const nextDay = new Date(current)
        nextDay.setDate(nextDay.getDate() + 1)
        nextDay.setHours(0, 0, 0, 0)

        const segmentStart = start > current ? new Date(start) : new Date(current)
        const segmentEnd = end < nextDay ? new Date(end) : new Date(nextDay)

        if (segmentEnd > segmentStart) {
            result.push({
            ...item,
            idItem: `${item.idItem}-${segmentIndex}`,
            startDate: new Date(current),
            timeStart: segmentStart,
            timeEnd: segmentEnd,
            style: defineItemStyle(item.catColor, segmentStart, segmentEnd)
            })
        }
        segmentIndex++
        current.setDate(current.getDate() + 1)
        current.setHours(0, 0, 0, 0)
        
    }

    return result
}

/**
 * Calculates style for the items that have overlapping times and need to share width.
 * 
 * @param items an array with ScheduleItem elements
 * @returns     items with changed style for overlapping items
 */
function applyCollisionLayout(items: ScheduleItem[]) {
    // Sorts items numerically by their start time
    const sortedItems = items.sort((left, right) =>
        (left.timeStart?.getTime() ?? 0) - (right.timeStart?.getTime() ?? 0)
    )

    // Groups of overlapping items
    const groups: ScheduleItem[][] = []

    // Grouping items if they overlap
    for (const item of sortedItems) {
        const currentGroup = groups.at(-1)
        const groupEnd = currentGroup?.reduce(
            (latest, groupItem) => Math.max(latest, groupItem.timeEnd?.getTime() ?? 0),
            0
        ) ?? 0

        if (!currentGroup || (item.timeStart?.getTime() ?? 0) >= groupEnd) {
            groups.push([item])
        } else {
            currentGroup.push(item)
        }
    }

    for (const group of groups) {
        // Columns within the groups
        const columns: ScheduleItem[][] = []

        for (const item of group) {
            const columnIndex = columns.findIndex(column => {
                const lastItem = column[column.length - 1]
                return !lastItem || (lastItem.timeEnd?.getTime() ?? 0) <= (item.timeStart?.getTime() ?? 0)
            })

            if (columnIndex === -1) {
                columns.push([item])
            } else {
                columns[columnIndex]!.push(item)
            }
        }

        // Defines horizontal position for items in a group
        for (const item of group) {
            const columnIndex = columns.findIndex(column => column.includes(item))
            const width = 100 / columns.length
            item.style += `left: calc(${columnIndex * width}% + 4px); width: calc(${width}% - 8px); right: auto; background-color: ${item.catColor}`
        }
    }

    return items
}



/**
 * Creates an array of ScheduleItem objects mapped from Task array and Event array.
 * 
 * @param events    an array with Event objects
 * @param tasks     an array with Task objects
 * @returns         an array of ScheduleItem objects converted from tasks and events
 */
export function generateScheduleItems(events: Event[], tasks: Task[]): ScheduleItem[] {
    const scheduleItems: ScheduleItem[] = []
    const taskOccurences: Task[] = generateTasksOccurences(tasks)
    const eventOccurences: Event[] = generateEventsOccurences(events)

    for (let i = 0; i < eventOccurences.length; i++) {
        const event = eventOccurences[i]!
        const startDate = new Date(`${event.dateStart.slice(0, 10)}T00:00:00`)
        const timeStart = buildDateFromDateAndTime(event.dateStart.slice(0, 10), event.timeStart)
        const timeEnd = buildDateFromDateAndTime(event.dateEnd.slice(0, 10), event.timeEnd, true)

        const item: ScheduleItem = {
            idItem: `event-${i}`,
            title: event.title,
            type: 'event',
            startDate,
            endDate: new Date(`${event.dateEnd.slice(0, 10)}T00:00:00`),
            timeStart,
            timeEnd,
            catColor: event.category?.color ?? '',
            style: ''
        }

        if (event.timeStart || event.timeEnd) {
            scheduleItems.push(...splitItemByDay(item))
        }
    }
    
    for (let i = 0; i < taskOccurences.length; i++) {
        const task = taskOccurences[i]!

        if (!task.startDate) {
            continue
        }

        const startDate = new Date(`${task.startDate.slice(0, 10)}T00:00:00`)
        const timeStart = buildDateFromDateAndTime(task.startDate.slice(0, 10), task.timeStart)
        const timeEnd = buildDateFromDateAndTime(task.startDate.slice(0, 10), task.timeEnd, true)

        const item: ScheduleItem = {
            idItem: `task-${i}`,
            title: task.title,
            type: 'task',
            startDate,
            endDate: null,
            timeStart,
            timeEnd,
            catColor: task.category?.color ?? '',
            style: ''
        }

        if (task.timeStart || task.timeEnd) {
            scheduleItems.push(...splitItemByDay(item))
        }
    }

    return scheduleItems
}

/**
 * Creates an array of ScheduleItem objects from tasks and events that do not have time specified, only date.
 * 
 * @param events    an array with Event objects
 * @param tasks     an array with Task objects
 * @returns         an array of ScheduleItem objects converted from tasks and events
 */
export function generateScheduleItemsAllDay(events: Event[], tasks: Task[]): ScheduleItem[] {
    const scheduleItems: ScheduleItem[] = []
    const taskOccurences: Task[] = generateTasksOccurences(tasks)
    const eventOccurences: Event[] = generateEventsOccurences(events)

    for (let i = 0; i < eventOccurences.length; i++) {
        const event = eventOccurences[i]!
        const startDate = new Date(`${event.dateStart.slice(0, 10)}T00:00:00`)

        if (event.timeStart || event.timeEnd) {
            continue
        }

        scheduleItems.push({
            idItem: `event-${i}`,
            title: event.title,
            type: 'event',
            startDate,
            endDate: new Date(`${event.dateEnd.slice(0, 10)}T00:00:00`),
            timeStart: null,
            timeEnd: null,
            catColor: event.category?.color ?? '',
            style: defineItemStyle(event.category?.color ?? '')
        })
    }
    
    for (let i = 0; i < taskOccurences.length; i++) {
        const task = taskOccurences[i]!

        if (!task.startDate) {
            continue
        }

        const startDate = new Date(`${task.startDate.slice(0, 10)}T00:00:00`)

        if (task.timeStart || task.timeEnd) {
            continue
        }

        scheduleItems.push({
            idItem: `task-${i}`,
            title: task.title,
            type: 'task',
            startDate,
            endDate: null,
            timeStart: null,
            timeEnd: null,
            catColor: task.category?.color ?? '',
            style: defineItemStyle(task.category?.color ?? '')
        })
    }

    return scheduleItems
}

/**
 * Creates an array of ScheduleItem objects for the week from the given date.
 * 
 * @param events    an array with Event objects
 * @param tasks     an array with Task objects
 * @returns         an array of ScheduleItem objects converted from tasks and events
 */
export function getScheduleDaysForWeek(date: Date, events: Event[], tasks: Task[], allDay: boolean): ScheduleDay[] {
    const week: ScheduleDay[] = []
    const items: ScheduleItem[] = allDay ? generateScheduleItemsAllDay(events, tasks) : generateScheduleItems(events, tasks)
    const first = getFirstDayOfWeek(date)
    const today = new Date()

    for (let i = 0; i < 7 ; i++) {
        const nextDay = new Date(first)
        nextDay.setDate(first.getDate() + i)

        const dayItems = items.filter(item =>
            isSameDay(item.startDate, nextDay)
        )

        week.push({
            date: nextDay,
            weekday: nextDay.toLocaleDateString('en-US', {
                weekday: 'short'
            }).toUpperCase(),
            isToday: 
                nextDay.getFullYear() === today.getFullYear()
                && nextDay.getMonth() === today.getMonth()
                && nextDay.getDate() === today.getDate()
                ? true
                : false,
            items: allDay ? dayItems : applyCollisionLayout(dayItems)
            
        })
    }

    return week
}



