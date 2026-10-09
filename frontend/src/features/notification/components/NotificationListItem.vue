<script setup lang="ts">
import { computed } from 'vue';
import { NotificationStatus, type Notification } from '../types/notification';
import { ReminderType } from '@/features/reminder/types/reminder';
import { formatDateTimeForDisplay } from '@/common/utils/dateTimeUtils';


const props = defineProps<{
    /** Notification represented by this list item. */
    notification: Notification
}>()

const emit = defineEmits<{
    /** Requests that the parent update the notification as opened. */
    'open': [Notification]
    /** Requests that the parent close the notification panel. */
    'close': []
}>()

/** Reminder linked to this notification. */
const reminder = computed(() => props.notification.reminder)

/** Reminder type label, or null for reminders without a target type. */
const type = computed(() => {
    if (reminder.value.type !== ReminderType.NONE){
        return reminder.value.type.toString()
    } else {
        return null
    }   
})

/** Resolves the destination associated with the reminder target. */
function determineRedirection() {
    if (reminder.value.type === ReminderType.EVENT) {
        return 'events/' + reminder.value.idEvent?.toString()
    } else if (reminder.value.type === ReminderType.TASK) {
        return 'tasks/' + reminder.value.idTask?.toString()
    } else {
        return '/reminders'
    }
}

/** Indicates whether this notification has already been read. */
function wasOpened() {
    return props.notification.status === NotificationStatus.OPENED ? true : false
}

</script>

<template>
    <!-- Displays a notification and emits actions to open or close its details. -->
    <li class="d-flex align-items-center justify-content-between gap-2">
        <div class="d-flex align-items-center gap-4">
            <div
                v-if="!wasOpened()"
                class="notification__dot"
            >
            </div>
            <div class="notification__date">{{ formatDateTimeForDisplay(notification.sentTime) }}</div>
        </div>
        
        <RouterLink
            class="text-center link-offset-3 link-opacity-75-hover link-underline link-underline-opacity-0 link-underline-opacity-50-hover flex-grow-1"
            :class="wasOpened() ? 'notification__title--read' : 'notification__title--unread'"
            :to="{ path: determineRedirection() }"
            @click="emit('open', props.notification); emit('close')"
        >
            {{ notification.reminder.content }}
        </RouterLink>
        <h5
            v-if="type !== null"
            class="notification__type d-none d-md-block"
        >{{ type }}</h5>
    </li>
</template>

<style scoped>
.notification__dot {
    background-color: var(--app-secondary);
    box-shadow: var(--app-shadow-encircle);
    border-radius: 50%;
    width: 0.5rem;
    height: 0.5rem;
}

.notification__date {
    color: var(--app-text-muted);
    min-width: fit-content;
}

.notification__title--read {
    color: var(--app-text-muted);
}
.notification__title--unread {
    font-weight: bold;
    color: var(--app-text);
}

.notification__type {
    color: var(--app-text-muted-light);
}
</style>