<script setup lang="ts">
import CategoryDot from "@/features/category/components/CategoryDot.vue";
import type { Event } from "@/features/event/types/event";

const props = defineProps<{
    /** Event represented in the selected day's event list. */
    event: Event
}>()

const emit = defineEmits<{
    /** Requests that the parent display this event's details. */
    selectEvent: [event: Event]
}>()

</script>
<template>
    <!-- Displays an event entry in the calendar day view. -->
    <li class="list-group-item">
        <button
            type="button"
            class="btn btn-link link-offset-3 link-opacity-75-hover link-underline link-underline-opacity-0 "
            @click="emit('selectEvent', props.event)"
        >
            <div class="d-flex flex-row align-items-center gap-1">
                <h5>{{ props.event.title }}</h5>
                <CategoryDot
                    :color="props.event.category?.color ?? null"
                    :size="'md'"
                />
            </div>
            
        </button>
    </li>
    
</template>