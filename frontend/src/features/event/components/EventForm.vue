<script setup lang="ts">
import { type Category } from '@/features/category/types/category';
import { RepeatedFrequency, type Event, type EventCreateRequest } from '../types/event';
import { onMounted, reactive, ref, watch } from 'vue';
import { formatTimeForDisplay } from '@/common/utils/dateTimeUtils';

/** Holds editable event details, timing, category, and recurrence fields. */
const form = reactive<EventCreateRequest>({
    title: '',
    details: null,
    idCategory: null,
    dateStart: '',
    dateEnd: '',
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: false,
    repeatedFrequency: null
})

const titleInput = ref<HTMLInputElement | null>(null)
const errors = ref<Record<string, string>>({})

/** Converts backend date-time values to the YYYY-MM-DD format required by date inputs. */
function toDateInputValue(date: string | null | undefined) {
    return date ? date.slice(0, 10) : ''
}

const props = defineProps<{
    /** Categories available for event assignment. */
    categories: Category[] | []
    /** Existing event to populate when editing. */
    initialEvent?: Event
    /** Optional date used to initialize a new event. */
    initialDate?: string
    /** Optional label for the submit button. */
    submitLabel?: string
}>()

const emit = defineEmits<{
    /** Sends validated event data to the parent view. */
    submit: [data: EventCreateRequest]
    /** Requests that the parent view cancel editing. */
    cancel: []
}>()

/** Focuses the event title input on mount. */
onMounted(() => {
    titleInput.value?.focus()
})

/** Defaults the end date to the selected start date when not yet set. */
watch(() => form.dateStart, (dateStart) => {
    if (form.dateEnd == '') {
        form.dateEnd = dateStart
    }
})

/** Applies a date passed from calendar navigation to a new event. */
watch(
    () => props.initialDate,
    date => {
        if (!date || props.initialEvent) return

        form.dateStart = toDateInputValue(date)
        form.dateEnd = toDateInputValue(date)
    },
    { immediate: true }
)

/** Populates the form fields when an existing event is selected for editing. */
watch(
    () => props.initialEvent,
    event => {
        if(!event) return

        form.title = event.title
        form.details = event.details
        form.idCategory = event.category?.idCategory ?? null
        form.dateStart = toDateInputValue(event.dateStart)
        form.dateEnd = toDateInputValue(event.dateEnd)
        form.duration = event.duration
        form.timeStart = event.timeStart ? formatTimeForDisplay(event.timeStart) : null
        form.timeEnd = event.timeEnd ? formatTimeForDisplay(event.timeEnd) : null
        form.repeated = event.repeated
        form.repeatedFrequency = event.repeatedFrequency
    },
    { immediate: true }
)

/** Emits a copy of the form when validation succeeds. */
function handleSubmit(){
    if (!validate()) return 

    emit('submit', {...form})
}

/** Delegates form cancellation to the parent view. */
function handleCancel() {
    emit('cancel')
}

/** Validates required fields and timing or recurrence-dependent values. */
function validate() {
    errors.value = {}

    if (!form.title && form.title.trim() !== '') {
        errors.value.title = 'Title is required'
    } else if (form.title.length > 200) {
        errors.value.title = 'Title is too long'
    }

    if (form.details && form.details.length > 1200) {
        errors.value.details = 'Event details section is too long'
    }

    if (!form.dateStart) {
        errors.value.dateStart = 'Date of the event is required'
    }

    if (form.duration) {
        if (!form.timeStart || !form.timeEnd) {
            errors.value.time = 'Please specify start and end time of the event'
        }
    }

    if (form.repeated) {
        if (!form.repeatedFrequency) {
            errors.value.frequency = 'Please choose repetition option'
        }
    }
    return Object.keys(errors.value).length === 0
}

</script>
<template>
    <!-- Collects and validates the fields for creating or editing an event. -->
    <form @submit.prevent="handleSubmit">
        <div>
            <div class="row mb-3">
                <div class="col-12 col-md-6 mb-4 mb-md-0">
                    <label class="form-label" for="title">Title*</label>
                    <input 
                        class="form-control"
                        id="title"
                        v-model="form.title"
                        ref="titleInput"
                        type="text"
                        required
                    />
                    <p v-if="errors.title">
                        {{ errors.title }}
                    </p>
                </div>
                <div class="col-12 col-md-3">
                    <label class="form-label" for="category">Category</label>
                    <select 
                        class="form-select"
                        id="category"
                        v-model="form.idCategory"    
                    >
                        <option disabled v-if="categories.length === 0">
                            No categories available
                        </option>
                        <option
                            v-else
                            v-for="cat in categories"
                            :key="cat.idCategory ?? cat.name ?? 'category'"
                            :value="cat.idCategory"
                        >
                            {{ cat.name }}
                        </option>
                    </select>
                </div>
            </div>
            <div class="row mb-md-5">
                <div class="col-12 col-md-6 mb-4 mb-md-0">
                    <label class="form-label" for="details">Details</label>
                    <textarea 
                        class="form-control"
                        id="details"
                        v-model="form.details"
                        type="text"
                        rows="3"
                    ></textarea>
                    <p v-if="errors.details">
                        {{ errors.details }}
                    </p>
                </div>
            </div>
        
            <div class="row mb-3 mb-md-5">
                <div class="col-6 col-md-3">
                    <label class="form-label" for="dateFrom">Start date*</label>
                    <input 
                        class="form-control"
                        id="dateFrom"
                        v-model="form.dateStart"
                        type="date"
                    />
                </div>
                
                <div class="col-6 col-md-3">
                    <label class="form-label" for="dateTo">End date</label>
                    <input 
                        class="form-control"
                        id="dateTo"
                        v-model="form.dateEnd"
                        type="date"
                    />
                </div>
            
                <p v-if="errors.date">
                    {{ errors.date }}
                </p>
            </div>
        
            <div class="row mb-3">
                <div class="col-12 col-md-2 pt-4 mb-4 mb-md-0">
                    <input 
                        class="btn-check"
                        id="duration"
                        v-model="form.duration"
                        type="checkbox"
                        autocomplete="off"
                    />
                    <label class="btn btn-secondary w-100" for="duration">Set timing</label>
                </div>  
            </div>
            <div v-if="form.duration" class="row mb-5">
                <div class="col-6 col-md-3">
                    <label class="form-label" for="timeStart">Starts at</label>
                    <input 
                        class="form-control"
                        id="timeStart"
                        v-model="form.timeStart"
                        type="time"
                        step="60"
                    />
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label" for="timeEnd">Ends at</label>
                    <input 
                        class="form-control"
                        id="timeEnd"
                        v-model="form.timeEnd"
                        type="time"
                        step="60"
                    />
                    <p v-if="errors.time">
                        {{ errors.time }}
                    </p>   
                </div>
            </div>
            <div class="row mb-3 mt-5">
                <div class="col-12 col-md-3">
                    <div class="w-100">
                        <label class="form-label" for="repeated">Is the event cyclical?</label>
                    
                        <div class="d-inline-flex flex-row gap-3 w-100 mb-md-0 mb-4" id="repeated">
                            <div class="w-100">
                                <input
                                    class="btn btn-check"
                                    id="no"
                                    v-model="form.repeated"
                                    :value="false"
                                    type="radio"
                                    autocomplete="off"
                                >
                                <label class="btn btn-secondary w-100" for="no">No</label>
                            </div>
                                
                            <div class="w-100">
                                <input
                                    class="btn btn-check"
                                    id="yes"
                                    v-model="form.repeated"
                                    :value="true"
                                    type="radio"
                                    autocomplete="off"
                                >
                                <label class="btn btn-secondary w-100" for="yes">Yes</label>
                            </div>
                            
                        </div>
                    </div>
                </div>
            </div>
            <div class="row mb-5">
                <div class="col-12 col-md-3" v-if="form.repeated">
                    <label class="form-label" for="repFrequency">How often</label>
                    <select
                        class="form-select"
                        id="repFrequency"
                        v-model="form.repeatedFrequency"
                    >
                        <option
                            v-for="[key, label] in Object.entries(RepeatedFrequency)"
                            :key="key"
                            :value="key"
                        >{{ label }}</option>
                    </select>
                </div>
                <p v-if="errors.frequency">
                    {{ errors.frequency }}
                </p>
            </div>

            <div class="row justify-content-between">
                <div class="col-6 col-md-3">
                    <button
                        type="button"
                        class="btn btn-danger w-100"
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

</template>