<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useAuth } from '../composables/useAuth';
import { type LoginRequest } from '../types/auth';
import { useRoute, useRouter } from 'vue-router';
import AuthForm from '../components/AuthForm.vue';

const {
    error,
    authenticateUser
} = useAuth()

const router = useRouter()
const route = useRoute()

const status = ref(route.query.status?.toString())
const info = ref<string | null>(null)
const isSubmitting = ref(false)

/** Displays a message when navigation includes a successful account action. */
onMounted(() => {
    if (status.value === 'logout') {
        info.value = 'You were successfully logged out!'
    } else if (status.value === 'delete') {
        info.value = 'Your account was successfully deleted!'
    } else if (status.value === 'registered') {
        info.value = 'You were successfully registered. Please log in'
    } else {
        info.value = null
    }
})

/** Authenticates the user and navigates to the dashboard on success. */
async function loginUser(request: LoginRequest) {
    if (isSubmitting.value) {
        return
    }

    isSubmitting.value = true
    try {
        await authenticateUser(request)

        router.push({
            name: 'dashboard',
        })
    } catch {
    } finally {
        isSubmitting.value = false
    }
}

</script>

<template>
    <!-- Presents the login form and handles authentication navigation. -->
    <div class="login__page d-flex flex-column align-items-center justify-content-center gap-3">
        <h1 class="fw-bold">Log In</h1>
        <div v-if="info" class="alert alert-info my-2">{{ info }}</div>
        <div class="login__card d-flex flex-column justify-content-center">
            <AuthForm
                :isRegister="false"
                :buttonText="'Log In'"
                :isSubmitting="isSubmitting"
                @login="loginUser"
            />
            <RouterLink class="text-center" :to="{ name: 'register'}">
                Register
            </RouterLink>
            <div v-if="error" class="alert alert-danger">{{ error }}</div >
        </div>
    </div>
</template>

<style scoped>
.login__page {
    min-height: 70vh;
    padding: 2rem 1rem;
}

.login__card {
    width: min(100%, 420px);
    margin: 0 auto;
}

</style>