<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import CalendarGrid from '../components/CalendarGrid.vue';
import CalendarHeader from '../components/CalendarHeader.vue';
import { useCalendar } from '../composables/useCalendar.ts'
import CalendarEventCard from '../components/CalendarEventCard.vue';
import CalendarDayCard from '../components/CalendarDayCard.vue';
import type { Event } from "@/features/event/types/event";
import { useRouter } from 'vue-router';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import { useNotes } from '@/features/note/composables/useNotes.ts';
import NoteContainer from '@/features/note/components/NoteContainer.vue';

const router = useRouter()

const {
    currentDate,
    calendarDays,
    selectedDay,
    nextMonth,
    previousMonth,
    today
} = useCalendar()

const {
    notes,
    loading: notesLoading,
    error: notesError,
    loadNotesByDate
} = useNotes()

/** Controls which detail panel is visible for the selected calendar date. */
const showDayCard = ref(false)
const showEventCard = ref(false)
const showNotesCard = ref(false)
const selectedEvent = ref<Event | null>(null)
/** Indicates whether any selected-day detail panel should be displayed. */
const hasTodaySection = computed(
    () => showDayCard.value || showEventCard.value || showNotesCard.value
)

const calendarDayEl = ref<HTMLElement | null>(null)

/** Refreshes notes whenever the selected calendar date changes. */
watch(
    currentDate,
    (date) => {
        void loadNotesByDate(date)
    },
    { immediate: true }
)


/** Selects a date and opens its day details. */
function handleDaySelect(date: Date){
    
    showEventCard.value = false
    showDayCard.value = true
    currentDate.value = date
    scroll()
}

/** Scrolls the selected-day section into view when it is open. */
function scroll() {
    if (showDayCard.value === true) {
        calendarDayEl.value?.scrollIntoView({ behavior: 'smooth' })
    }
    
}

/** Opens the details panel for the selected event. */
function showEventDetails(event: Event) {
    showNotesCard.value = false
    showEventCard.value = true
    selectedEvent.value = event
}

/** Opens event creation with the selected date prefilled. */
function showEventForm() {

    router.push({
        name: 'event-creation',
        query: {
            date: currentDate.value.toString()
        }
    })
    
}

/** Toggles the notes panel and hides the event details panel. */
function showNotes() {
    showEventCard.value = false
    showNotesCard.value = !showNotesCard.value
}

</script>
<template>
    <!-- Coordinates calendar navigation, date selection, and day detail panels. -->
    
    <div class="d-flex flex-row justify-content-between align-items-center">
        <button
            class="btn btn-subtle"
            @click="router.back"
        >
            <i class="bi bi-arrow-left"></i>
        </button>
        <h1 class="d-flex gap-4">
            <span>Calendar</span>
            <i class="bi bi-calendar-date"></i>
        </h1>
        <div class="d-flex gap-2">
            <button
                class="btn btn-secondary d-none d-md-block"
                @click="today"
            >
                Today
            </button>
            <button
                class="calendar-top__button btn btn-primary"
                type="button"
                @click="showEventForm"
            >
                <span class="d-none d-md-inline">Add event</span>
                <span class="d-inline d-md-none"><i class="bi bi-plus-lg"></i></span>
            </button>
        </div>
    </div>
    
    <div
        class="calendar-layout d-block d-lg-flex flex-row gap-2 gap-xl-5"
        :class="{ 'calendar-layout--with-today': hasTodaySection }"
    >
        
    
        <div class="align-items-stretch w-100">
            <CommonContainer :margin-bottom="true" class="calendar w-100">
                <template #1>
                    <CalendarHeader
                        :current-date="currentDate"
                        @previous="previousMonth"
                        @next="nextMonth"
                    />

                    <CalendarGrid
                        :days="calendarDays"
                        :selected-day="selectedDay"
                        :selected-date="currentDate"
                        @select-date="handleDaySelect($event)"
                    />
                </template>
            </CommonContainer>
        </div>       

        <div v-if="hasTodaySection" class="today d-block d-lg-flex flex-column" ref="calendarDayEl">
            <CommonContainer :margin-bottom="true" v-if="showDayCard && selectedDay">
                <template #1>
                    <CalendarDayCard
                        :calendar-day="selectedDay"
                        @select-event="showEventDetails"
                        @show-notes="showNotes"
                    />
                </template>
            </CommonContainer>
            <CalendarEventCard
                v-if="showEventCard && selectedEvent"
                :event="selectedEvent"
            />      
                
            <CommonContainer :margin-bottom="true" v-if="showNotesCard && selectedDay">
                <template #1>
                    <RouterLink
                        :to="{ name: 'note-creation', query: { date: selectedDay.date.toISOString().slice(0, 10) }}"
                        class="btn btn-primary mb-3 "
                    >
                        Add note
                    </RouterLink>
                    <div v-if="notes.length === 0">No notes on this day</div>
                    <div v-else-if="notesError" class="alert alert-danger"></div>
                    <div v-else-if="notesLoading">Loading notes...</div>
                    <NoteContainer
                        v-else
                        :notes="notes"
                    />
                    
                </template>
                
            </CommonContainer>
        </div>
    </div>  
</template>

<style scoped>
.calendar-layout {
    gap: var(--app-gap-lg);
    align-items: stretch;
}

.calendar {
    overflow: hidden;
    flex: 1 1 100%;
}

@media (max-width: 767.98px) {
    .calendar-layout {
        flex-direction: column;
        gap: var(--app-gap-sm);
    }

    .today {
        flex-basis: auto;
        min-width: 0;
    }
}

@media (min-width: 768px) {
    .calendar-layout--with-today .calendar {
        flex-basis: 70%;
    }

    .today {
        flex: 0 0 30%;
        min-width: 20%;
    }
    .calendar-top__button {
        flex: 0 0 30%;
    }
}
</style>
