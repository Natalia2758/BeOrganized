<script setup lang="ts">
import type { CalendarDay } from '../types/calendar.ts';
import CalendarEventStripe from './CalendarEventStripe.vue';
const props = defineProps<{
    /** Day data for the cell, absent for padding cells. */
    day: CalendarDay | undefined
    /** Whether this date is currently selected. */
    selected?: boolean
}>()

const emit = defineEmits<{
    /** Emits the date when the user selects this cell. */
    selectDate: [date: Date]
}>()

/** Builds CSS state classes for adjacent-month, today, and selected dates. */
function determineDayStyle() {
    if (!props.day) return

        return [!props.day.isCurrentMonth ? 'calendar-day--other-month' : '',
        props.day.isToday ? 'calendar-day--today' : '',
        props.selected ? 'calendar-day--selected' : ''
    ].filter(Boolean).join(' ') 
}

/** Emits the date only when this cell represents a calendar day. */
function selectDay() {
    if (props.day) {
        emit('selectDate', props.day.date)
    }
}
</script>
<template>
    <!-- Renders one day cell in the calendar grid and emits date selection. -->
    <div 
        class="calendar-day p-0 p-md-2"
        :class="determineDayStyle()"
        @click="selectDay"
    >
        <div
            class="calendar-day__date fw-bolder fs-md-6"
        >
            {{ props.day ? props.day.date.getDate() : '' }}
        </div>
        <div class="calendar-day__events d-flex flex-wrap align-items-center my-2 justify-content-between">
            <CalendarEventStripe
                v-for="event in props.day?.events"
                :key="event.idEvent"
                :event="event"
            />
        </div>
    </div>
</template>

<style scoped>

.calendar-day {
    border: var(--app-border-subtle);
    cursor: pointer;
    border-radius: var(--app-radius-sm);
    
        &:hover {
            background-color: var(--app-secondary-subtle);
            transform: translateY(-1px);
        }
        &:active {
            background: var(--app-secondary-hover);
        }
}

.calendar-day--other-month {
    color: var(--app-text-muted);
    background-color: var(--app-surface-muted);
    pointer-events: none;
}

.calendar-day--today {
    border-color: var(--app-secondary);
}

.calendar-day--today .calendar-day__date {
    color: var(--app-secondary);
}

.calendar-day--selected {
    background: var(--app-secondary-subtle);
}

@media (max-width: 767.98px) {
    .calendar-day {
        width: 100%;
        min-height: 80px;
        max-height: 80px;
        overflow: hidden;
    }
}

@media (min-width: 768px) {
    .calendar-day {
        min-height: 100px;
    }
    .calendar-day__events {
        gap: var(--app-gap-sm);
    }
}
</style>