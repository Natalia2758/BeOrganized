<script setup lang="ts">
import type { Event } from '@/features/event/types/event';
import type { Task } from '@/features/task/types/task';
import { watch } from 'vue';
import { ref } from 'vue';

const errors = ref<Record<string, string>>({})

const props = defineProps<{
    /** Determines whether the selector lists tasks or events. */
    type: string
    /** Available candidate targets, when loaded. */
    events?: Event[]
    tasks?: Task[]
    /** Loading or selection error displayed by the modal. */
    error: string | null
    /** Details of the selected target, used for repeat options. */
    task?: Task
    event?: Event
}>()

const emit = defineEmits<{
    /** Closes the selector without further changes. */
    close: []
    /** Emits the selected event ID. */
    idEvent: [number | null]
    /** Emits the selected task ID. */
    idTask: [number | null]
    /** Emits whether the reminder should apply to all repetitions. */
    repeatForAll: [boolean]
}>()

const idObject = ref<number | null>(null)
const repeatForAll = ref<boolean>(false)

/** Forwards the selected target ID using the current target type. */
watch(idObject, (id) => {
    if (props.type === 'event') {
        emit('idEvent', id)
    } else if (props.type === 'task') {
        emit('idTask', id)
    }
},
{ immediate: true })

/** Requires a target selection before confirming the modal. */
function validate() {
    errors.value = {}

    if (idObject.value === null) {
        errors.value.title = 'Please choose ' + props.type
    }

    return Object.keys(errors.value).length === 0
}

/** Emits repeat settings and closes the selector after validation. */
function emitData() {
    if (validate()) {
        emit('repeatForAll', repeatForAll.value)

        emit('close')
    }
}

</script>

<template>
    <!-- Lets the user choose a task or event to associate with a reminder. -->
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">

                <div class="modal-header">
                    <h2>Choose {{ props.type }}</h2>
                    <button
                        type="button"
                        class="btn-close"
                        @click="emit('close')"
                    ></button>
                </div>

                <div class="modal-body">
                    
                    <select
                        v-model="idObject"
                        class="form-select mb-3"
                        :placeholder="'Choose ' + props.type"
                    >
                        <option
                            v-if="!props.events || props.events.length === 0 || !props.tasks || props.tasks.length === 0"
                            selected disabled
                        >
                            No {{ props.type }}s available
                        </option>
                        <option
                            v-else-if="props.type === 'event' && props.events"
                            v-for="event in events"
                            :key="event.idEvent"
                            :value="event.idEvent"
                        >
                            {{ event.title }} - {{ event.dateStart }}
                        </option>

                        <option
                            v-else-if="props.type === 'task' && props.tasks"
                            v-for="task in tasks"
                            :key="task.idTask"
                            :value="task.idTask"
                        >
                            {{ task.title }}
                        </option>
                    </select>
                    <div v-if="errors.title || error" class="alert alert-danger">
                        {{ errors.title }}
                        {{ props.error }}
                    </div>

                    <div
                        v-if="(props.type === 'task' && props.task && props.task.repeated === true) || (props.type === 'event' && props.event && props.event.repeated === true)"
                        class="form-check my-2"
                    >
                        <input
                            v-model="repeatForAll"
                            type="checkbox"
                            class="form-check-input"
                            id="repeatForAll"
                        />
                        <label class="form-check-label" for="repeatForAll">Create reminder for each {{ props.type }} repetition</label>
                    </div>

                    <button
                        class="btn btn-primary"
                        type="button"
                        @click="emitData"
                    >
                        Select
                    </button>
                </div>

            </div>
        </div>
    </div>
    <div class="modal-backdrop fade show"></div>
</template>