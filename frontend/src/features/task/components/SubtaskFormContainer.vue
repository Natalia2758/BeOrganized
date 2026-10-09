<script setup lang="ts">
import { computed } from 'vue'
import SubTaskForm from './SubtaskForm.vue'

const props = defineProps<{
    /** Number of subtask input rows to display. */
    clicks: number
    /** Current subtask titles managed by the parent form. */
    modelValue: string[]
}>()

const emit = defineEmits<{
    /** Updates the parent form's subtask title list. */
    'update:modelValue': [value: string[]]
    /** Requests removal of the subtask row at the given index. */
    remove: [index: number]
}>()

/** Builds one input value per requested row, filling missing titles with blanks. */
const subtasks = computed(() => {
    return Array.from({ length: props.clicks }, (_, index) => props.modelValue[index] ?? '')
})

/** Copies and emits the updated title list to preserve parent-owned state. */
function updateSubtask(index: number, value: string) {
    const next = [...props.modelValue]
    next[index] = value
    emit('update:modelValue', next)
}

</script>
<template>
    <!-- Manages the collection of editable subtask title inputs. -->
    <div v-if="props.clicks > 0" class="w-100 w-md-75">
        <div
            v-for="(subtask, index) in subtasks"
            :key="index"
            class="d-flex gap-3 align-items-center mb-3"
        >
            <SubTaskForm
                :subtask-num="index + 1"
                :model-value="subtask"
                @update:model-value="value => updateSubtask(index, value)"
            />
            <button
                type="button"
                class="btn btn-danger"
                @click="emit('remove', index)"
            >
                <span class="d-md-block d-none">Remove</span>
                <span class="d-md-none d-block"><i class="bi bi-trash-fill"></i></span>
            </button>
        </div>
    </div>
</template>