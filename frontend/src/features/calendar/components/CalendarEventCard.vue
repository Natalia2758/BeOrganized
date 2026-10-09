<script setup lang="ts">
import EventContainer from "@/features/event/components/EventContainer.vue";
import type { Event } from "@/features/event/types/event";

const props = defineProps<{
    /** Event selected from the calendar. */
    event: Event
}>()
</script>
<template>
    <!-- Displays the selected event's details from the calendar. -->
    <EventContainer :event="props.event" />
</template>