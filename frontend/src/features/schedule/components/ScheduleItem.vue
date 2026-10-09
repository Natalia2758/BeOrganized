<script setup lang="ts">
import type { ScheduleItem } from '../types/schedule';

defineProps<{
/** Task or event positioned in a schedule day. */
item: ScheduleItem
/** Renders the item in the all-day row instead of a timed slot. */
allDay?: boolean
}>()

</script>
<template>
    <!-- Displays one scheduled task or event. -->
    <div
        class="schedule-item p-2 rounded-1"
        :class="allDay ? 'schedule-item--all-day' : ''"
        :style="item.style"
    >
        <div class="fw-semibold">
            {{ item.title }}
        </div>
        <div v-if="!allDay" class="schedule-item__time">
            {{ 
                item.timeStart?.toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    minute: '2-digit'
                })
            }}
             - 
            {{ 
                item.timeEnd?.toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    minute: '2-digit'
                })
            }}
        </div>
    </div>
</template>

<style lang="css" scoped>
.schedule-item {
    position: absolute;
    left: 4px;
    right: 4px;
    overflow: hidden;

    font-size: 0.8rem;
    box-shadow: var(--app-shadow-encircle);
}

.schedule-item--all-day {
    position: static;
    margin: 4px;
}

.schedule-item__time {
    color: var(--app-text-muted);
}
</style>