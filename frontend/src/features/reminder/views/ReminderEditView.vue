<script setup lang="ts">
import { useReminder } from '../composables/useReminder';
import { useReminders } from '../composables/useReminders';
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ReminderForm from '../components/ReminderForm.vue';
import { ReminderType, type ReminderCreateRequest } from '../types/reminder.ts';
import { useEvents } from '@/features/event/composables/useEvents.ts';
import { useTasks } from '@/features/task/composables/useTasks.ts';
import { ref } from 'vue';
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue';
import type { Event } from '@/features/event/types/event.ts';
import type { Task } from '@/features/task/types/task.ts';

const {
    reminder,
    loading,
    error: errorReminder,
    loadReminder
} = useReminder()

const {
    editReminder
} = useReminders()

const {
    events,
    loading: loadingEvents,
    error: errorEvents,
    loadEvents
} = useEvents()

const {
    tasks,
    loading: loadingTasks,
    error: errorTasks,
    loadTasks
} = useTasks()

const route = useRoute()
const router = useRouter()
const showModal = ref(false)

const event = ref<Event | undefined>()
const task = ref<Task | undefined>()

/** Loads the reminder selected by the route before editing. */
onMounted(async () => {
    const idReminder = Number(route.params.idReminder)

    await loadReminder(idReminder)

})

/** Saves edited reminder values and returns to the reminder list. */
async function updateCurrentReminder(request: ReminderCreateRequest) {
    if (!reminder.value) return

    try {
        await editReminder(reminder.value.idReminder, request)

        router.push({
            name: 'reminders',
        })
    } catch {
        
    }
}

/** Loads available targets matching the reminder's selected type. */
async function loadObjects(type: ReminderType) {
    if (type === ReminderType.EVENT) {
        await loadEvents()
    } else if (type === ReminderType.TASK) {
        await loadTasks()
    }
}

/** Resolves a selected task, loading task options if needed. */
async function findTask(idTask: number) {
    if (!idTask) return
    if (tasks.value.length === 0 ) {
        await loadTasks()
    }
    const index = tasks.value.findIndex(task => task.idTask === idTask)
    task.value = tasks.value[index]
}

/** Resolves a selected event, loading event options if needed. */
async function findEvent(idEvent: number) {
    if (!idEvent) return
    if (events.value.length === 0) {
        await loadEvents()
    }
    const index = events.value.findIndex(event => event.idEvent === idEvent)
    event.value = events.value[index]
}

/** Opens cancellation confirmation before leaving the form. */
function handleCancelRequest() {
    showModal.value = true
}

/** Returns to the previous page after cancellation is confirmed. */
function handleConfirmCancel() {
    router.back()
}
</script>

<template>
    <!-- Loads a reminder and its related target for editing. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Edit Reminder</h1>

        <div v-if="loading || loadingEvents || loadingTasks">
            Loading...
        </div>

        <div v-else-if="errorReminder">
            {{ errorReminder }}
        </div>


        <ReminderForm
            v-if="reminder"
            :initialReminder="reminder"
            :submit-label="'Edit Reminder'"
            :events="[]"
            :tasks="[]"
            :loading="loadingEvents ?? loadingTasks"
            :error="errorEvents ?? errorTasks"
            :task="task"
            :event="event"
            @submit="updateCurrentReminder"
            @load-by-type="loadObjects"
            @cancel="handleCancelRequest"
            @get-event="findEvent"
            @get-task="findTask"
        />

    </div>
    <ConfirmCancellingModal
        v-if="showModal"
        :object="'reminder'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />
</template>