<script setup lang="ts">
defineProps<{
    /**
     * Adds vertical margins below the container when true.
     */
    marginBottom: boolean
}>()
</script>
<template>
    <div class="common-container d-flex flex-column w-100 p-3 p-lg-4" :class="marginBottom ? 'my-3' : ''">
        <!-- Named content slot for the container body -->
        <div><slot name="1"></slot></div>
    </div>
</template>
<style lang="css" scoped>
    .common-container {
        background: var(--app-surface);
        border-radius: var(--app-radius-lg);
        box-shadow: var(--app-shadow);
        gap: var(--app-gap-md)
    }
</style>