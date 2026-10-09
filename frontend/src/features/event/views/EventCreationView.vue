<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useCategories } from '@/features/category/composables/useCategories';
import type { EventCreateRequest } from '../types/event';
import { useEvents } from '../composables/useEvents';
import { useRoute, useRouter } from 'vue-router';
import EventForm from '../components/EventForm.vue';
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue';

const {
    categories,
    loading: categoriesLoading,
    error: categoriesError,
    loadCategories
} = useCategories()

const {
    error,
    addEvent
} = useEvents()

const router = useRouter()
const route = useRoute()
const initialDate: string | undefined = route.query.date && typeof route.query.date === 'string' ? route.query.date : undefined
const showModal = ref<boolean>(false)

onMounted(() => {
    loadCategories()
})

/** Creates an event and returns to the event list after success. */
async function createEvent(request: EventCreateRequest) {

    try {
        const createdEvent = await addEvent(request)

        router.push({
            name: 'event-details',
            params: {
                idEvent: createdEvent.idEvent
            }
        })
    } catch { }
}

/** Opens confirmation before discarding the event form. */
function handleCancelRequest() {
    showModal.value = true
}

/** Returns to the previous page after cancellation is confirmed. */
function handleConfirmCancel() {
    router.back()
}

</script>
<template>
    <!-- Loads event form data and handles event creation and cancellation. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Create Event</h1>

        <div v-if="categoriesLoading">
                Loading categories...
        </div>
        <div class="alert alert-danger" v-else-if="categoriesError">
            {{ categoriesError }}
        </div>
            
        <EventForm
            :categories="categories ?? []"
            :initial-date="initialDate ? initialDate : undefined"
            :submit-label="'Add Event'"
            @submit="createEvent"
            @cancel="handleCancelRequest"
        ></EventForm>

        <div class="alert alert-danger my-2" v-if="error">
            {{ error }}
        </div>
    </div>

    <ConfirmCancellingModal
        v-if="showModal === true"
        :object="'task'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />
    
</template>