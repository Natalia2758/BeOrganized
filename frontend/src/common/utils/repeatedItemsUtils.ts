import type { Event } from "@/features/event/types/event"
import type { Task } from "@/features/task/types/task"

/**
 * Converts a Date object into a date-only string in YYYY-MM-DD format.
 * @param date  a Date object to be converted
 * @returns     a formatted date string
 */
export function getDateKey(date: Date): string {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}-${month}-${day}`
}

/**
 * Adds whole calendar days to an ISO date string and returns the resulting date key.
 * @param dateKey   a date string
 * @param days      a date difference
 * @returns         new date with added calendar days
 */
export function addDaysToDateKey(dateKey: string, days: number): string {
    const date = new Date(`${dateKey}T00:00:00`)

    if (Number.isNaN(date.getTime())) {
        return dateKey
    }

    date.setDate(date.getDate() + days)

    return getDateKey(date)
}

/**
 * Returns an end date for the event
 * 
 * @param event         an event for which the date is calcualted
 * @param startString   a start date of the event as string type
 * @returns             event end date
 */
export function getEndDate(event: Event, startString: string) {
    const startDateKey = event.dateStart.slice(0, 10)
    const endDateKey = event.dateEnd.slice(0, 10)

    if (startDateKey === endDateKey) {
        return startString.slice(0, 10)
    }

    const start = new Date(`${startDateKey}T00:00:00`)
    const end = new Date(`${endDateKey}T00:00:00`)

    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
        return startString.slice(0, 10)
    }

    const differenceInDays = Math.round((end.getTime() - start.getTime()) / 86400000)

    return addDaysToDateKey(startString.slice(0, 10), differenceInDays)
}

/**
 * Generates an array of all event occurences, including events that are repeated cyclically.
 * 
 * @param events    events array not including cyclical events
 * @returns         an array of Event objects with all event occurrences
 */
export function generateEventsOccurences(events: Event[]): Event[] {
    const occurences: Event[] = []

    events.forEach((event) => {
        if (event.repeated && event.dates) {
            for (const date of event.dates) {

                const endDate = getEndDate(event, date)
                const eventOccurence: Event = {
                    idEvent: event.idEvent,
                    title: event.title,
                    details: event.details,
                    category: event.category,
                    dateStart: date,
                    dateEnd: endDate,
                    duration: event.duration,
                    timeStart: event.timeStart,
                    timeEnd: event.timeEnd,
                    repeated: event.repeated,
                    repeatedFrequency: event.repeatedFrequency,
                    dates: null
                }
                occurences.push(eventOccurence)
            }
        } else {
            occurences.push(event)
        }
    }) 
    return occurences
}

/**
 * Generates an array of all task occurences, including tasks that are repeated cyclically.
 * 
 * @param tasks     tasks array not including cyclical tasks
 * @returns         an array of Task objects with all task occurrences
 */
export function generateTasksOccurences(tasks: Task[]): Task[] {
    const occurences: Task[] = []

    tasks.forEach((task) => {
        if (task.repeated && task.dates) {
            for (const date of task.dates) {
                const taskOccurence: Task = {
                    idTask: task.idTask,
                    title: task.title,
                    description: task.description,
                    category: task.category,
                    priority: task.priority,
                    startDate: date,
                    duration: task.duration,
                    timeStart: task.timeStart,
                    timeEnd: task.timeEnd,
                    repeated: task.repeated,
                    repeatedFrequency: task.repeatedFrequency,
                    dates: null,
                    progress: task.progress,
                    finished: task.finished,
                    subtasks: task.subtasks
                }
                occurences.push(taskOccurence)
            }
        } else {
            occurences.push(task)
        }
    })

    return occurences
}