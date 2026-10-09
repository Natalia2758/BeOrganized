<script setup lang="ts">
/**
 * Provides navigation links to the application's main views.
 */
</script>

<template>
    <div class="py-3 my-2">
        <!-- Links to the main application sections -->
        <ul class="nav nav-underline justify-content-center flex-wrap border-bottom pb-3 mb-3">
            <li class="nav-item">
                <RouterLink :to="{ name: 'tasks'}" class="nav-link ">Tasks</RouterLink>
            </li>
            <li class="nav-item">
                <RouterLink :to="{ name: 'events'}" class="nav-link">Events</RouterLink>
            </li>
            <li class="nav-item">
                <RouterLink :to="{ name: 'notes'}" class="nav-link">Notes</RouterLink>
            </li>
            <li class="nav-item">
                <RouterLink :to="{ name: 'categories'}" class="nav-link">Categories</RouterLink>
            </li>
            <li class="nav-item">
                <RouterLink :to="{ name: 'calendar'}" class="nav-link">Calendar</RouterLink>
            </li>
            <li class="nav-item">
                <RouterLink :to="{ name: 'schedule'}" class="nav-link">Schedule</RouterLink>
            </li>
            <li class="nav-item">
                <RouterLink :to="{ name: 'reminders'}" class="nav-link">Reminders</RouterLink>
            </li>
        </ul>

        <!-- Application copyright -->
        <p class="text-center"><i class="bi bi-c-circle"></i><i> 2026 BeOrganized</i></p> 
        
    </div>
</template>