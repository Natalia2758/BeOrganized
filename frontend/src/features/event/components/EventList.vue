<script setup lang="ts">
import { computed } from 'vue';
import { type Event } from '../types/event';
import EventListItem from './EventListItem.vue';

const props = defineProps<{
    /** Events to display. */
    events: Event[]
    /** Optional maximum number of entries to show. */
    number?: number
    /** Enables the compact dashboard presentation for each item. */
    forDashboard: boolean
}>()

const emit = defineEmits<{
    /** Forwards an event deletion request to the parent. */
    deleteRequest: [event: Event]
}>()

/** Applies the optional item limit used by dashboard previews. */
const displayedEvents = computed(() => {
    return props.number ? props.events.slice(0, props.number) : props.events
})

</script>

<template>
    <!-- Renders the event collection and forwards item actions. -->
    <ul class="list-group list-group-flush">
        <EventListItem
            v-for="event in displayedEvents"
            :key="event.idEvent"
            :event="event"
            :for-dashboard="props.forDashboard"
            @delete-request="emit('deleteRequest', $event)"
        />
    </ul>
</template>