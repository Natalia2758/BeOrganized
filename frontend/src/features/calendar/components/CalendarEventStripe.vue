<script setup lang="ts">
import type { Event } from "@/features/event/types/event";

const props = defineProps<{
    /** Event represented by the calendar marker. */
    event: Event
}>()

/** Uses the event category color when one is assigned. */
function determineColor() {
    if (props.event.category !== null) {
        return 'background: ' + props.event.category.color
    }
}

</script>
<template>
    <!-- Renders a color-coded event marker within a calendar cell. -->
    <div
        class="event-stripe w-100"
        :style="determineColor() ?? 'background-color: var(--app-surface); color: var(--app-surface-text); border: var(--app-border-subtle-thin); '"
    >
        <div class="event-stripe__content w-100">
            {{ props.event.title }}
        </div>
    </div>
</template>

<style lang="css" scoped>
.event-stripe {
    border-radius: 5px;
    padding: 1px;
    text-align: center;
}
.event-stripe__content {
    font-size: x-small;
}

</style>
