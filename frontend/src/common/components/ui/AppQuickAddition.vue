<script setup lang="ts">
import { useTasks } from '@/features/task/composables/useTasks';
import type { TaskCreateRequestQuick } from '@/features/task/types/task';
import { ref } from 'vue';
import QuickTaskModal from '@/features/task/components/QuickTaskModal.vue';
import { useEvents } from '@/features/event/composables/useEvents';
import QuickEventModal from '@/features/event/components/QuickEventModal.vue';
import type { EventCreateQuickRequest } from '@/features/event/types/event';
import QuickNoteModal from '@/features/note/components/QuickNoteModal.vue';
import type { NoteCreateRequestQuick } from '@/features/note/types/note';
import { useNotes } from '@/features/note/composables/useNotes';

/**
 * Provides the task creation action and its current error state.
 */
const {
    addTaskQuick,
    error: taskError
} = useTasks()

/**
 * Provides the event creation action and its current error state.
 */
const {
    addEventQuick,
    error: eventError
} = useEvents()

/**
 * Provides the note creation action and its current error state.
 */
const {
    addNoteQuick,
    error: noteError
} = useNotes()

/**
 * Controls display of the quick addition actions menu
 */
const showQuickMenu = ref(false)

/**
 * Controls display of the short task form
 */
const showQuickTaskModal = ref(false)
/**
 * Controls display of the short event form
 */
const showQuickEventModal = ref(false)
/**
 * Controls display of the short note form
 */
const showQuickNoteModal = ref(false)

/**
 * Indicates whether one of the quick-add forms is currently being submitted.
 */
const isSubmitted = ref(false)

/**
 * Holds an optional submission error message and is cleared when the task form opens.
 */
const submitError = ref<string | null>(null)

/**
 * Opens and closes the quick menu
 */
function toggleQuickMenu() {
    showQuickMenu.value = !showQuickMenu.value
}

/**
 * Closes the quick menu
 */
function closeQuickMenu() {
    showQuickMenu.value = false
}

/**
 * Opens a modal with a short task form
 */
function openQuickTaskModal() {
    submitError.value = null
    showQuickTaskModal.value = true
}

/**
 * Creates a task from the quick-add form data and closes its modal on success.
 * @param request an object with the task data provided by the user
 */
async function handleQuickTaskSubmit(request: TaskCreateRequestQuick) {

    isSubmitted.value = true

    try {
        await addTaskQuick(request)

        showQuickTaskModal.value = false
    } finally {
        isSubmitted.value = false
    }
}

/**
 * Opens a modal with a short event form
 */
function openQuickEventModal() {
    showQuickEventModal.value = true
}

/**
 * Creates an event from the quick-add form data and closes its modal on success.
 * @param request an object with the event data provided by the user
 */
async function handleQuickEventSubmit(request: EventCreateQuickRequest) {
    isSubmitted.value = true

    try {
        await addEventQuick(request)
        showQuickEventModal.value = false
    } finally {
        isSubmitted.value = false
    }
}

/**
 * Opens a modal with a short note form
 */
function openQuickNoteModal() {
    showQuickNoteModal.value = true
}

/**
 * Creates a note from the quick-add form data and closes its modal on success.
 * @param request an object with the note data provided by the user
 */
async function handleQuickNoteSubmit(request: NoteCreateRequestQuick) {
    isSubmitted.value = true

    try {
        await addNoteQuick(request)
        showQuickNoteModal.value = false
    } finally {
        isSubmitted.value = false
    }
}

</script>

<template>
    <div class="quick-addition">
        <button
            class="btn-addition btn btn-lg"
            type="button"
            @click="toggleQuickMenu"
        >
            <i class="bi bi-plus-lg"></i>
        </button>

        <div
            v-if="showQuickMenu"
            class="quick-addition__menu"
        >
            <button
                class="quick-addition__menu__item btn btn-outline-primary w-100 d-flex gap-2 justify-content-center"
                @click="openQuickTaskModal(); closeQuickMenu()"

            >
                <span>Add Task</span>
                <i class="bi bi-clipboard-check"></i>
            </button>
            
            <button
                class="quick-addition__menu__item btn btn-outline-primary w-100 d-flex gap-2 justify-content-center"
                @click="openQuickEventModal(); closeQuickMenu()"
            >
                <span>Add Event</span>
                <i class="bi bi-clock"></i>
            </button>
            
            <button
                class="quick-addition__menu__item btn btn-outline-primary w-100 d-flex gap-2 justify-content-center"
                @click="openQuickNoteModal(); closeQuickMenu()"
            >
                <span>Add Note</span>
                <i class="bi bi-card-list"></i>
            </button>
        </div>
    </div>

    <QuickTaskModal
        v-if="showQuickTaskModal"
        :isSubmitted="isSubmitted"
        :error="taskError"
        @submit="handleQuickTaskSubmit"
        @close="showQuickTaskModal = false"
    />

    <QuickEventModal
        v-if="showQuickEventModal"
        :isSubmitted="isSubmitted"
        :error="eventError"
        @submit="handleQuickEventSubmit"
        @close="showQuickEventModal = false"
    />

    <QuickNoteModal
        v-if="showQuickNoteModal"
        :isSubmitted="isSubmitted"
        :error="noteError"
        @submit="handleQuickNoteSubmit"
        @close="showQuickNoteModal = false"
    />
</template>
<style scoped>
.quick-addition {
    position: fixed;
    bottom: 1rem;
    left: 1rem;
    z-index: 1030;
}

.quick-addition__menu {
    position: absolute;
    bottom: calc(100% + 0.5rem);
    left: 0;

    display: flex;
    flex-direction: column;
    gap: 0.25rem;

    width: 180px;
    padding: 0.5rem;

    background: var(--bs-body-bg);
    border: 1px solid var(--bs-border-color);
    border-radius: 0.5rem;
    box-shadow: 0 0.5rem 1rem rgb(0 0 0 / 15%);
} 

.quick-addition__menu__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;

    width: 100%;
    padding: 0.5rem 0.75rem;

    border: 0;
    border-radius: 0.375rem;
    background: transparent;
}

.btn-addition {
    background-color: var(--app-secondary);
    color: var(--app-secondary-text);
}

.btn-addition:hover, .btn-addition:active, .btn-addition[aria-expanded="true"], .btn-addition:focus-visible {
    background-color: var(--app-secondary-hover);
    color: var(--app-secondary-text-hover);
    border-color: var(--app-secondary-hover);
}

@media(max-width: 768px) {
    .app-quick-addition {
        left: 1.5rem;
        bottom: 1.5rem;
        max-width: calc(100vw - 2rem);
        max-height: calc(100vh - 2rem);
    }
}
</style>