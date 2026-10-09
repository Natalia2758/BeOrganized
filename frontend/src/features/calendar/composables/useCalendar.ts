import { computed, onMounted, ref } from "vue";
import { toCalendarDaysType } from "../utils/calendarDateUtils";
import { useEvents } from "@/features/event/composables/useEvents";
import { isSameDay } from "@/common/utils/dateTimeUtils";

/** Provides calendar dates, events, and month/day navigation. */
export function useCalendar() {

    const currentDate = ref(new Date())

    const {
        events,
        error: eventsError,
        loading: eventsLoading,
        loadEvents
    } = useEvents()

    /** Loads events once so the calendar can populate its day cells. */
    onMounted(() => {
        void loadEvents()
    })

    /** Builds the visible month grid from the current date and events. */
    const calendarDays = computed(() => {
        return toCalendarDaysType(currentDate.value, events.value ?? [])
    })

    /** Resolves the calendar cell matching the selected date. */
    const selectedDay = computed(() => {
        return calendarDays.value.find(
            day => isSameDay(day.date, currentDate.value)
        )
    })

    /** Moves the calendar to the first day of the next month. */
    function nextMonth() {
        currentDate.value = new Date(
            currentDate.value.getFullYear(),
            currentDate.value.getMonth() + 1,
            1
        )
    }

    /** Moves the calendar to the first day of the previous month. */
    function previousMonth() {
        currentDate.value = new Date(
            currentDate.value.getFullYear(),
            currentDate.value.getMonth() - 1,
            1
        )
    }

    /** Selects the current date. */
    function today() {
        currentDate.value = new Date()
    }

    /** Moves the selected date back one day. */
    function previousDay() {
        const date = new Date(currentDate.value)
        date.setDate(date.getDate() - 1)
        currentDate.value = date
    }

    /** Moves the selected date forward one day. */
    function nextDay() {
        const date = new Date(currentDate.value)
        date.setDate(date.getDate() + 1)
        currentDate.value = date
    }

    return {
        currentDate,
        calendarDays,
        selectedDay,
        eventsLoading,
        eventsError,
        nextMonth,
        previousMonth,
        today,
        previousDay,
        nextDay
    }
}