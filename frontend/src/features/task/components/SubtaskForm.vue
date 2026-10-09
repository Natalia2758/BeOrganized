<script setup lang="ts">
const props = defineProps<{
    /** Position of this input in the subtask form. */
    subtaskNum: number
    /** Current title value supplied by the parent form. */
    modelValue: string
}>()

const emit = defineEmits<{
    /** Updates the parent form with the edited title. */
    'update:modelValue': [value: string]
}>()

/** Emits the current input value using the component's v-model event. */
function handleInput(event: Event) {
    emit('update:modelValue', (event.target as HTMLInputElement).value)
}
</script>

<template>
    <!-- Renders an editable input for one subtask title. -->
        <div class="w-100 d-flex flex-row align-items-center gap-3">
            <span>{{ props.subtaskNum }}</span>
            <input
                class="form-control "
                :value="props.modelValue"
                type="text"
                @input="handleInput"
            />
        </div>
</template>