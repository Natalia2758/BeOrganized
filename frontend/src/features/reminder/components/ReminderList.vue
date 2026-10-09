<script setup lang="ts">
import { type Reminder, type ReminderStatusChangeRequest } from '../types/reminder';
import ReminderListItem from './ReminderListItem.vue';


const props = defineProps<{
    /** Reminder entries to display. */
    reminders: Reminder[]
    /** Uses the compact presentation when rendered on the dashboard. */
    forDashboard?: boolean
}>()

const emit = defineEmits <{
    /** Forwards reminder status updates. */
    toggle: [reminder: ReminderStatusChangeRequest]
    /** Forwards a request to delete a reminder. */
    deleteRequest: [reminder: Reminder]
}>()

</script>
<template>
    <!-- Renders reminder entries and forwards status and deletion actions. -->
    <div class="d-flex w-100 gap-3">
        <ul class="list-group list-group-flush w-100">
            <ReminderListItem
                v-for="reminder in reminders"
                :key="reminder.idReminder"
                :reminder="reminder"
                :for-dashboard="props.forDashboard"
                @toggle="emit('toggle', $event)"
                @delete-request="emit('deleteRequest', $event)"
            >
            </ReminderListItem>
        </ul>
    </div>
</template>