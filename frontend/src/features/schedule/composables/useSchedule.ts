import { useEvents } from "@/features/event/composables/useEvents";
import { useTasks } from "@/features/task/composables/useTasks";
import { computed, onMounted, ref, watch } from "vue";
import { getScheduleDaysForWeek } from "../utils/scheduleUtils";
import { useTaskListReloadToken } from "@/features/task/composables/useTasks";
import { isSameDay } from "@/common/utils/dateTimeUtils";

/** Combines events and tasks into a navigable schedule. */
export function useSchedule() {

    const currentDate = ref(new Date())
    const error = ref<string | null>(null)

    const {
        events,
        loadEvents
    } = useEvents()

    const {
        tasks,
        loadTasks
    } = useTasks()

    const taskListReloadToken = useTaskListReloadToken()

    /** Loads events when the schedule is first displayed. */
    onMounted(async() => {
        await loadEvents()
    })

    /** Refreshes schedule tasks after a task-list mutation. */
    watch(taskListReloadToken, async () => {
        await loadTasks()
    }, { immediate: true })

    /** Builds schedule days containing timed tasks and events. */
    const scheduleDaysWithTime = computed(() => {
        return getScheduleDaysForWeek(currentDate.value, events.value, tasks.value, false)
    })

    /** Builds schedule days containing all-day tasks and events. */
    const scheduleDaysAllDay = computed(() => {
        return getScheduleDaysForWeek(currentDate.value, events.value, tasks.value, true)
    })

    /** Advances the schedule by one week. */
    function nextWeek() {
        const date = new Date(currentDate.value)
        date.setDate(date.getDate() + 7)
        currentDate.value = date
    }

    /** Moves the schedule back by one week. */
    function previousWeek() {
        const date = new Date(currentDate.value)
        date.setDate(date.getDate() - 7)
        currentDate.value = date
    }

    /** Selects the current date in the schedule. */
    function goToToday() {
        currentDate.value = new Date()
    }

    /** Resolves the selected date in the timed schedule. */
    const selectedDate = computed(() => {
        return scheduleDaysWithTime.value.find(
            day => isSameDay(day.date, currentDate.value)
        )
    })

    /** Resolves the selected date in the all-day schedule. */
    const selectedDateAllDay = computed(() => {
        return scheduleDaysAllDay.value.find(
            day => isSameDay(day.date, currentDate.value)
        )
    })

    /** Moves the schedule back by one day. */
    function previousDay() {
        const date = new Date(currentDate.value)
        date.setDate(date.getDate() - 1)
        currentDate.value = date
    }

    /** Advances the schedule by one day. */
    function nextDay() {
        const date = new Date(currentDate.value)
        date.setDate(date.getDate() + 1)
        currentDate.value = date
    }

    return {
        currentDate,
        scheduleDaysWithTime,
        scheduleDaysAllDay,
        selectedDate,
        selectedDateAllDay,
        nextWeek,
        previousWeek,
        nextDay,
        previousDay,
        goToToday,
        error
    }
}