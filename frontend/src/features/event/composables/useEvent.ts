import { ref } from "vue";
import { getEventDetails } from "../api/eventApi";
import type { Event } from "../types/event";
import { getEventErrorMessage } from "../api/eventApiError";

/** Loads and exposes the state of a single event. */
export function useEvent() {
    const event = ref<Event | null>(null)
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Fetches event details and tracks loading and API errors.
     * @param idEvent ID of the event to load.
     */
    async function loadEvent(idEvent: number) {
        loading.value = true
        error.value = null

        try {
            event.value = await getEventDetails(idEvent)
        } catch (e) {
            error.value = getEventErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /** Resets event state when the route ID cannot be parsed. */
    function invalidEventId() {
        error.value = 'Invalid event ID'
        event.value = null
        loading.value = false
    }

    return {
        event,
        loading,
        error,
        loadEvent,
        invalidEventId
    }
}