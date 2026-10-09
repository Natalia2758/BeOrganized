<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ReminderType, Timing, reminderTypeLabels, timingLabels, type Reminder, type ReminderCreateRequest } from '../types/reminder';
import ReminderAssignEventTaskModal from './ReminderAssignEventTaskModal.vue';
import type { Task } from '@/features/task/types/task.ts';
import type { Event } from '@/features/event/types/event.ts';
import { formatTimeForStorage } from '@/common/utils/dateTimeUtils.ts';
import Tooltip from 'bootstrap/js/dist/tooltip'


/** Holds reminder details while the user edits the form. */
const form = reactive<ReminderCreateRequest>({
    content: '',
    type: ReminderType.NONE,
    idEvent: null as number | null, // list of user tasks/events? => another view? popup with list to choose?
    idTask: null as number | null,
    repeatForAll: false,
    remindAt: Timing.SET_MANUALLY,
    remindDate: '',
    remindTime: ''
})

const contentInput = ref<HTMLInputElement | null>(null)
const errors = ref<Record<string, string>>({})
const showSelectEventTaskModal = ref(false)

const emit = defineEmits<{
    /** Emits validated reminder data for creation or update. */
    submit: [data: ReminderCreateRequest]
    /** Requests available tasks or events when the reminder type changes. */
    loadByType: [ReminderType]
    /** Requests the selected related event or task details. */
    getEvent: [number]
    getTask: [number]
    /** Requests that the parent cancel reminder editing. */
    cancel: []
}>()

const props = defineProps<{
    /** Existing values when editing a reminder. */
    initialReminder?: Reminder
    /** Optional label override for the submit button. */
    submitLabel?: string
    /** Optional event or task preselected from its source view. */
    idEvent?: number
    idTask?: number
    /** Available targets for the selected reminder type. */
    events: Event[]
    tasks: Task[]
    /** Target-loading state shown by the form and selector. */
    loading: boolean
    error: string | null
    /** Details of the currently selected reminder target. */
    task?: Task
    event?: Event
}>()

/** Keeps the form in sync with edit data or a preselected target. */
watch(
    () => [props.initialReminder, props.idEvent, props.idTask],
    syncForm,
    { immediate: true }
)

/** Loads target options and clears stale targets when the type changes. */
watch(
    () => form.type,
    (type, previousType) => {
        if (type !== ReminderType.NONE) {
            emit('loadByType', type)
        }
        if (previousType !== undefined && type !== previousType) {
            form.idEvent = null
            form.idTask = null
        }
    },
    { immediate: true}  
)

/** Requests the details for the currently selected target. */
watch(
    () => [form.idEvent, form.idTask],
    () => {
        if (form.idEvent) {
            emit('getEvent', form.idEvent)
        } else if (form.idTask) {
            emit('getTask', form.idTask)
        }
    },
    { immediate: true }
)

/** Focuses the message field and initializes Bootstrap tooltips. */
onMounted(() => {
    contentInput.value?.focus()

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    tooltipTriggerList.forEach(element => {
        new Tooltip(element)
    })
})

/** Populates edit values or initializes a new reminder from route props. */
function syncForm() {
    if (props.initialReminder) {
        form.content = props.initialReminder.content
        form.type = props.initialReminder.type
        form.idEvent = props.initialReminder.idEvent
        form.idTask = props.initialReminder.idTask
        form.repeatForAll = props.initialReminder.repeatForAll ?? false
        form.remindAt = props.initialReminder.remindAt
        form.remindDate = props.initialReminder.remindDate
        form.remindTime = props.initialReminder.remindTime
        return
    }

    form.content = ''
    form.type = ReminderType.NONE
    form.idEvent = null
    form.idTask = null
    form.repeatForAll = false
    form.remindAt = Timing.SET_MANUALLY
    form.remindDate = ''
    form.remindTime = ''

    if (props.idEvent != null) {
        form.type = ReminderType.EVENT
        form.idEvent = props.idEvent
        return
    }

    if (props.idTask != null) {
        form.type = ReminderType.TASK
        form.idTask = props.idTask
    }
}

/** Opens the target selector only after target options are loaded. */
function openSelectEventTaskModal() {
    showSelectEventTaskModal.value = !props.loading ? true : false
}

/** Validates the form and emits values in API-compatible format. */
function handleSubmit() {

    if (!validate()) return 

    emit('submit', {
        content: form.content,
        type: form.type,
        idEvent: form.idEvent,
        idTask: form.idTask,
        repeatForAll: form.repeatForAll,
        remindAt: form.remindAt,
        remindDate: form.remindDate,
        remindTime: form.remindTime ? formatTimeForStorage(form.remindTime) : null
    }
    )
}

/** Delegates cancellation to the parent view. */
function handleCancel() {
    emit('cancel')
}

/** Validates message, target selection, and manually scheduled time. */
function validate() {
    errors.value = {}

    if(!form.content) {
        errors.value.content = 'Reminder message must not be null'
    } else if (form.content.length > 200) {
        errors.value.content = 'Content is too long'
    }

    if (form.type === ReminderType.EVENT && !form.idEvent) {
        errors.value.idEvent = 'Please choose an event to be reminded about'
    } else if (form.type === ReminderType.TASK && !form.idTask) {
        errors.value.idTask = 'Please choose a task to be reminded about'
    }

    if (form.remindAt === Timing.SET_MANUALLY) {
        if (!form.remindDate || !form.remindTime) {
            errors.value.date = 'Please set the date and time of the reminder'
        } else if (!isFutureDate(form.remindDate, form.remindTime)) {
            errors.value.date = 'Date and time must be in the future'
        }
    }

    return Object.keys(errors.value).length === 0
}

/** Checks whether the combined reminder date and time are in the future. */
function isFutureDate(dateString: string | null, timeString: string | null) {
    const [year, month, day] = dateString !== null ? dateString.split('-') : []
    
    const [hour, minute] = timeString !== null ? timeString.split(':') : []
    
    const now = new Date()
    const dateTime = new Date(Number(year), Number(month) - 1, Number(day), Number(hour) ?? undefined, Number(minute) ?? undefined)
    return dateTime > now ? true : false
}

function showTimingSelect() {
    const show = form.type !== ReminderType.NONE
    && ((props.event?.dateStart && props.event.timeStart)
    || (props.task?.startDate && props.task.timeStart)) ? true : false

    if (show) {
        const remindAt = document.querySelector('#remindAt')
        if (remindAt != null) new Tooltip(remindAt)
    }

    return show
}

/** Returns the title of the selected event or task, when available. */
function getTitle() {

    if (form.idEvent && props.event) {
        return props.event.title
    } else if (form.idTask && props.task) {
        return props.task.title
    }
}

/** Applies an event selection from the target modal. */
function setIdEvent(idEvent: number | null) {
    if (idEvent === null) return
    form.idEvent = idEvent
}

/** Applies a task selection from the target modal. */
function setIdTask(idTask: number | null) {
    if (idTask === null) return
    form.idTask = idTask
}

</script>
<template>
    <!-- Collects reminder details and emits form, target-selection, and cancel actions. -->
    
    <form @submit.prevent="handleSubmit">
        <div>
            <div class="row mb-3">
                <div class="col-12 col-md-6 mb-4 mb-md-0">
                    <label class="form-label" for="content">Message*</label>
                    <input
                        class="form-control"
                        id="content"
                        v-model="form.content"
                        ref="contentInput"
                        type="text"
                    >
                    
                </div>
                <div class="alert alert-danger mt-3" v-if="errors.content">
                    {{ errors.content }}
                </div>
                
                <div class="col-12 col-md-3 mb-4 mb-md-0">
                    <label class="form-label" for="type">Type</label>
                    <select
                        class="form-select"
                        id="type"
                        v-model="form.type"
                        :disabled="props.initialReminder !== undefined"
                        :data-bs-toggle="props.initialReminder !== undefined ? 'tooltip' : ''"
                        data-bs-title="Type cannot be changed after creation"
                    >
                        <option v-if="props.initialReminder !== undefined" selected :value="props.initialReminder.type">{{ props.initialReminder.type }}</option>
                        <option
                            v-for="[key, label] in Object.entries(reminderTypeLabels)"
                            :key="key"
                            :value="key"
                        > {{ label }}
                        </option>
                    </select>
                </div>
                <div v-if="form.type !== ReminderType.NONE" class="col-12 col-md-3 d-flex align-items-end">
                    <div 
                        class="input-group"
                        :data-bs-toggle="props.initialReminder !== undefined ? 'tooltip' : ''"
                        :data-bs-title="`${form.type === ReminderType.EVENT ? 'event' : 'task'} cannot be changed after creation`"
                    >
                        <button
                            @click="openSelectEventTaskModal"
                            class="btn btn-secondary"
                            type="button"
                            :disabled="props.initialReminder !== undefined && (tasks.length === 0 || events.length === 0)"
                            
                        >
                            Choose {{ form.type === ReminderType.EVENT ? 'event' : (form.type === ReminderType.TASK ? 'task' : '')}}
                        </button>
                        <input
                            class="form-control bg-white"
                            :placeholder="getTitle()"
                            disabled
                            readonly
                        />
                    </div>
                    
                </div>
                <div v-if="errors.idEvent || errors.idTask" class="alert alert-danger mt-3">
                    {{ errors.idEvent || errors.idTask }}
                </div>
                
            </div>
            <div class="row mb-3">
                <div
                    v-if="showTimingSelect()"
                    class="col-12 col-md-6"
                >
                    <label class="form-label" for="remindAt">Timing option</label>
                    <select
                        class="form-select"
                        id="remindAt"
                        v-model="form.remindAt"
                        :disabled="props.initialReminder !== undefined"
                        :data-bs-toggle="props.initialReminder !== undefined ? 'tooltip' : ''"
                        data-bs-title="Timing option cannot be changed after creation"
                    >
                        <option
                            v-if="props.initialReminder !== undefined" 
                            selected 
                            :value="form.remindAt"
                            
                        >
                            {{ form.remindAt }}
                        </option>
                        <option
                            v-for="[key, label] in Object.entries(timingLabels)"
                            :key="key"
                            :value="key"
                        >{{ label }}</option>
                    </select>
                </div>
            </div>
            <div v-if="form.remindAt === Timing.SET_MANUALLY" class="row mb-3">
                <div class="col-6 col-md-3">
                    <label class="form-label" for="remindDate">Date*</label>
                    <input
                        class="form-control"
                        id="remindDate"
                        v-model="form.remindDate"
                        type="date"
                    >
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label" for="remindTime">Time*</label>
                    <input
                        class="form-control"
                        id="remindTime"
                        v-model="form.remindTime"
                        type="time"
                        step="60"
                    >
                </div>
                <div v-if="errors.date" class="alert alert-danger mt-3">
                    {{ errors.date }}
                </div>
            </div>
            <div class="row justify-content-between mt-5">
                <div class="col-6 col-md-3">
                    <button
                        class="btn btn-danger w-100"
                        type="button"
                        @click="handleCancel"
                    >
                        Cancel
                    </button>
                </div>
                <div class="col-6 col-md-3">
                    <button 
                        class="btn btn-primary w-100"
                        type="submit"
                    >
                        {{ props.submitLabel ?? 'Save' }}
                    </button>
                </div>
            </div>
        </div>
    </form>

    <ReminderAssignEventTaskModal
        v-if="showSelectEventTaskModal"
        :type="((form.type).toString()).toLowerCase()"
        :events="props.events"
        :tasks="props.tasks"
        :error="props.error"
        :task="props.task"
        :event="props.event"
        @close="showSelectEventTaskModal = false"
        @id-event="setIdEvent($event)"
        @id-task="setIdTask($event)"
        @repeat-for-all="form.repeatForAll = $event"
    />
</template>