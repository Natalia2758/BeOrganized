<script setup lang="ts">
/**
 * Confirms deletion of a named object and reports the user's choice to its parent.
 */
const props = defineProps<{
    /**
     * Name of the object that will be deleted if the user confirms.
     */
    object: string
}>()

const emit = defineEmits<{
    /**
     * Emits when the user confirms deletion.
     */
    'yes': []
    /**
     * Emits when the user cancels the deletion.
     */
    'cancel': []
}>()

</script>

<template>
    <div class="confirm-deletion-modal d-flex align-items-center justify-content-center" role="dialog" aria-modal="true">
        <!-- Clicking the backdrop cancels the deletion. -->
        <div class="confirm-deletion-modal__backdrop" @click="emit('cancel')"></div>

        <!-- Confirmation prompt and user actions -->
        <div class="confirm-deletion-modal__panel p-2">
            <div class="modal-header">
                <h5>Are you sure you want to delete {{ props.object }} ?</h5>
                <button
                    type="button"
                    class="btn-close"
                    @click="emit('cancel')"
                >
                </button>
            </div>

            <div class="modal-body d-flex justify-content-between gap-4">
                <button
                    type="button"
                    class="btn btn-secondary w-100"
                    @click="emit('cancel')"
                >
                    Cancel
                </button>
                <button
                    type="button"
                    class="btn btn-danger w-100"
                    @click="emit('yes')"
                >
                    Yes
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.confirm-deletion-modal {
    position: fixed;
    inset: 0;
    z-index: 1050;
}

.confirm-deletion-modal__backdrop {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
}

.confirm-deletion-modal__panel {
    position: relative;
    z-index: 1;
    width: min(24rem, calc(100vw - 2rem));
    min-height: 6rem;
    background: var(--app-surface, #fff);
    border-radius: var(--app-radius-md, 0.75rem);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
}

.confirm-deletion-modal__panel :deep(.modal-header),
.confirm-deletion-modal__panel :deep(.modal-body) {
    padding: 1rem;
}

</style>