<script setup lang="ts">
import { useCategories } from '@/features/category/composables/useCategories';
import { useEvents } from '../composables/useEvents';
import { useEvent } from '../composables/useEvent';
import { onMounted, ref } from 'vue';
import type { EventCreateRequest } from '../types/event';
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
    editEvent
} = useEvents()

const {
    event,
    error,
    loading,
    loadEvent
} = useEvent()

const router = useRouter()
const route = useRoute()
const showModal = ref(false)

onMounted(async () => {

    const idEvent = Number(route.params.idEvent)

    await loadEvent(idEvent)
    await loadCategories()
})

/** Saves the edited event and returns to its details page. */
async function updateCurrentEvent(request: EventCreateRequest) {

    if(!event.value) return

    try {
        await editEvent(event.value.idEvent, request)

        router.push({
            name: 'event-details',
            params: {
                idEvent: event.value.idEvent
            }
        })
    } catch { }
}

/** Opens confirmation before discarding event edits. */
function handleCancelRequest() {
    showModal.value = true
}

/** Returns to the previous page after cancellation is confirmed. */
function handleConfirmCancel() {
    router.back()
}

</script>

<template>
    <!-- Loads an event for editing and handles save or cancellation. -->
    <div class="mt-5 mt-lg-0">
        <h1 class="mb-4">Edit Event</h1>

        <div v-if="loading">
                Loading event...
        </div>

        <div v-else-if="error">
            {{ error }}
        </div>

        <div v-else-if="categoriesLoading">
            Loading categories...
        </div>

        <div v-else-if="categoriesError">
            {{ categoriesError }}
        </div>
            
        <EventForm
            v-if="categories && event"
            :categories="categories"
            :initialEvent="event"
            submit-label="Save"
            @submit="updateCurrentEvent"
            @cancel="handleCancelRequest"
        />

    </div>

    <ConfirmCancellingModal
        v-if="showModal"
        :object="'event'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />
    
</template>