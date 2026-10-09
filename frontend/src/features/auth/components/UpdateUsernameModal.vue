<script setup lang="ts">
import { onMounted, ref } from 'vue';

/** References the username input and holds its value and validation message. */
const usernameInput = ref<HTMLInputElement | null>()
const username = ref<string | null>(null)
const error = ref<string | null>(null)

const emit = defineEmits<{
    /** Emits the new username when it is submitted. */
    'save': [username: string]
    /** Emits when the user closes or cancels the modal. */
    'cancel': []
}>()

/** Focuses the username field when the modal is mounted. */
onMounted(() => {
    usernameInput.value?.focus
})

/** Emits a non-empty username or displays a validation message. */
function handleSave() {
    if (username.value) {
        emit('save', username.value)
    } else {
        error.value = 'Input field is empty. Please type new username'
    }
}

</script>
<template>
    <!-- Collects a replacement username and emits save or cancel actions. -->
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5>Change your username</h5>
                    <button
                        type="button"
                        class="btn-close"
                        @click="emit('cancel')"
                    ></button>
                </div>

                <div class="modal-body">
                    <input v-model="username" ref="usernameInput" class="form-control" type="text" placeholder="New username" />
                    <div v-if="error" class="alert alert-danger">{{ error }}</div>
                </div>

                <div class="modal-footer justify-content-between">
                    <button
                        type="button"
                        class="btn btn-danger"
                        @click="emit('cancel')"
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        class="btn btn-primary"
                        @click="handleSave"
                    >
                        Save
                    </button>
                </div>
            </div>
        </div>
        
    </div>  
    <div class="modal-backdrop fade show"></div>
</template>
<style scoped>

</style>