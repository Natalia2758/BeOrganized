import { describe, expect, it } from 'vitest'
import type { Event } from '@/features/event/types/event'
import type { Task } from '@/features/task/types/task'
import { calculateItemPosition, generateScheduleItems, generateScheduleItemsAllDay, getFirstDayOfWeek, getScheduleDaysForWeek } from '../utils/scheduleUtils'

function createEvent(overrides: Partial<Event> = {}): Event {
    return {
        idEvent: 1,
        title: 'Meeting',
        details: null,
        category: { idCategory: 1, name: 'Work', color: 'red' },
        dateStart: '2026-05-13',
        dateEnd: '2026-05-13',
        duration: false,
        timeStart: '10:00',
        timeEnd: '11:00',
        repeated: false,
        repeatedFrequency: null,
        ...overrides
    }
}

function createTask(overrides: Partial<Task> = {}): Task {
    return {
        idTask: 1,
        title: 'Task',
        description: null,
        priority: null,
        category: { idCategory: 2, name: 'Personal', color: 'blue' },
        startDate: '2026-05-13',
        duration: false,
        timeStart: '12:00',
        timeEnd: '13:00',
        repeated: false,
        repeatedFrequency: null,
        progress: 0,
        finished: false,
        subtasks: [],
        ...overrides
    }
}

describe('scheduleUtils', () => {
    it('gets the first day of the week', () => {
        const date = new Date(2026, 4, 13) // Wednesday

        const result = getFirstDayOfWeek(date)

        expect(result).toEqual(new Date(2026, 4, 11)) // Monday
        expect(date).toEqual(new Date(2026, 4, 13))
    })

    it('calculates item position', () => {
        const start = new Date(2026, 4, 13, 10, 0)
        const end = new Date(2026, 4, 13, 11, 30)

        const result = calculateItemPosition(start, end)

        expect(result).toEqual({
            itemTop: 600,
            itemHeight: 90
        })
    })

    it('generates schedule items from events and tasks', () => {
        const result = generateScheduleItems([createEvent()], [createTask()])

        expect(result).toHaveLength(2)

        const eventItem = result[0]!
        expect(eventItem).toMatchObject({
            idItem: 'event-0',
            title: 'Meeting',
            type: 'event',
            startDate: new Date('2026-05-13'),
            endDate: new Date('2026-05-13'),
            catColor: 'red',
            style: 'top: 600px; height: 60px; background-color: red;'
        })
        expect(eventItem.timeStart).toBeInstanceOf(Date)
        expect(eventItem.timeStart?.getHours()).toBe(10)
        expect(eventItem.timeStart?.getMinutes()).toBe(0)
        expect(eventItem.timeEnd?.getHours()).toBe(11)
        expect(eventItem.timeEnd?.getMinutes()).toBe(0)

        const taskItem = result[1]!
        expect(taskItem).toMatchObject({
            idItem: 'task-0',
            title: 'Task',
            type: 'task',
            startDate: new Date('2026-05-13'),
            endDate: null,
            catColor: 'blue',
            style: 'top: 720px; height: 60px; background-color: blue;'
        })
        expect(taskItem.timeStart?.getHours()).toBe(12)
        expect(taskItem.timeStart?.getMinutes()).toBe(0)
        expect(taskItem.timeEnd?.getHours()).toBe(13)
        expect(taskItem.timeEnd?.getMinutes()).toBe(0)
    })

    it('generates only all-day events and tasks', () => {
        const allDayEvent = createEvent({
            title: 'Holiday',
            timeStart: null,
            timeEnd: null
        })
        const allDayTask = createTask({
            title: 'Remember',
            timeStart: null,
            timeEnd: null
        })

        const result = generateScheduleItemsAllDay(
            [allDayEvent, createEvent()],
            [allDayTask, createTask()]
        )

        expect(result).toHaveLength(2)
        expect(result[0]).toMatchObject({
            title: 'Holiday',
            type: 'event',
            idItem: 'event-0',
            startDate: new Date('2026-05-13'),
            endDate: new Date('2026-05-13'),
            catColor: 'red',
            timeStart: null,
            timeEnd: null,
            style: 'background-color: red;'
        })
        expect(result[1]).toMatchObject({
            title: 'Remember',
            type: 'task',
            idItem: 'task-0',
            startDate: new Date('2026-05-13'),
            endDate: null,
            catColor: 'blue',
            timeStart: null,
            timeEnd: null,
            style: 'background-color: blue;'
        })
    })

    it('creates 7 days for a week', () => {
        const date = new Date(2026, 4, 13)

        const result = getScheduleDaysForWeek(
            date,
            [],
            [],
            false
        )

        expect(result).toHaveLength(7)
        expect(result[0]!.date).toEqual(new Date(2026, 4, 11))
        expect(result[6]!.date).toEqual(new Date(2026, 4, 17))
        expect(result.map(day => day.weekday)).toEqual([
            'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'
        ])
        expect(result.every(day => Array.isArray(day.items))).toBe(true)
    })
})