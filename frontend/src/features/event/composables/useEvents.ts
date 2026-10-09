import { ref } from 'vue'
import { getEvents, createEvent, updateEvent, deleteEvent, createEventQuick, getEventsByDate, getEventsInFuture} from '../api/eventApi'
import { type Event, type EventCreateQuickRequest, type EventCreateRequest, type EventFilter } from "../types/event"
import { getEventErrorMessage, getEventListErrorMessage } from '../api/eventApiError'


/** Manages event collections and their loading and error state. */
export function useEvents() {

    const events = ref<Event[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Loads events matching the optional event filter.
     * @param params Optional event filters.
     */
    async function loadEvents(params?: EventFilter) {
        loading.value = true
        error.value = null

        try {
            const result = await getEvents(params)
            events.value = result ?? []
        } catch (e) {
            error.value = getEventListErrorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /**
     * Creates an event and appends it to the current collection.
     * @param request Event details to create.
     */
    async function addEvent(request: EventCreateRequest) {
        error.value = null
        loading.value = true

        try {
            const createdEvent = await createEvent(request)

            events.value.push(createdEvent)

            return createdEvent
        } catch (e) {
            error.value = getEventErrorMessage(e)
            throw e
        }
    } 

    /**
     * Creates a quick event and appends it to the current collection.
     * @param request Event details to create.
     */
    async function addEventQuick(request: EventCreateQuickRequest) {
        error.value = null
        loading.value = true

        try {
            const createdEvent = await createEventQuick(request)
            events.value.push(createdEvent)
            return createdEvent
        } catch (e) {
            error.value = getEventErrorMessage(e)
        }
    }

    /**
     * Saves event changes and replaces the matching collection entry.
     * @param idEvent ID of the event to update.
     * @param request Updated event details.
     */
    async function editEvent(idEvent: number, request: EventCreateRequest) {
        error.value = null
        loading.value = true

        try {
            const updatedEvent = await updateEvent(idEvent, request)
            const index = events.value.findIndex(event => event.idEvent === idEvent)

            if (index !== -1) {
                events.value[index] = updatedEvent
            }

            return updatedEvent
        } catch (e) {
            error.value = getEventErrorMessage(e)
            throw e
        }
    } 

    /**
     * Deletes an event by ID.
     * @param idEvent ID of the event to delete.
     */
    async function removeEvent(idEvent: number) {
        error.value = null
        loading.value = false

        try {
            await deleteEvent(idEvent)

            events.value.filter(event => event.idEvent !== idEvent)
        } catch (e) {
            error.value = getEventErrorMessage(e)
            throw e
        }
    }

    /**
     * Loads events scheduled on the given date.
     * @param date Date whose events should be loaded.
     */
    async function loadEventsByDate(date: Date) {
            error.value = null
            loading.value = true
            
        try {
            events.value = await getEventsByDate(date)
        } catch (e) {

            error.value = getEventListErrorMessage(e)

        } finally {
            loading.value = false
        }
        
    }

    /** Loads future events for reminder target selection. */
    async function loadEventsInFuture() {

        loading.value = true
        error.value = null

        try {
            const result = await getEventsInFuture()
            events.value = result ?? []
        } catch (e) {
            error.value = getEventListErrorMessage(e)
        } finally {
            loading.value = false
        }
        
    }

    return {
        events,
        loading,
        error,
        loadEvents,
        addEvent,
        addEventQuick,
        editEvent,
        removeEvent,
        loadEventsByDate,
        loadEventsInFuture
    }
}