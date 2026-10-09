<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { LoginRequest, RegisterRequest } from '../types/auth';

/** Holds the values entered into the login or registration form. */
const form = reactive({
    email: '',
    username: '',
    password: ''
})

/** Contains validation messages keyed by field name. */
const errors = ref<Record<string, string>>({})

const props = defineProps<{
    /** Shows the username field and emits registration data when enabled. */
    isRegister: boolean
    /** Label displayed on the submit button. */
    buttonText: string
    /** Disables the submit button while a request is in progress. */
    isSubmitting?: boolean
}>()

const emit = defineEmits<{
    /** Emits valid login credentials to the parent. */
    login: [loginData: LoginRequest]
    /** Emits valid registration details to the parent. */
    register: [registerData: RegisterRequest]
}>()

/** Validates the current form and emits the matching authentication request. */
async function handleSubmit() {

    if (!validate()) return

    if (props.isRegister) {
        emit('register', {
            email: form.email, 
            username: form.username,
            password: form.password
        })
    } else {
        emit('login', {
            email: form.email,
            password: form.password
        })
    }
}

/** Validates email, optional username, and password fields. */
function validate() {
    errors.value = {}

    const emailRegex = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/

    if (!form.email) {
        errors.value.email = 'Email is required'
    } else if (!emailRegex.test(form.email)) {
        errors.value.email = 'Invalid email'
    }

    if (props.isRegister && form.username) {
        if (form.username.length > 50) {
            errors.value.username = 'Username is too long'
        }
    }

    if (!form.password) {
        errors.value.password = 'Password is required'
    } else if (form.password.length < 8) {
        errors.value.password = 'Password must be at least 8 characters'
    }

    /* 
    Check if form passed all validation rules
    - extracts all keys from an object and returns them as an array
    - counts how many keys (errors) are in the array
    - checks if there are no errors (0 array elements)
    */
    return Object.keys(errors.value).length === 0

}

</script>
<template>
    <!-- Renders the shared login or registration form and emits submitted credentials. -->
    <form
        class="form d-flex flex-column gap-3 p-3"
        @submit.prevent="handleSubmit"
    >
        
        <div>
            <label class="form-label" for="email">Email:</label>
            <input
                id="email"
                v-model="form.email"
                type="text"
                class="form-control"
            />
        </div>
        <div v-if="errors.email" class="alert alert-danger my-2">
            {{ errors.email }}
        </div>

        <div v-if="isRegister">
            <label class="form-label" for="username">Username:</label>
            <input
                
                id="username"
                v-model="form.username"
                type="text"
                class="form-control"
            />
        </div>
        <p v-if="errors.username" class="text-danger small mb-0">
            {{ errors.username }}
        </p>

        <div>
            <label class="form-label" for="password">Password:</label>
            <input
                id="password"
                v-model="form.password"
                type="password"
                class="form-control"
            />
        </div>
        
        <p v-if="errors.password" class="text-danger small mb-0">
            {{ errors.password }}
        </p>
        

        <button
            type="submit"
            class="btn btn-primary mt-3"
            :disabled="props.isSubmitting"
        >
            {{ props.isSubmitting ? 'Please wait...' : buttonText }}
    </button>
    </form>
</template>
<style scoped>
.form {
    width: 100%;
}
</style>