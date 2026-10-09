<script setup lang="ts">

/**
 * Confirms cancellation of an in-progress object and reports the user's choice to its parent.
 */
const props = defineProps<{
    /**
     * Name of the object that will not be saved if the user confirms cancellation.
     */
    object: string
}>()

const emit = defineEmits<{
    /**
     * Emits when the user confirms cancellation.
     */
    'yes': []
    /**
     * Emits when the user keeps editing the object.
     */
    'no': []
}>()

</script>

<template>
    <div class="confirm-cancel-modal d-flex align-items-center justify-content-center" role="dialog" aria-modal="true">
        <!-- Clicking the backdrop keeps the user in the current form. -->
        <div class="confirm-cancel-modal__backdrop" @click="emit('no')"></div>

        <!-- Unsaved-changes prompt and user actions -->
        <div class="confirm-cancel-modal__panel">
            <div class="modal-header">
                <button
                    type="button"
                    class="btn-close"
                    @click="emit('no')"
                ></button>
            </div>

            <div class="modal-body">
                <div class="text text-center w-100">Are you sure you want to cancel? </div>
                <div class="text text-center w-100 mb-4">The {{ props.object }} <b>will not</b> be saved.</div>
                
                <div class="d-flex gap-4">
                    <button
                        type="button"
                        class="btn btn-secondary w-50"
                        @click="emit('no')"
                    >
                        No
                    </button>
                    <button
                        type="button"
                        class="btn btn-danger w-50"
                        @click="emit('yes')"
                    >
                        Yes
                    </button>
                </div>
            </div>
            
        </div>
    </div>
</template>

<style scoped>
.confirm-cancel-modal {
    position: fixed;
    inset: 0;
    z-index: 1050;
}

.confirm-cancel-modal__backdrop {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
}

.confirm-cancel-modal__panel {
    position: relative;
    z-index: 1;
    width: min(24rem, calc(100vw - 2rem));
    min-height: 6rem;
    background: var(--app-surface, #fff);
    border-radius: var(--app-radius-md, 0.75rem);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
    font-size: large;
}

.confirm-cancel-modal__panel :deep(.modal-header),
.confirm-cancel-modal__panel :deep(.modal-body) {
    padding: 1rem;
}
</style>