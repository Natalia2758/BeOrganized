<script setup lang="ts">
import type { CalendarDay } from '../types/calendar.ts';
import CalendarDayCell from './CalendarDayCell.vue';
import { isSameDay } from '@/common/utils/dateTimeUtils.ts';
import { getDateKey } from '@/common/utils/repeatedItemsUtils';

/** Calendar data and current selection supplied by CalendarView. */
const props = defineProps<{
    days: CalendarDay[]
    selectedDay: CalendarDay | undefined
    selectedDate: Date
}>()

/** Sends selected dates back to CalendarView. */
const emit = defineEmits<{
    selectDate: [date: Date]
}>()
/** Weekday labels ordered from Monday through Sunday. */
const weekDays = [
    'MON',
    'TUE',
    'WED',
    'THU',
    'FRI',
    'SAT',
    'SUN'
]
</script>
<template>
    <!-- Arranges calendar day cells into a selectable month grid. -->
    <div>
        <div class="calendar__weekdays d-grid my-1 my-md-2">
            <div
                class="calendar__weekday d--block p-2 text-center"
                v-for="day in weekDays"
                :key="day"
            >
                {{ day }}
            </div>
        </div>
        
        <div class="calendar__grid d-grid">
            <CalendarDayCell
                v-for="day in days"
                :key="getDateKey(day.date)"
                :day="day"
                :selected="isSameDay(day.date, selectedDate)"
                @select-date="emit('selectDate', $event)"
            />
        </div>
    </div>
</template>

<style scoped>

.calendar__weekdays {
    width: 100%;
    grid-template-columns: repeat(7, minmax(0, 1fr));
}

.calendar__weekday {
    width: 100%;
    font-weight: 700;
    color: var(--app-text-muted)
}

.calendar__grid {
    width: 100%;
    grid-template-columns: repeat(7, minmax(0, 1fr));
}

@media (min-width: 768px) {
    .calendar__grid {
        gap: var(--app-gap-sm);
    }
}
</style>