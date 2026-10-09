<script setup lang="ts">
import { computed, ref } from 'vue';
import { colorOptions, colorPalette } from '../types/color';

const props = defineProps<{
    /** Currently selected color value. */
    modelValue: string
}>()

const emit = defineEmits<{
    /** Emits the selected color through the v-model contract. */
    'update:modelValue': [color: string]
}>()

/** Controls visibility of the color swatches. */
const isOpen = ref(false)

/** Resolves the selected palette entry for display. */
const selectedColor = computed(() => {
    Object.values(colorPalette).find(
        color => color.color === props.modelValue
    )
})

/** Updates the bound color and closes the palette. */
function selectColor(color: string) {
    emit('update:modelValue', color)
    isOpen.value = false
}
</script>

<template>
    <!-- Lets the user select a category color and updates the bound value. -->
    <div class="color-picker">
        <div class="input-group">
            <input
                class="form-control" 
                :style="{ backgroundColor: props.modelValue }" 
                type="text" 
                :value="selectedColor" 
                disabled 
            />
            <button
                class="btn btn-subtle"
                type="button"
                @click="isOpen = !isOpen"
            >
                <i class="bi bi-caret-down-fill"></i>
            </button>
        </div>
        
        <div
            v-if="isOpen"
            class="color-picker__popover d-flex flex-row justify-content-between p-3 w-100"
        >
            <div class="color-picker__swatches d-flex gap-3">
                <button
                    v-for="color in colorOptions"
                    :key="color.color"
                    type="button"
                    class="color-picker__swatch"
                    :class="{ 'color-picker__swatch--selected': color.color === props.modelValue, 'color-picker__swatch--nocolor': color.name === 'No color'}"
                    :style="{ backgroundColor: color.color }"
                    :title="color.name"
                    @click="selectColor(color.color)"
                >
                </button>
            </div>
            <button
                class="btn color-picker__close link-opacity-75-hover"
                type="button"
                @click="isOpen = false"
            >
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
    </div>
</template>

<style scoped>

.color-picker {
    --color-size: 32px;
}

.color-picker__popover {
    background-color: var(--app-surface);
    border-radius: var(--app-radius-lg);
    box-shadow: var(--app-shadow);
}

.color-picker__swatches {
    display: flex;
}

.color-picker__swatch {
    width: var(--color-size);
    height: var(--color-size);
    padding: 0;
    border: 0;
    border-radius: 50%;
    box-shadow: 0 1px 3px rgb(0 0 0 / 20%);
}

.color-picker__swatch--nocolor {
    border: 1px solid var(--app-text-muted);
}

.color-picker__swatch--selected {
    outline: 2px solid var(--app-surface);
    outline-offset: 2px;
}

.color-picker__close {
    color: var(--app-text-muted);
    
}
</style>