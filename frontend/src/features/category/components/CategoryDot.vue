<script setup lang="ts">

const props = defineProps<{
    /** Category color, or null to show the neutral fallback. */
    color: string | null
    /** Visual size of the indicator. */
    size: 'sm' | 'md' | 'lg'
}>()

</script>
<template>
    <!-- Displays a category color as a size-adjustable dot. -->
    <div class="dot__container d-flex align-items-center px-2">
        <div
            class="dot__element"
            :style="props.color !== null ? 'background-color: ' + props.color : 'background-color: var(--app-surface);  border: var(--app-border-subtle-thin);'"
            :class="'dot__element--' + props.size"
        >
        </div>
    </div>
</template>
<style scoped>
.dot__container {
    max-height: fit-content;
}

.dot__element {
    border-radius: 50%;
    flex-shrink: 0;
    display: inline-block;
}

.dot__element--sm {
    width: 10px;
    height: 10px;
}
.dot__element--md {
    width: 16px;
    height: 16px;
}
.dot__element--lg {
    width: 24px;
    height: 24px;
}
</style>