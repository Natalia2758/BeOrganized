<script setup lang="ts">
import type { ScheduleDay } from '../types/schedule';
import ScheduleItem from './ScheduleItem.vue';


const props = defineProps<{
    /** Days and their timed items for the displayed schedule period. */
    scheduleDays: ScheduleDay[]
    /** Current day rendered in the mobile single-day layout. */
    selectedDay: ScheduleDay | undefined
}>()


/** Labels the 24 hourly rows shared by each day column. */
const hours = Array.from(
    { length: 24 },
    (_, index) => `${String(index).padStart(2, '0')}:00`
)

/** Determines whether a date should receive the today highlight. */
function isToday(date: Date) {
    const today = new Date()

    return (
        date.getFullYear() === today.getFullYear() &&
        date.getMonth() === today.getMonth() &&
        date.getDate() === today.getDate()
    )
}

</script>

<template>
    <!-- Arranges scheduled items across the selected week's timed slots. -->
    <div class="schedule d-flex flex-column overflow-hidden">
        <div class="schedule__header flex-shrink-0 d-md-grid d-flex justify-content-center">
            <div class="schedule__corner d-none d-md-block"></div>
            <div
                    v-for="day in scheduleDays"
                    :key="day.date.getTime()"
                    class="schedule__weekday d-none d-md-flex flex-column align-items-center justify-content-center"
                    :style="isToday(day.date) ? 'color: var(--app-secondary); font-weight: bold;' : ''"
                >
                    <div>{{ day.weekday }}</div>
                    <div>{{ day.date.getDate() }}</div>
            </div>
            <div
                class="schedule__weekday d-flex d-md-none flex-column align-items-center justify-content-center ps-5"
                :style="selectedDay && isToday(selectedDay.date) ? 'color: var(--app-secondary); font-weight: bold;' : ''"
            >
                <div>{{ selectedDay?.weekday }}</div>
                <div>{{ selectedDay?.date.getDate() }}</div>
            </div>
        </div>

        <div class="schedule__scrollable overflow-y-auto overflow-x-hidden">
            <div class="schedule__grid d-grid">

                <div class="schedule__hours pe-3 pt-2">
                    <div 
                        v-for="hour in hours" 
                        :key="hour"
                        class="schedule__hour text-end"
                    >
                            {{ hour }}
                    </div>
                </div>
                
                <div 
                    v-for="day in scheduleDays"
                    :key="day.date.getTime()"
                    class="schedule__day position-relative d-md-block d-none"
                >
                    <div
                        v-for="hour in hours"
                        :key="hour"
                        class="schedule__cell"
                    >
                    </div>

                    <ScheduleItem
                        v-for="item in day.items"
                        :key="item.idItem"
                        :item="item"
                    />

                </div>
                <div 
                    class="schedule__day position-relative d-block d-md-none"
                >
                    <div
                        v-for="hour in hours"
                        :key="hour"
                        class="schedule__cell"
                    >
                    </div>

                    <ScheduleItem
                        v-for="item in selectedDay?.items"
                        :key="item.idItem"
                        :item="item"
                    />
                </div>
            </div>
        </div>
    </div>
    

</template>
<style lang="css" scoped>

.schedule {

    --cell-height: 60px;
    --header-height: 60px;
    --hours-width: 60px;

    min-height: 0;
    max-height: 70vh;
    flex: 1 1 auto;
}

.schedule__corner {
    height: var(--header-height);
}

.schedule__weekday {
    height: var(--header-height);
}

.schedule__scrollable {
    min-height: 0;
    flex: 1;
}

.schedule__grid {
   
    height: calc(24 * var(--cell-height));
}

.schedule__hours {
    border-right: var(--app-border-subtle);
    min-width: 0;
}

.schedule__hour {
    padding: var(--app-padding-sm);
    font-size: 0.75rem;
    height: var(--cell-height);
}

.schedule__day {
    height: calc(24 * var(--cell-height));
    border-left: var(--app-border-subtle-thin);
    border-right: var(--app-border-subtle-thin);
    min-width: 0;
}

.schedule__cell {
    height: var(--cell-height);
}

@media (max-width: 767.98px) {
    .schedule_header {
        grid-template-columns: var(--hours-width) repeat(1, minmax(0, 1fr));
    }
    .schedule__grid {
        grid-template-columns: var(--hours-width) repeat(1, minmax(0, 1fr));
    }
}

@media (min-width: 768px) {
    .schedule__grid {
        grid-template-columns: var(--hours-width) repeat(7, minmax(0, 1fr));
    }
    .schedule__header {
        grid-template-columns: var(--hours-width) repeat(7, minmax(0, 1fr));
    }
}

</style>