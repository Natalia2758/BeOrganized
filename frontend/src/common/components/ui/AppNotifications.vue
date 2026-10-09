<script setup lang="ts">
import NotificationList from '@/features/notification/components/NotificationList.vue';
import type { Notification } from '@/features/notification/types/notification';
import { Offcanvas } from 'bootstrap';
import { onBeforeUnmount, onMounted, ref } from 'vue';


/**
 * Displays notifications in a Bootstrap offcanvas and reports user actions to its parent.
 */
const props = defineProps<{
    /**
     * An ID of the offcanvas component
     */
    offcanvasId: string
    /**
     * Notifications that are to be displayed
     */
    notifications: Notification[]
}>()

const emit = defineEmits<{
    /**
     * Emits request to open notification details
     */
    'open': [Notification]
    /**
     * Emits request to close notifications offcanvas
     */
    'close': []
}>()

/**
 * Linked to the offcanvas HTML element.
 */
const offcanvasElement = ref<HTMLElement | null>(null)

let offcanvas: Offcanvas | null = null

/**
 * Registers for the Bootstrap event that fires after the offcanvas is hidden.
 */
onMounted(() => {
      
    offcanvasElement.value?.addEventListener(
        'hidden.bs.offcanvas',
        handleHidden
    )

})

/**
 * Removes the Bootstrap event listener before the component is unmounted.
 */
onBeforeUnmount(() => {
    offcanvasElement.value?.removeEventListener(
        'hidden.bs.offcanvas',
        handleHidden
    )

})

/**
 * Sends an event to close the notifications offcanvas and mobile navigation
 * @event close
 */
function handleHidden() {
    emit('close')
}

/**
 * Closes notifications offcanvas
 */
function close() {
    if (!offcanvasElement.value) return

    const instance = Offcanvas.getOrCreateInstance(
        offcanvasElement.value
    )

    instance.hide()
}

/**
 * Requests navigation to the selected notification's details, then closes the offcanvas.
 * @param notification notification selected by the user
 * @event open
 */
function handleOpen(notification: Notification) {
    emit('open', notification)
    close()
}

/**
 * Opens the notifications panel. Exposed so the parent can open it from its header button.
 */
function open() {
    if (!offcanvasElement.value) return

    const instance = Offcanvas.getOrCreateInstance(offcanvasElement.value)
    instance.show()
}

defineExpose({
    open
})

</script>

<template>
    <Teleport to="body">
        <div
            class="offcanvas offcanvas-end"
            tabindex="-1"
            :id="props.offcanvasId"
            ref="offcanvasElement"
            :aria-labelledby="`${props.offcanvasId}-title`"
        >
            <div class="offcanvas-header">
                <h3 class="offcanvas-title" :id="`${props.offcanvasId}-title`">Notifications</h3>
                <button
                    type="button"
                    class="btn-close"
                    aria-label="Close"
                    @click="close"
                ></button>
            </div>

            <div class="offcanvas-body">
                <!-- Notifications list -->
                 <!--
                    If notification is assigned to the task/event -> redirect to the task/event details
                    If notification is not assigned -> redirect to ReminderView
                 -->
                <div v-if="notifications.length == 0">
                    No notifications
                </div>
                <NotificationList
                    v-else
                    :notifications="props.notifications"
                    @open="handleOpen"
                    @close="close"
                />
            </div>
        </div>
    </Teleport>
</template>