<script setup lang="ts">
import { useAuth } from '@/features/auth/composables/useAuth';
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute()
const error = ref<string>('')
const {
    error: authError
} = useAuth()

onMounted(() => {
    if (route.query.isEmpty) {
        error.value = 'Page not found'
    } else if (route.query.error) {
        switch (route.query.error){
            case 'logout':
                error.value = authError.value ?? 'Logout was unsuccessful. Please try again.'
            case 'update-username':
                error.value = authError.value ?? 'Failed to update username. Please try again'
            case 'delete-account':
                error.value = authError.value ?? 'Failed to delete account. Please try again'
            default:
                error.value = route.query.error.toString()
        }
    }
})

</script>

<template>
    <!-- Error information -->
    <div class="alert alert-danger p-1 p-md-3">
        {{ error }}
    </div>
</template>