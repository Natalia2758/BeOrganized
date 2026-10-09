<script setup lang="ts">
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { useEvent } from '../composables/useEvent';
import { onMounted, ref } from 'vue';
import { useEvents } from '../composables/useEvents';
import EventContainer from '../components/EventContainer.vue';
import CategoryDot from '@/features/category/components/CategoryDot.vue';
import { useIsMobile } from '@/common/composables/useIsMobile.ts';
import ConfirmDeletionModal from '@/common/components/ui/ConfirmDeletionModal.vue';


const route = useRoute()
const router = useRouter()

const { isMobile } = useIsMobile()

const {
    event,
    loading,
    error,
    loadEvent,
    invalidEventId
} = useEvent()

const {
    error: eventsError,
    removeEvent
} = useEvents()

const showDeletionModal = ref(false)

onMounted(async () => {
    const idEvent = Number(route.params.idEvent)

    if (Number.isNaN(idEvent)) {
        invalidEventId()
        return
    }
    await loadEvent(idEvent)
    
})

/** Deletes the displayed event and returns to the event list. */
async function handleConfirmDelete() {
    if (!event.value) return

    try {
        await removeEvent(event.value.idEvent)

        router.push({
            name: 'events'
        })
     
    } finally {
        showDeletionModal.value = false
    }
}

/** Opens the event deletion confirmation modal. */
function handleDeleteRequest() {
    showDeletionModal.value = true
}

</script>
<template>
    <!-- Loads and displays an event with its edit and deletion actions. -->
    <div class="mt-5 mt-lg-3">
        <div class="d-flex flex-row align-items-center justify-content-between gap-3 my-lg-4">
            <RouterLink
                class="btn btn-subtle"
                :to="{ name: 'events'}"
            >
                <i class="bi bi-arrow-left"></i>
            </RouterLink> 
            <div v-if="loading">Loading event...</div>
            <div v-else class="event__title-container d-flex flex-row justify-content-center align-items-center gap-2">
                <div class="event__title"  :class="isMobile ? ' h4' : ' h2'" >{{ event?.title}}</div>
                <CategoryDot
                    :color="event?.category?.color ?? null"
                    :size="'lg'"
                />
            </div>
            <div class="event__actions flex-column flex-md-row d-flex gap-3">
                <RouterLink
                    class="btn btn-secondary"
                    :to="{
                        name: 'event-update',
                        params: { 
                            idEvent: event?.idEvent
                        }
                    }"
                >
                    <span class="d-none d-md-inline">Edit</span>
                    <span class="d-inline d-md-none"><i class="bi bi-pencil-square"></i></span>
                </RouterLink>

                <button
                    class="btn btn-danger"
                    type="button"
                    @click="handleDeleteRequest">
                    <span class="d-none d-md-inline">Delete</span>
                    <span class="d-inline d-md-none"><i class="bi bi-trash-fill"></i></span>
                </button>
            </div>
        </div>
        
        <div v-if="loading">Loading event...</div>
        <div class="alert alert-danger my-2" v-else-if="error">
            {{ error }}
        </div>
        <div class="alert alert-danger my-2" v-else-if="eventsError">
            {{ eventsError }}
        </div>

        <EventContainer
            v-else-if="event"
            :event="event"
        />

        <div class="d-flex justify-content-end">
            <RouterLink
                :to="{
                    name: 'reminder-creation',
                    params: {
                        idEventTask: event?.idEvent
                    },
                    query: {
                        source: 'event'
                    }
                }"
                class="btn btn-primary"
            >
                Add reminder
            </RouterLink>
        </div>
    </div>

    <ConfirmDeletionModal
        v-if="showDeletionModal"
        :object="'event'"
        @cancel="showDeletionModal = false"
        @yes="handleConfirmDelete"
    />
</template>
<style scoped>
.event__title-container {
    min-width: 0;
    flex: 1 1 auto;
}

.event__title {
    margin: 0;
    overflow-wrap: anywhere;
}

.event__actions {
    flex-shrink: 0;
}
</style>