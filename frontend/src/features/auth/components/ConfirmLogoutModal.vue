<script setup lang="ts">

/** Reports whether the user confirms or cancels logout. */
const emit = defineEmits<{
    /** Emits when the user confirms logout. */
    'yes': []
    /** Emits when the user cancels logout. */
    'cancel': []
}>()
</script>
<template>
    <!-- Confirms logout and emits confirmation or cancellation. -->
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5>Are you sure you want to leave app?</h5>
                    <button
                        type="button"
                        class="btn-close"
                        @click="emit('cancel')"
                    ></button>
                </div>

                <div class="modal-footer justify-content-between">
                    <button
                        type="button"
                        class="btn btn-secondary"
                        @click="emit('cancel')"
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        class="btn btn-danger"
                        @click="emit('yes')"
                    >
                        Yes
                    </button>
                </div>
            </div>
        </div>
        
    </div>  
    <div class="modal-backdrop fade show"></div>
</template>
<style scoped>

</style>