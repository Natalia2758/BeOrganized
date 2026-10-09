<script setup lang="ts">
import type { Notification } from '../types/notification';
import NotificationListItem from './NotificationListItem.vue';

const props = defineProps<{
    /** Notifications to display. */
    notifications: Notification[]
}>()

const emit = defineEmits<{
    /** Forwards a request to mark a notification as opened. */
    'open': [Notification]
    /** Forwards a request to close the notification panel. */
    'close': []
}>()
</script>

<template>
    <!-- Renders notification entries and forwards open and close actions. -->
    <div class="px-3">
        <NotificationListItem 
            v-for="notification in notifications"
            :key="notification.idNotification"
            :notification="notification"
            @open="emit('open', $event)"
            @close="emit('close')"
        />
    </div>
</template>