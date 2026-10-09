<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import ReminderForm from '../components/ReminderForm.vue';
import { useReminders } from '../composables/useReminders';
import { ReminderType, type ReminderCreateRequest } from '../types/reminder';
import { computed, ref, watch } from 'vue';
import { useEvents } from '@/features/event/composables/useEvents.ts';
import { useTasks } from '@/features/task/composables/useTasks.ts';
import type { Task } from '@/features/task/types/task.ts';
import type { Event } from '@/features/event/types/event.ts';
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue';

const router = useRouter()
const route = useRoute()
const showModal = ref(false)

const {
    error: reminderError,
    addReminder
} = useReminders()

const {
    events,
    loading: loadingEvents,
    error: errorEvents,
    loadEventsInFuture
} = useEvents()

const {
    tasks,
    loading: loadingTasks,
    error: errorTasks,
    loadTasksInFuture
} = useTasks()

const event = ref<Event | undefined>()
const task = ref<Task | undefined>()

/** Reads a valid preselected event ID from the route. */
const idEvent = computed(() => {
    if (route.query.source !== 'event') {
        return undefined
    }

    const idEvent = route.params.idEventTask
    const parsedId = Number(idEvent)

    return Number.isNaN(parsedId) ? undefined : parsedId
})

/** Loads and resolves the route-provided event target. */
watch(() => idEvent, async (idEvent) => {
    if (!idEvent.value) return
    await loadEventsInFuture()
    findEvent(idEvent.value)
},
    { immediate: true}
)

/** Reads a valid preselected task ID from the route. */
const idTask = computed(() => {
    if (route.query.source !== 'task') {
        return undefined
    }

    const idTask = route.params.idEventTask
    const parsedId = Number(idTask)

    return Number.isNaN(parsedId) ? undefined : parsedId
})

/** Loads and resolves the route-provided task target. */
watch(() => idTask, async (idTask) => {
    if (!idTask.value) return
    await loadTasksInFuture()
    findTask(idTask.value)
},
    { immediate: true}
)

/** Loads target options for the reminder type chosen in the form. */
async function loadObjects(type: ReminderType) {
    if (type === ReminderType.EVENT) {
        await loadEventsInFuture()
    } else if (type === ReminderType.TASK) {
        await loadTasksInFuture()
    }
}

/** Resolves the selected task from the loaded target options. */
function findTask(idTask: number) {
    if (!idTask) return
    const index = tasks.value.findIndex(task => task.idTask === idTask)
    task.value = tasks.value[index]
}

/** Resolves the selected event from the loaded target options. */
function findEvent(idEvent: number) {
    if (!idEvent) return
    const index = events.value.findIndex(event => event.idEvent === idEvent)
    event.value = events.value[index]
}

/** Creates the reminder and returns to the reminder list on success. */
async function createReminder(request: ReminderCreateRequest) {
    try {
        await addReminder(request)

        router.push({
            name: 'reminders'
        })
    } catch {}
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
    <!-- Resolves an optional event or task target and creates a reminder. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Add Reminder</h1>
        <ReminderForm
            @submit="createReminder"
            @load-by-type="loadObjects"
            @cancel="handleCancelRequest"
            @get-event="findEvent"
            @get-task="findTask"
            :id-event="idEvent"
            :id-task="idTask"
            :submit-label="'Add reminder'"
            :events="events"
            :tasks="tasks"
            :loading="loadingEvents ?? loadingTasks"
            :error="errorEvents ?? errorTasks"
            :task="task"
            :event="event"
        >
        <p v-if="reminderError">
            {{ reminderError }}
        </p>

        </ReminderForm>
    </div>
    <ConfirmCancellingModal
        v-if="showModal"
        :object="'reminder'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />
</template>