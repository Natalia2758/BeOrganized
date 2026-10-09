<script setup lang="ts">
import { onMounted, reactive, ref, nextTick, watch } from 'vue'
import { Priority, RepeatedFrequency, type Task, type TaskCreateRequest } from "@/features/task/types/task"
import type { Category } from '@/features/category/types/category'
import SubtaskFormContainer from './SubtaskFormContainer.vue'
import Tooltip from 'bootstrap/js/dist/tooltip'

/** Holds editable task fields, including the current subtask titles. */
const form = reactive<TaskCreateRequest>({
    title: '',
    description: null,
    priority: Priority.MEDIUM,
    idCategory: null,
    startDate: null,
    duration: false,
    timeStart: null,
    timeEnd: null,
    repeated: false,
    repeatedFrequency: null,
    subtasks: []
})

const addClicks = ref<number>(0)
const titleInput = ref<HTMLInputElement | null>(null)
const addSubtaskButton = ref<HTMLButtonElement | null>(null)
const errors = ref<Record<string, string>>({})

const emit = defineEmits<{
    /** Sends validated task data to the parent view. */
    submit: [data: TaskCreateRequest]
    /** Requests that the parent view cancel form editing. */
    cancel: []
}>()

const props = defineProps<{
    /** Categories available for assignment to the task. */
    categories: Category[]
    /** Existing task to populate when editing. */
    initialTask?: Task
    /** Optional label for the submit button. */
    submitLabel?: string
}>()

/** Keeps the form synchronized with the task being edited. */
watch(
    () => props.initialTask,
    task => {
        if (!task) return

        form.title = task.title
        form.description = task.description
        form.priority = task.priority
        form.idCategory = task.category?.idCategory ?? null
        form.startDate = task.startDate
        form.duration = task.duration
        form.timeStart = task.timeStart
        form.timeEnd = task.timeEnd
        form.repeated = task.repeated
        form.repeatedFrequency = task.repeatedFrequency
        form.subtasks = task.subtasks.map(subtask => subtask.title)
        addClicks.value = form.subtasks.length
    },
    { immediate: true }
)

/** Adds an empty subtask row to the form. */
async function addSubtaskForm() {
    addClicks.value += 1
    form.subtasks = [...form.subtasks, '']
    await nextTick()
    addSubtaskButton.value?.blur()
}

/** Removes the subtask row at the requested index. */
function removeSubtask(index: number) {
    form.subtasks.splice(index, 1)
    addClicks.value = form.subtasks.length
}

/** Emits a copy of the form when all task fields are valid. */
function handleSubmit() {
    if (!validate()) return 
    
    emit('submit', { ...form })
}

/** Delegates cancellation to the parent view. */
function handleCancel() {
    emit('cancel')
}

/** Focuses the title input and initializes form tooltips. */
onMounted(() => {
    titleInput.value?.focus()

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    tooltipTriggerList.forEach(element => {
        new Tooltip(element)
    })
})

/** Checks task fields that require client-side validation. */
function validate() {
    errors.value = {}

    if (!form.title.trim()) {
        errors.value.title = 'Title is required'
    } else if (form.title.length > 200) {
        errors.value.title = 'Title is too long'
    }

    if (form.description != null && form.description.length > 1200) {
        errors.value.description = 'Description is too long'
    }

    if (form.duration) {
        if (!form.timeStart || !form.timeEnd) {
            errors.value.time = 'Please specify start and end time of the task'
        }
    }

    if (form.repeated) {
        if (!form.repeatedFrequency) {
            errors.value.frequency = 'Please choose repetition option'
        }
    }

    if (addClicks.value !== form.subtasks.length) {
        errors.value.subtasks = 'Subtasks cannot be empty'
    }
    return Object.keys(errors.value).length === 0
}
</script>

<template>
    <!-- Collects and validates the fields for creating or editing a task. -->
    <form @submit.prevent="handleSubmit">
        <div class="">
            <div class="row mb-3">
                <div class="col-12 col-md-6 mb-4 mb-md-0">
                    <label class="form-label" for="title">Title</label>
                    <input
                        class="form-control"
                        id="title"
                        v-model="form.title" 
                        ref="titleInput"
                        type="text"
                    />
                    <div v-if="errors.title" class="alert alert-danger mt-3">
                        {{ errors.title }}
                    </div>
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

                        <option v-else v-for="category in categories"
                            :key="category.idCategory ?? category.name ?? 'category'"
                            :value="category.idCategory"
                        >
                            {{ category.name }}
                        </option>
                    </select>
                </div>
            </div>

            <div class="row mb-3">
                <div class="col-12 col-md-6 mb-4 mb-md-0">
                    <label class="form-label" for="description">Description</label>
                    <textarea
                        class="form-control"
                        id="description"
                        v-model="form.description"
                        rows="3"
                        >
                    </textarea>
                    <div v-if="errors.description" class="alert alert-danger mt-3">
                        {{ errors.description }}
                    </div>
                </div>

                <div class="col-12 col-md-3">
                    <label class="form-label" for="priority">Priority level</label>
                    <select
                        class="form-select"
                        id="priority"
                        v-model="form.priority"
                    >
                        <option
                            v-for="[key, label] in Object.entries(Priority)"
                            :key="key"
                            :value="key"
                        >{{ label }}</option>
                    </select>
                </div>
            </div>

            <div class="row mb-3 mb-5">
                <div class="col-12 col-md-3">
                    <label class="form-label" for="date">Date</label>
                    <input
                        class="form-control"
                        id="date"
                        v-model="form.startDate" 
                        type="date"/>
                </div>
            </div>

            <div v-if="form.startDate"class="row mb-5">
                <div class="col-12 col-md-2 pt-4 mb-4 mb-md-0">
                    <input
                        class="btn btn-check"
                        id="duration"
                        v-model="form.duration"
                        type="checkbox"
                        autocomplete="off"
                    />
                    <label class="btn btn-secondary w-100" for="duration">Set timing</label>
                </div>

                <div v-if="form.duration" class="col-6 col-md-3">
                    <label class="form-label" for="timeStart">Starts at</label>
                    <input
                        class="form-control"
                        id="timeStart"
                        v-model="form.timeStart"
                        type="time"
                        step="60"
                    />
                </div>

                <div v-if="form.duration" class="col-6 col-md-3">
                    <label class="form-label" for="timeEnd">Ends at</label>
                    <input
                        class="form-control"
                        id="timeEnd"
                        v-model="form.timeEnd"
                        type="time"
                        step="60"
                    />
                    <div v-if="errors.time" class="alert alert-danger mt-3">
                        {{ errors.time }}
                    </div>
                </div> 
            </div>
            
            <div v-if="form.startDate" class="row mb-5">
                <div class="col-12 col-md-3">
                    <div class="w-100">
                        <label class="form-label" for="repeated">Is the task cyclical?</label>

                        <div class="d-inline-flex flex-row gap-3 w-100 mb-md-0 mb-4" id="repeated">
                            <div class="w-100">
                                <input
                                    class="btn btn-check"
                                    id="no"
                                    name="options"
                                    v-model="form.repeated"
                                    :value="false"
                                    type="radio"
                                    autocomplete="off"
                                    checked
                                >
                                <label class="btn btn-secondary w-100" for="no">No</label>
                            </div>
                            <div class="w-100">
                                <input
                                    class="btn btn-check"
                                    id="yes"
                                    name="options"
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


                <div class="col-12 col-md-3" v-if="form.repeated">
                    <label class="form-label" for="repFrequency">How often?</label>
                    <select
                        id="repFrequency"
                        class="form-select"
                        v-model="form.repeatedFrequency"
                    >
                        <option
                            v-for="[key, label] in Object.entries(RepeatedFrequency)"
                            :key="key"
                            :value="key"
                        >{{ label }}</option>
                    </select>
                        
                </div>
                    
                <div v-if="errors.frequency" class="alert alert-danger mt-3">
                    {{ errors.frequency }}
                </div>
            </div>  
            <hr>
            <div class="row mt-5 d-flex flex-column justify-content-between">
                <div class="col-6 col-md-2">
                    <button
                        ref="addSubtaskButton"
                        type="button"
                        class="btn btn-secondary mb-5 w-100 w-md-auto"
                        @click="addSubtaskForm"
                        data-bs-toggle="tooltip"
                        data-bs-trigger="hover"
                        data-bs-title="Add subtask"
                    >
                        <i class="bi bi-plus-lg"></i>
                    </button>
                </div>
            </div>

            <div class="row mb-5">
                <div class="col-12 col-md-9">
                    <SubtaskFormContainer
                        :clicks="addClicks"
                        v-model="form.subtasks"
                        @remove="removeSubtask"
                    />
                </div>
            </div>

            <div v-if="errors.subtasks" class="alert alert-danger mt-3">
                {{ errors.subtasks }}
            </div>
            
            <div class="row justify-content-between">
                <div class="col-6 col-md-3">
                    <button
                        class="btn btn-danger w-100"
                        @click="handleCancel"
                        type="button"
                    >
                        Cancel
                    </button>
                </div>
                <div class="col-6 col-md-3">
                    <button
                        type="submit"
                        class="btn btn-primary w-100"
                    >
                        {{ props.submitLabel ?? 'Save' }}
                    </button>
                </div>
            </div>
        </div>   
    </form>
</template>