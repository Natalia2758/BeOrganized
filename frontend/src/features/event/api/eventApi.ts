import http from "@/common/api/http"
import type { Event, EventCreateQuickRequest, EventCreateRequest, EventFilter } from "../types/event"
import { toApiError } from "@/common/api/apiError"


/**
 * Retrieves events matching the optional filter.
 * @param params Optional event filters.
 */
export async function getEvents(params?: EventFilter): Promise<Event[]> {

    try {
        const response = await http.get<Event[]>('/events', { params })

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves one event by ID.
 * @param idEvent ID of the event to retrieve.
 */
export async function getEventDetails(idEvent: number): Promise<Event> {
    try {
        const response = await http.get<Event>(`/events/${idEvent}`)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates an event using the full event form.
 * @param request Event details to create.
 */
export async function createEvent(request: EventCreateRequest): Promise<Event> {

    try {
        const response = await http.post<Event>('/events/create', request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Creates an event using the quick-create form.
 * @param request Event details to create.
 */
export async function createEventQuick(request: EventCreateQuickRequest): Promise<Event> {
    try {
        const response = await http.post<Event>('/events/create-quick', request)
        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Replaces the editable details of an event.
 * @param idEvent ID of the event to update.
 * @param request Updated event details.
 */
export async function updateEvent(idEvent: number, request: EventCreateRequest): Promise<Event> {

    try {
        const response = await http.put(`/events/${idEvent}`, request)

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Deletes an event by ID.
 * @param idEvent ID of the event to delete.
 */
export async function deleteEvent(idEvent: number): Promise<void> {

    try {
        await http.delete(`/events/${idEvent}`)
    } catch (error) {
        throw toApiError(error)
    }
}

/**
 * Retrieves events for a local calendar date.
 * @param date Local calendar date to query.
 */
export async function getEventsByDate(date: Date): Promise<Event[]> {
    try {
        const events = await http.get<Event[]>('/events/by-date', {
            params: {
                year: date.getFullYear(),
                month: date.getMonth() + 1,
                day: date.getDate()
            }
        })
        return events.data
    } catch (error) {
        throw toApiError(error)
    }
}

/** Retrieves future events for reminder target selection. */
export async function getEventsInFuture(): Promise<Event[]> {
    try {
        const response = await http.get<Event[]>('/events/future')

        return response.data
    } catch (error) {
        throw toApiError(error)
    }
}
