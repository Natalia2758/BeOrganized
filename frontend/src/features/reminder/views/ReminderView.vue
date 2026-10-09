<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useReminders } from '../composables/useReminders';
import ReminderList from '../components/ReminderList.vue';
import type { Reminder, ReminderStatusChangeRequest } from '../types/reminder.ts';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';

const reminderToDelete = ref<Reminder | null>(null)

const {
    reminders,
    loading,
    error,
    loadReminders,
    updateStatus,
    removeReminder
} = useReminders() 

onMounted(async () => {
    await loadReminders()
})

/** Persists the status selected by a reminder list item. */
async function toggleReminder(reminder: ReminderStatusChangeRequest) {
    await updateStatus(reminder)
}

/** Shows the empty state only after loading an empty reminder list. */
const isEmpty = computed(() => {
    return !loading.value && reminders.value.length === 0
})

/** Stores the reminder awaiting deletion confirmation. */
function handleDeleteRequest(reminder: Reminder) {
    reminderToDelete.value = reminder
}

/** Deletes the confirmed reminder and always clears the pending selection. */
async function handleConfirmDelete() {
    if (!reminderToDelete.value) return

    try {
        await removeReminder(reminderToDelete.value.idReminder)
    } finally {
        reminderToDelete.value = null
    }
}

</script>

<template>
    <!-- Displays the user's reminders and coordinates status changes and deletion. -->
    <div>
        <div class="d-flex flex-row align-items-center justify-content-between">
            <h1 class="d-flex gap-4">
                <span>Reminders</span>
                <i class="bi bi-bell"></i>
            </h1>
            <RouterLink
                class="btn btn-primary"
                :to="{ name: 'reminder-creation'}"
            >
                <span class="d-none d-md-inline">Add reminder</span>
                <span class="d-inline d-md-none"><i class="bi bi-plus-lg"></i></span>
                
            </RouterLink>
        </div>

        <CommonContainer :margin-bottom="true">
            <template #1>
                <div v-if="isEmpty">
                    No reminders found.
                </div>

                <div v-else-if="loading">Loading reminders...</div>
                <div v-else-if="error">
                    {{ error }}
                </div>
                
                <ReminderList
                    :reminders="reminders"
                    @toggle="toggleReminder"
                    @delete-request="handleDeleteRequest"
                />
            </template>
        </CommonContainer>     
    </div>
    <ConfirmDeletionModal
        v-if="reminderToDelete"
        :object="'task'"
        @cancel="reminderToDelete = null"
        @yes="handleConfirmDelete"
    />
</template>