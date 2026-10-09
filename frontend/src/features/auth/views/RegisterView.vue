<script setup lang="ts">
import AuthForm from '../components/AuthForm.vue';
import type { RegisterRequest } from '../types/auth.ts';
import { useAuth } from '../composables/useAuth.ts'
import { useRouter } from 'vue-router';
import { ref } from 'vue';

const router = useRouter()

const {
    error,
    addUser
} = useAuth()
const isSubmitting = ref(false)

/** Registers the user and navigates to login after successful registration. */
async function registerUser(request: RegisterRequest) {
    if (isSubmitting.value) {
        return
    }

    isSubmitting.value = true
    
    try {
        await addUser(request)

        router.push({
            name: 'login',
            query: {
                status: 'registered'
            }
        })
    } catch {} finally {
        isSubmitting.value = false
    }
}
</script>

<template>
    <!-- Presents the registration form and handles account-creation feedback. -->
    <div class="register__page d-flex flex-column align-items-center justify-content-center gap-3">
        <h1 class="fw-bold">Sign Up</h1>
        <div class="register__card d-flex flex-column justify-content-center">
            <AuthForm
                :isRegister="true"
                :buttonText="'Sign Up'"
                :isSubmitting="isSubmitting"
                @register="registerUser"
            >
            </AuthForm>
            <RouterLink
                class="text-center"
                :to="{ name: 'login' }"
            >
                Log in
            </RouterLink>
            <div  v-if="error" class="alert alert-danger">{{ error }}</div >
        </div>
        
    </div>
    
</template>

<style scoped>
.register__page {
    min-height: 70vh;
    padding: 2rem 1rem;
}

.register__card {
    width: min(100%, 420px);
    margin: 0 auto;
}
</style>