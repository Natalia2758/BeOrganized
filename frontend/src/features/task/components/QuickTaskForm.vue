<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { Priority, type TaskCreateRequestQuick } from '../types/task';

const props = defineProps<{
    /** Disables submission controls while the parent request is in progress. */
    isSubmitted: boolean
}> ()

const emit = defineEmits<{
    /** Sends validated quick-task data to the parent. */
    submit: [data: TaskCreateRequestQuick]
    /** Requests that the parent close the form. */
    cancel: []
}>()

/** Holds the values collected by the quick-task form. */
const form = reactive<TaskCreateRequestQuick>({
    title: '',
    priority: Priority.MEDIUM,
    startDate: null
})

const titleInput = ref<HTMLInputElement | null>(null)
const errors = ref<Record<string, string>>({})

/** Focuses the title field when the form is mounted. */
onMounted(() => {
    titleInput.value?.focus()

})

/** Validates the form before emitting a copy of its current values. */
function handleSubmit(){
    if (!validate()) return 

    emit('submit', { ...form })
}

/** Checks required title and maximum title length. */
function validate() {
    errors.value = {}

    if (!form.title.trim()) {
        errors.value.title = 'Title is required'
    } else if (form.title.length > 200) {
        errors.value.title = 'Title is too long'
    }
    return Object.keys(errors.value).length === 0
}
</script>

<template>
    <!-- Collects and validates the fields for creating a task quickly. -->
    <form @submit.prevent="handleSubmit">
        <div class="mb-3">
            <label class="form-label" for="title">Title*</label>
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
        <div class="row mb-5">
            <div class="col col-6">
                <label class="form-label" for="priority">Priority level*</label>
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
            <div class="col col-6">
                <label class="form-label" for="date">Date</label>
                <input
                    class="form-control"
                    id="date"
                    v-model="form.startDate" 
                    type="date"/>
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
            >
               {{ props.isSubmitted ? 'Adding...' : 'Add Task'}}
            </button>
        </div>
    </form>
</template>