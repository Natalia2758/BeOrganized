<script setup lang="ts">
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import ScheduleGrid from '../components/ScheduleGrid.vue';
import { useSchedule } from '../composables/useSchedule.ts';
import ScheduleHeader from '../components/ScheduleHeader.vue';
import { computed } from 'vue';
import ScheduleGridAllDay from '../components/ScheduleGridAllDay.vue';
import { useRouter } from 'vue-router';

const router = useRouter()

/** Supplies schedule data and navigation actions for the current period. */
const {
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
} = useSchedule()

/** First visible date in the weekly schedule. */
const weekStart = computed(() => {
    return scheduleDaysWithTime.value.at(0)?.date ?? null
})

/** Last visible date in the weekly schedule. */
const weekEnd = computed(() => {
    return scheduleDaysWithTime.value.at(scheduleDaysWithTime.value.length - 1)?.date ?? null
})

</script>
<template>
    <!-- Builds and displays the schedule for the currently selected period. -->
    <div class="schedule-view d-flex flex-column gap-3">
        <div class="d-flex gap-5 w-100 align-items-center">
            <button
                class="btn btn-subtle"
                @click="router.back"
            >
                <i class="bi bi-arrow-left"></i>
            </button> 
            <h1 class="mb-0 d-flex gap-4">
                <span>Schedule</span>
                <i class="bi bi-calendar-week"></i>
            </h1>
        </div>
            
        <CommonContainer class="schedule-view__panel" :margin-bottom="true">
            <template #1>
                <ScheduleHeader
                    @next-week="nextWeek()"
                    @previous-week="previousWeek()"
                    @next-day="nextDay()"
                    @previous-day="previousDay()"
                    :week-start="weekStart"
                    :week-end="weekEnd"
                    :today="currentDate"
                />
            </template>
        </CommonContainer>
        
        <CommonContainer class="schedule-view__panel schedule-view__panel--grid" :margin-bottom="false">
            <template #1>
                <ScheduleGrid
                    :schedule-days="scheduleDaysWithTime"
                    :selected-day="selectedDate"
                />
            </template>
        </CommonContainer>
        <CommonContainer class="schedule-view__panel" :margin-bottom="true">
            <template #1>
                <ScheduleGridAllDay
                    :schedule-days="scheduleDaysAllDay"
                    :selected-day="selectedDateAllDay"
                />
            </template>
        </CommonContainer>
    </div>
</template>
<style lang="css" scoped>
.schedule-view {
    min-height: 0;
}

.schedule-view__panel {
    min-height: 0;
}

.schedule-view__panel--grid {
    flex: 1 1 auto;
}

</style>