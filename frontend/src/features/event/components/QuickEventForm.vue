<script setup lang="ts">
import { onMounted, reactive, ref} from 'vue';
import type { EventCreateQuickRequest } from '../types/event';

/** Holds the quick event's title and date range. */
const form = reactive({
    title: '',
    dateStart: '',
    dateEnd: ''
})

const titleInput = ref<HTMLInputElement | null>(null)

const errors = ref<Record<string, string>>({})

const props = defineProps<{
    /** Disables form actions while the parent request is running. */
    isSubmitted: boolean
}>()

const emit = defineEmits<{
    /** Sends validated quick-event data to the parent. */
    submit: [request: EventCreateQuickRequest]
    /** Requests that the parent close the form. */
    cancel: []
}>()

/** Focuses the title input when the form is mounted. */
onMounted(() => {
    titleInput.value?.focus()

})

/** Validates the form before submitting a copy of its values. */
function handleSubmit() {
    if(!validate()) return

    emit('submit', { ...form })
}

/** Checks the event title and required start date. */
function validate() {
    errors.value = {}

    if (!form.title.trim()) {
        errors.value.title = 'Title is required'
    } else if (form.title.length > 200) {
        errors.value.title = 'Title is too long'
    }

    if (!form.dateStart) {
        errors.value.dateStart = 'Start date of the event is required'
    }

    return Object.keys(errors.value).length === 0
}

</script>

<template>
    <!-- Collects and validates the fields for creating an event quickly. -->
    <form @submit.prevent="handleSubmit">
        <div class="row mb-3">
            <div class="col col-12">
                <label class="form-label" for="title">Title</label>
                <input
                    class="form-control"
                    id="title"
                    v-model="form.title" 
                    ref="titleInput"
                    type="text"
                />
                <p v-if="errors.title">
                    {{ errors.title }}
                </p>
            </div>
            
        </div>

        <div class="row mb-5">
            <div class="col col-6">
                <label class="form-label" for="start-date">Start date</label>
                <input
                    class="form-control"
                    id="start-date"
                    v-model="form.dateStart"
                    type="date"
                />
                <p v-if="errors.dateStart">
                    {{ errors.dateStart }}
                </p>
            </div>

            <div class="col col-6">
                <label class="form-label" for="end-date">End date</label>
                <input
                    class="form-control"
                    id="end-date"
                    v-model="form.dateEnd"
                    type="date"
                />
            </div>
        </div>

        <div class="d-flex justify-content-between gap-3">
            <button
                class="btn btn-danger w-100 w-md-auto"
                type="button"
                :disabled="props.isSubmitted"
                @click="emit('cancel')"
            >
                Cancel
            </button>
            <button
                class="btn btn-primary w-100 w-md-auto"
                type="submit"
                :disabled="props.isSubmitted"
                @click="handleSubmit"
            >
            {{ props.isSubmitted ? 'Adding...' : 'Add Event'}}
            </button>
            
            
        </div>
    </form>
</template>