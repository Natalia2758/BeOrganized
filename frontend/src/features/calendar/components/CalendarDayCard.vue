<script setup lang="ts">
import type { CalendarDay } from '../types/calendar.ts';
import CalendarEventItem from './CalendarEventItem.vue';
import type { Event } from "@/features/event/types/event";
import { computed } from 'vue';

const props = defineProps<{
    /** Selected calendar day and its events. */
    calendarDay: CalendarDay
}>()

const emit = defineEmits<{
    /** Requests display of the selected event's details. */
    selectEvent: [event: Event]
    /** Requests opening the event creation form for this day. */
    addEvent: []
    /** Toggles the notes panel for this day. */
    showNotes: []
}>()

/** Formats the selected date for the day-card heading. */
const dateString = computed(() => {
    return props.calendarDay.date.toLocaleDateString()
})

</script>
<template>
    <!-- Summarizes the selected calendar day and exposes its event and note actions. -->
    
    <h3 class="day-card__date">{{ dateString }}</h3>
    
    <div>
        <!-- list of all events on this day-->
        <ul class="list-group list-group-flush mb-5">
            <CalendarEventItem
                v-for="event in calendarDay.events"
                :key="event.idEvent"
                :event="event"
                @select-event="emit('selectEvent', $event)" 
            />
        </ul>
    </div>
    <button
        class="btn btn-outline-secondary"
        data-bs-toggle="button"
        @click="emit('showNotes')"
    >
        Notes
    </button>
</template>
<style scoped>
.day-card__date {
    color: var(--app-text-muted);
}
</style>