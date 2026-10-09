<script setup lang="ts">
import EventList from '../components/EventList.vue';
import { useEvents } from '../composables/useEvents';
import { computed, onMounted, ref, watch } from 'vue';
import { type Event } from '../types/event.ts';
import EventFilters from '../components/EventFilters.vue';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';

const {
    events,
    loading,
    error,
    loadEvents,
    removeEvent
} = useEvents()

const search = ref('')
const idCategory = ref<number | undefined>(undefined)
const eventToDelete = ref<Event | null>(null)

onMounted(async () => {
    await loadEvents()
})

/** Shows the empty state only after event loading has finished. */
const isEmpty = computed(() => {
    return !loading.value && events.value.length === 0
})

/** Reloads events when either search text or category selection changes. */
watch([search, idCategory], 
    async ([searchValue, categoryValue]) => {
        await loadEvents({
            search: searchValue || undefined,
            idCategory: categoryValue
        })
    },
    { immediate: true }
)

/** Clears the active search and category filters. */
function handleResetRequest() {
    search.value = ''
    idCategory.value = undefined
}

/** Opens deletion confirmation for the selected event. */
function handleDeleteRequest(event: Event) {
    eventToDelete.value = event
}

/** Deletes the selected event and clears the pending selection. */
async function handleConfirmDelete() {
    if (!eventToDelete.value) return

    try {
        await removeEvent(eventToDelete.value.idEvent)
        
    } finally {
        eventToDelete.value = null
    }
}

</script>
<template>
    <!-- Displays searchable, category-filtered events and handles deletion. -->
    <div class="d-flex flex-row align-items-center justify-content-between">
        <h1 class="d-flex gap-4">
            <span>Events</span>
            <i class="bi bi-clock"></i>
        </h1>
        <RouterLink class="btn btn-primary" :to="{ name: 'event-creation'}">
            <span class="d-none d-md-inline">Add event</span>
            <span class="d-inline d-md-none"><i class="bi bi-plus-lg"></i></span>
        </RouterLink>
    </div>

    <div class="d-none d-md-block">
        <CommonContainer :margin-bottom="true">
            <template #1>
                <EventFilters
                    :search="search"
                    :cat-id="idCategory"
                    @update:id-category="idCategory = $event"
                    @update:search="search = $event"
                    @reset="handleResetRequest"
                />
            </template>
        </CommonContainer>
    </div>
    
    <CommonContainer :margin-bottom="true">
        <template #1>
            <div v-if="isEmpty">
                No events found.
            </div>

            <div v-else-if="loading">Loading events...</div>
            <div v-else-if="error">
                {{ error }}
            </div>
            <EventList
                v-else
                :events="events"
                :for-dashboard="false"
                @delete-request="handleDeleteRequest"
            ></EventList>
        </template>
    </CommonContainer>
    
    <ConfirmDeletionModal
        v-if="eventToDelete"
        :object="'event'"
        @cancel="eventToDelete = null"
        @yes="handleConfirmDelete"
    />
</template>