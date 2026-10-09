<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import { useEvents } from '@/features/event/composables/useEvents';
import { useNotes } from '@/features/note/composables/useNotes';
import { useTasks } from '@/features/task/composables/useTasks';
import EventList from '@/features/event/components/EventList.vue';
import NoteContainer from '@/features/note/components/NoteContainer.vue';
import TaskList from '@/features/task/components/TaskList.vue';
import { useAuth } from '@/features/auth/composables/useAuth';
import { useReminders } from '@/features/reminder/composables/useReminders';
import { useNotifications } from '@/features/notification/composables/useNotifications';
import ReminderList from '@/features/reminder/components/ReminderList.vue';

const date = ref<string>('')

const { connect } = useNotifications()

const {
    tasks,
    loading: tasksLoading,
    error: tasksError,
    loadTasksByDate
} = useTasks()

const {
    events,
    loading: eventsLoading,
    error: eventsError,
    loadEventsByDate
} = useEvents()

const {
    notes,
    loading: loadingNotes,
    error: notesError,
    loadNotesByDate
} = useNotes()

const {
    reminders,
    loading: loadingReminders,
    error: remindersError,
    loadRemindersByDate
} = useReminders()

const {
    user,
    loadCurrentUser
} = useAuth()

onMounted(async () => {

    if (!user.value) {
        await loadCurrentUser()
    }

    const currentDate = new Date()
    date.value = (currentDate.getDate() + '-' + currentDate.getMonth() + '-' + currentDate.getFullYear());

    await Promise.allSettled([
        loadTasksByDate(currentDate),
        loadEventsByDate(currentDate),
        loadNotesByDate(currentDate),
        loadRemindersByDate(currentDate)
    ])

    connect()
})

</script>
<template>
    <!-- Presents the dashboard overview of tasks, events, notes, and reminders. -->
    <div class="row mb-4">
        <div class="col-12">
            <h2 class="fw-semibold ">Welcome back{{ user !== null ? ', ' + user.username : '' }}!</h2>
        </div>
    </div>
    <div class="dashboard__page">
        
        <div class="dashboard__schedule row align-items-stretch">
            <div class="col-12 col-lg-4 d-flex">
                <CommonContainer class="dashboard__panel dashboard__schedule-panel w-100 mb-lg-0 mb-4" :margin-bottom="false">
                    <template #1>
                        <div class="d-flex justify-content-between align-items-baseline flex-wrap">
                            <RouterLink :to="{ name: 'schedule'}" class="link-dark link-underline link-underline-opacity-0 link-opacity-50-hover mb-2">
                                <h2 class="d-flex gap-3 gap-xl-4">
                                    <span>Schedule</span>
                                    <i class="bi bi-calendar-week"></i>
                                </h2>
                                
                            </RouterLink>
                            <h3 class="date">{{ date }}</h3>
                        </div>

                        <div v-if="eventsLoading">Loading schedule...</div>
                        <div v-else-if="eventsError" class="alert alert-danger mb-0">
                            {{ eventsError }}
                        </div>
                        <div v-else-if="events.length == 0">
                            No events for today
                        </div>
                        <EventList
                            v-else
                            :events="events"
                            :for-dashboard="true"
                        />
                    </template>
                </CommonContainer>
            </div>

            <div class="col-12 col-lg-5 d-flex mb-lg-0 mb-4">
                <div class="dashboard__stack w-100 h-100">
                    <CommonContainer class="dashboard__notes d-flex w-100" :margin-bottom="false">
                        <template #1>
                            <RouterLink :to="{ name: 'notes'}" class="link-dark link-underline link-underline-opacity-0 link-opacity-50-hover">
                                <h2 class="d-flex gap-3 gap-lg-4 mb-4">
                                    <span>Notes</span>
                                    <i class="bi bi-card-list"></i>
                                </h2>
                            </RouterLink>
                            <div v-if="loadingNotes">Loading notes...</div>
                            <div v-else-if="notesError" class="alert alert-danger mb-0">
                                {{ notesError }}
                            </div>
                            <div v-else-if="notes.length == 0">
                                No notes for today
                            </div>
                            <NoteContainer
                                v-else
                                :notes="notes"
                                :for-dashboard="true"
                            />
                        </template>
                    </CommonContainer>

                    <CommonContainer class="dashboard__panel dashboard__tasks d-flex w-100 align-items-stretch" :margin-bottom="false">
                        <template #1>
                            <RouterLink :to="{ name: 'tasks'}" class="link-dark link-underline link-underline-opacity-0 link-opacity-50-hover">
                                <h2 class="mb-3 d-flex gap-3 gap-lg-4">
                                    <span>Tasks</span> 
                                    <i class="bi bi-clipboard-check"></i>
                                </h2>
                            </RouterLink>
                            <div v-if="tasksLoading">Loading tasks...</div>
                            <div v-else-if="tasksError" class="alert alert-danger mb-0">
                                {{ tasksError }}
                            </div>
                            <div v-else-if="tasks.length == 0">
                                No tasks for today
                            </div>
                            <TaskList
                                v-else
                                :tasks="tasks"
                                :for-dashboard="true"
                            />
                        </template>
                    </CommonContainer>
                </div>
            </div>

            <div class="col-12 col-lg-3 d-flex flex-column gap-1">
                <div class="vstack mb-lg-4 mb-3 w-100 dashboard__actions">
                    <RouterLink class="btn btn-lg btn-primary fw-semibold d-flex gap-4 justify-content-center" :to="{ name: 'calendar'}">
                        <span>Calendar</span>
                        <i class="bi bi-calendar-date"></i>
                    </RouterLink>
                    <RouterLink class="btn btn-lg btn-primary fw-semibold d-flex gap-4 justify-content-center" :to="{ name: 'categories'}">
                        <span>Categories</span>
                        <i class="bi bi-grid"></i>
                    </RouterLink>
                    <RouterLink class="btn btn-lg btn-primary fw-semibold d-flex gap-4 justify-content-center mb-1" :to="{ name: 'reminders'}">
                        <span>Reminders</span>
                        <i class="bi bi-bell"></i>
                    </RouterLink>
                </div>
                <CommonContainer :margin-bottom="false" class="d-flex flex-fill">
                    <template #1>
                        <RouterLink :to="{ name: 'reminders'}" class="link-dark link-underline link-underline-opacity-0 link-opacity-50-hover">
                            <h2 class="mb-3 d-flex gap-3 gap-lg-4">
                                <span>Reminders</span>
                                <i class="bi bi-bell"></i>
                            </h2>
                        </RouterLink>
                        
                        <div v-if="loadingReminders">Loading reminders...</div>
                        <div v-else-if="remindersError" class="alert alert-danger mb-0">
                            {{ remindersError }}
                        </div>
                        <div v-else-if="reminders.length == 0">
                            No reminders for today
                        </div>
                        
                        <ReminderList
                            v-else
                            :reminders="reminders"
                            :for-dashboard="true"
                        />
                    </template>
                </CommonContainer>
            </div>
        </div>
    </div>
</template>

<style lang="css" scoped>

.dashboard__page {
    min-height: calc(100dvh - 7.75rem);
    display: flex;
}

.dashboard__schedule {
    flex: 1;
    min-height: 0;
    width: 100%;
    align-items: stretch;
}

.dashboard__stack {
    min-height: 0;
    display: flex;
    flex-direction: column;
    gap: var(--app-gap-md);
}

.dashboard__panel {
    min-height: 0;
    flex: 1 1 0;
}

.dashboard__notes {
    flex: 0 0 auto;
}

.dashboard__tasks {
    min-height: auto;
}

.dashboard__actions {
    align-self: stretch;
    flex: 0 0 auto;
    gap: var(--app-gap-md);
}

.date {
    color: var(--app-text-muted);
}

@media (max-width: 992px) {
    .dashboard__page {
        min-height: auto;
    }
}

@media (max-width: 1200px) {
    .date {
        font-size: larger;
    }
}
</style>