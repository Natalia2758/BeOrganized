<script setup lang="ts">
import { type Event } from '../types/event'
import { RouterLink } from 'vue-router'
import CategoryDot from '@/features/category/components/CategoryDot.vue';
import { formatDateForDisplay } from '@/common/utils/dateTimeUtils';

const props = defineProps<{
    /** Event represented by this list item. */
    event: Event
    /** Uses a compact date display in dashboard previews. */
    forDashboard?: boolean
}>()

const emit = defineEmits<{
    /** Requests deletion of this event from the parent view. */
    deleteRequest: [event: Event]
}>()

/** Formats the end-date suffix, abbreviated in dashboard previews. */
function dateEnd() {
    return props.event.dateEnd && props.event.dateEnd !== props.event.dateStart ? (props.forDashboard ? ' - ...' : (' - ' + formatDateForDisplay(props.event.dateEnd)))  : ''
}
</script>

<template>
    <!-- Displays an event row and emits deletion requests. -->
    <li
        class="list-group-item d-flex align-items-center py-2 gap-1 gap-lg-2 w-100"
        :class="props.forDashboard ? 'event--for-dashboard' : 'event'"
    >
        
        <div class="event__side--start d-flex">
            <div class="event__date">{{ formatDateForDisplay(event.dateStart) + dateEnd() }}</div>
        </div>
        
        <div class="event__title d-flex flex-row gap-1 align-items-center justify-content-center">
            <RouterLink
                class="link-offset-3 link-opacity-75-hover link-underline link-underline-opacity-0 link-underline-opacity-50-hover mt-1" 
                :to="{
                    name: 'event-details',
                    params: {
                        idEvent: event.idEvent
                    }
                }"
            >
                <h5>{{ event.title }}</h5>
                
            </RouterLink>
            <CategoryDot
                :color="event.category?.color ?? null"
                :size="'md'"
            />
        </div>
            
        <div class="event__side--end d-none d-md-flex justify-content-end">
            <button
                type="button"
                class="event__delete btn btn-lg link-danger link-opacity-75-hover"
                @click="emit('deleteRequest', props.event)">
                <i class="bi bi-trash-fill"></i>
            </button>
        </div>
    </li>

</template>
<style lang="css" scoped>

.event--for-dashboard {
    font-size: medium;
}

.event__title {
    flex: 1 1 60%;
}

.event__side--start {
    justify-content: flex-start;
    flex: 1 0 20%;
}

.event__side--end {
    justify-content: flex-end;
    flex: 0 0 20%;
}

.event__date {
    color: var(--app-text-muted);
    white-space: nowrap;
    overflow: hidden;
}

@media (max-width: 768px) {
    .event__side--start {
        justify-content: flex-start;
        flex: 0 0 40%;
        flex-wrap: wrap;
    }
    .event__date {
        overflow: visible;
        white-space: wrap;
        flex-wrap: wrap;
    }
}

</style>