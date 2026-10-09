<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ReminderStatus, type Reminder, type ReminderStatusChangeRequest } from '../types/reminder';
import { formatDateForDisplay, formatTimeForDisplay } from '@/common/utils/dateTimeUtils';

const props = defineProps<{
    /** Reminder represented by this list item. */
    reminder: Reminder
    /** Hides full-list controls in the dashboard presentation. */
    forDashboard?: boolean
}>()

const isToggled = ref<boolean>(props.reminder.status === ReminderStatus.PENDING)

const emit = defineEmits<{
    /** Requests a reminder status update. */
    toggle: [reminder: ReminderStatusChangeRequest]
    /** Requests deletion of this reminder. */
    deleteRequest: [reminder: Reminder]
}>()

const statusUpdate = reactive<ReminderStatusChangeRequest>({
    idReminder: props.reminder.idReminder,
    status: props.reminder.status
})

/** Emits the next pending or cancelled status after a switch change. */
function toggleReminder() {
    
    statusUpdate.status = isToggled.value === true ? ReminderStatus.PENDING : ReminderStatus.CANCELLED
    emit('toggle', statusUpdate)
    
}

/** Applies the compact text style when displayed on the dashboard. */
function isForDashboard() {
    return props.forDashboard === true ? 'font-size: small;' : ''
}

</script>
<template>
    <!-- Displays a reminder with controls for status changes and deletion. -->
    <li
        class="list-group-item w-100 d-flex align-items-center gap-2 justify-content-between "
        :class="props.forDashboard ? 'reminder--for-dashboard pe-0' : 'reminder'"
    >
        
        <div class="reminder__time d-flex flex-column align-items-start justify-content-center pe-3 pe-xl-2 h-100">
            <div class="text-center fw-bold">{{ formatTimeForDisplay(reminder.remindTime) }}</div>
            <div class="text-center text-opacity-75">{{ formatDateForDisplay(reminder.remindDate) }}</div>
        </div>
        <div class="reminder__right row align-items-center justify-content-between ">
            <div class="reminder__content d-flex justify-content-center row align-items-center" :class="props.forDashboard ? 'col-10 col-md-8 col-lg-12 col-xl-10 justify-content-lg-start justify-content-xl-center' : 'col-12 col-md-6 col-lg-4 '">
                <RouterLink
                    :to="{ name: 'reminder-update', params: { idReminder: props.reminder.idReminder }}"
                    class="text-center link-offset-3 link-opacity-75-hover link-underline link-underline-opacity-0 link-underline-opacity-50-hover "
                    :class="props.forDashboard ? 'reminder__content-link' : ''"
                >
                    <h5>{{ reminder.content }}</h5>
                </RouterLink>

            </div>
            
            <div class="reminder__toggle form-switch  d-flex align-items-center pe-sm-3" :class="props.forDashboard ? 'justify-content-end pe-0 col-2 col-md-4 col-lg-12 col-xl-2' : 'justify-content-center col-6 col-md-5 col-lg-2'">
                <input
                    class="reminder__toggle-input form-check-input"
                    :class="props.forDashboard ? '' : 'me-2'"
                    v-model="isToggled"
                    :id="'switch' + props.reminder.idReminder"
                    role="switch"
                    type="checkbox"
                    ref="status-toggle"
                    @change="toggleReminder"
                >
                <label v-if="!props.forDashboard" class="active d-none d-md-block text-center" :for="'switch' + props.reminder.idReminder">{{ isToggled ? 'Active' : 'Not active' }}</label>
            </div>
            <div v-if="!props.forDashboard" class="col-6 col-md-1 d-flex align-items-center justify-content-end">
                <button 
                    type="button"
                    class="btn btn-lg link-danger link-opacity-75-hover "
                    @click="emit('deleteRequest', props.reminder)"
                >
                    <i class="bi bi-trash-fill"></i>
                </button>
            </div>
        </div>
    </li>
</template>
<style scoped>
.active {
    color: var(--app-text-muted);
    width: min(5rem);
}

.reminder__time {
    color: var(--app-text-muted);
    min-width: max-content;
    flex: 0 0 auto;
}

.reminder__right {
    flex: 1 1 auto;
    min-width: 0;
    width: 100%;
    margin: 0;
}

.reminder__toggle-input:hover {
    cursor: pointer;
    
}

.reminder__content {
    min-width: 0;
}

.reminder--for-dashboard {
    font-size: small;
    
}

.reminder__content-link {
        width: 100%;
        overflow: hidden;
        & h5 {
            width: 100%;
            font-size: medium;
            overflow: hidden;
        }
    }

@media (max-width: 768px) {
    .reminder__right {
        flex: 1 1 60%;
    }
}

</style>