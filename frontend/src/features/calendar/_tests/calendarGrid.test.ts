import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import CalendarGrid from '../components/CalendarGrid.vue'
import type { CalendarDay } from '../types/calendar'

const selectedDate = new Date(2026, 9, 12)
const outsideMonthDate = new Date(2026, 8, 30)

const days: CalendarDay[] = [
    {
        date: selectedDate,
        dayNumber: 14,
        weekDay: 0,
        isCurrentMonth: true,
        isToday: false,
        events: []
    },
    {
        date: outsideMonthDate,
        dayNumber: 2,
        weekDay: 1,
        isCurrentMonth: false,
        isToday: false,
        events: []
    }
]

describe('CalendarGrid', () => {
    it('renders weekday headings and the provided days', () => {
        const wrapper = mount(CalendarGrid, {
            props: {
                days,
                selectedDay: days[0],
                selectedDate
            }
        })

        expect(wrapper.findAll('.calendar__weekday').map(day => day.text())).toEqual([
            'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'
        ])
        expect(wrapper.findAll('.calendar-day')).toHaveLength(2)
        expect(wrapper.findAll('.calendar-day').map(day => day.text().trim())).toEqual([
            '12', '30'
        ])
    })

    it('marks the selected date and outside-month day', () => {
        const wrapper = mount(CalendarGrid, {
            props: {
                days,
                selectedDay: days[0],
                selectedDate
            }
        })
        const [selectedCell, outsideMonthCell] = wrapper.findAll('.calendar-day')

        expect(selectedCell!.classes()).toContain('calendar-day--selected')
        expect(selectedCell!.classes()).not.toContain('calendar-day--other-month')
        expect(outsideMonthCell!.classes()).toContain('calendar-day--other-month')
        expect(outsideMonthCell!.classes()).not.toContain('calendar-day--selected')
    })

    it('emits the clicked day date', async () => {
        const wrapper = mount(CalendarGrid, {
            props: {
                days,
                selectedDay: days[0],
                selectedDate
            }
        })

        await wrapper.findAll('.calendar-day')[1]!.trigger('click')

        expect(wrapper.emitted('selectDate')).toEqual([[outsideMonthDate]])
    })
})
