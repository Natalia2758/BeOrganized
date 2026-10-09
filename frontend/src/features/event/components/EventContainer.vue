<script setup lang="ts">
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import type { Event } from '../types/event';
import { formatDateForDisplay, formatTimeForDisplay } from '@/common/utils/dateTimeUtils';

const props = defineProps<{
    /** Event whose details are displayed. */
    event: Event
}>()

</script>

<template>
    <div>
        <!-- Displays the details of a single event. -->
        <CommonContainer :margin-bottom="true">
            <template #1>
                <dl>
                    <dt>Details</dt><dd>{{ props.event.details ?? 'No details' }}</dd>
                    <dt>Date</dt><dd>{{props.event.dateStart === props.event.dateEnd ? formatDateForDisplay(props.event.dateStart) : formatDateForDisplay(props.event.dateStart) + ' - ' + formatDateForDisplay(props.event.dateEnd) }}</dd>
                    <dt v-if="props.event.duration">Time</dt><dd v-if="props.event.duration">{{ props.event.timeStart ? (!props.event.timeEnd ? formatTimeForDisplay(props.event.timeStart) : formatTimeForDisplay(props.event.timeStart) + ' - ' + formatTimeForDisplay(props.event.timeEnd)) : '' }}</dd>
                    <dt v-if="props.event.repeated">Repeated</dt><dd v-if="props.event.repeated">{{ props.event.repeatedFrequency }}</dd>
                </dl>
            </template>
        </CommonContainer>
    </div>
</template>