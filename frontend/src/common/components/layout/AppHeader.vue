<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router';
import { useNotificationStore } from '@/features/notification/stores/useNotificationStore.ts';
import { computed, ref } from 'vue';
import { NotificationStatus, type Notification } from '@/features/notification/types/notification.ts';
import { useReminder } from '@/features/reminder/composables/useReminder.ts';
import { Collapse } from 'bootstrap';
import AppAccountDropdown from '../ui/AppAccountDropdown.vue';
import ConfirmDeletionModal from '../ui/ConfirmDeletionModal.vue';
import ConfirmLogoutModal from '@/features/auth/components/ConfirmLogoutModal.vue';
import { useAuth } from '@/features/auth/composables/useAuth.ts';
import UpdateUsernameModal from '@/features/auth/components/UpdateUsernameModal.vue';
import AppNotifications from '../ui/AppNotifications.vue';

/**
 * Provides the current notification list and the action for marking a notification opened.
 */
const {
    notifications,
    markAsOpened
} = useNotificationStore()

/**
 * Provides the reminder action used to synchronize opened status with the backend.
 */
const {
    markAsOpened: markReminderAsOpened
} = useReminder()

/**
 * Provides account actions for logout, deletion, and username changes.
 */
const {
    logoutUser,
    removeAccount,
    editUsername
} = useAuth()

const router = useRouter()

/**
 * Linked to the navigation links' components. 
 */
const navLinks = ref<HTMLElement | null>(null)

/**
 * Controls display of the mobile navigation component.
 */
const isNavOpen = ref(false)
/**
 * Controls display of the modal used to confirm deletion.
 */
const showDeletionConfirmModal = ref(false)
/**
 * Controls display of the modal used to confirm user logout.
 */
const showLogoutConfirmModal = ref(false)
/**
 * Controls display of the modal used to change username.
 */
const showUpdateUsernameModal = ref(false)

/**
 * Linked to the AppNotifications component.
 */
const notificationsRef = ref<InstanceType<typeof AppNotifications> | null>(null)

/**
 * Counts notifications that have been sent but not yet opened.
 */
const unreadNotifications = computed(() =>
    notifications.filter(
        notification => notification.status === NotificationStatus.SENT
    ).length
)

/**
 * Closes mobile naviagation if opened.
 */
function closeMobileNav() {
    if (!navLinks.value) {
        return
    }
    const collapse = Collapse.getOrCreateInstance(navLinks.value)
    collapse.hide()
    isNavOpen.value = false
}

/**
 * Toggles mobile navigation into opposed state.
 */
function toggleMobileNav() {
    if (!navLinks.value) {
        return
    }
    const collapse = Collapse.getOrCreateInstance(navLinks.value)
    if (isNavOpen.value) {
        collapse.hide()
    } else {
        collapse.show()
    }

    isNavOpen.value = !isNavOpen.value
}

/**
 * Attempts to sync the opened status with the reminder service, then marks it opened locally.
 * @param notification a notification which state is to be changed
 */
async function handleOpenNotificationRequest(notification: Notification) {
    try {
        await markReminderAsOpened(notification.reminder.idReminder)
    } catch { }
    markAsOpened(notification.idNotification)
}

/**
 * Opens notifications component.
 */
function openNotifications() {
    notificationsRef.value?.open()
}

/**
 * Shows modal with logout confirmation.
 */
function handleLogoutRequest() {
    showLogoutConfirmModal.value = true
}

/**
 * Logs out the user and redirects to the login page, or to the error page if logout fails.
 */
async function logOut() {
    try {
        await logoutUser()

        router.push({
            name: 'login',
            query: { status: 'logout' }
        })
    } catch {

        router.push({
            name: 'error',
            query: { error: 'logout'}
        })
    }
}

/**
 * Shows modal confirming account deletion.
 */
function handleDeleteAccountRequest() {
    showDeletionConfirmModal.value = true
}

/**
 * Deletes the user's account and redirects to the login page, or to the error page on failure.
 */
async function deleteAccount() {

    try {
        await removeAccount()

        router.push({
            name: 'login',
            query: { status: 'delete' }
        })
    } catch {
        router.push({
            name: 'error',
            query: { error: 'delete-account' }
        })
    }

}

/**
 * Shows modal with input to update username.
 */
function handleUpdateUsernameRequest() {
    showUpdateUsernameModal.value = true
}

/**
 * Saves the updated username, redirecting to the error page if the update fails.
 * @param username a new username provided by user
 */
async function saveUsername(username: string) {
    try {
        await editUsername(username)
    } catch {
        router.push({
            name: 'error',
            query: { error: 'update-username' }
        })
    }
}

</script>

<template>
    <nav class="navbar navbar-expand-lg fixed-top">
        <div class="container-fluid px-0 flex-wrap">

            <RouterLink to="/dashboard" class="navbar-brand px-3">
                <img src="@/assets/img/logo.png" alt="Logo" height="40">
            </RouterLink>

            <div class="d-flex flex-row me-3">
                <!--
                    Opens and closes mobile navigation.
                -->
                <button 
                    class="navbar-toggler"
                    type="button"
                    @click="toggleMobileNav"
                    :aria-expanded="isNavOpen"
                    aria-controls="nav-links"
                >
                    <span class="navbar-toggler-icon"></span>
                </button>
            </div>
            
            <!--
                Navigation
            -->
            <div class="collapse navbar-collapse navbar__links py-3 py-lg-0" id="nav-links" ref="navLinks">
                <ul class="navbar-nav nav nav-underline d-flex align-items-center justify-content-center justify-content-md-start gap-3 gap-lg-2 mb-0 flex-grow-1">
                    <li class="nav-item">
                        <RouterLink to="/dashboard" class="nav-link py-1" exact-active-class="active" @click="closeMobileNav">
                            <i class="bi bi-house-door-fill fs-5"></i>
                        </RouterLink>
                    </li>
                    <li class="nav-item">
                        <RouterLink to="/tasks" class="nav-link" exact-active-class="active" @click="closeMobileNav">Tasks</RouterLink>
                    </li>
                    <li class="nav-item">
                        <RouterLink to="/events" class="nav-link" exact-active-class="active" @click="closeMobileNav">Events</RouterLink>
                    </li>
                    <li class="nav-item">
                        <RouterLink to="/reminders" class="nav-link" exact-active-class="active" @click="closeMobileNav">Reminders</RouterLink>
                    </li>
                    <li class="nav-item">
                        <RouterLink to="/notes" class="nav-link" exact-active-class="active" @click="closeMobileNav">Notes</RouterLink>
                    </li>
                    <li class="dropdown nav-item ">
                        <!--
                            Dropdown with user account actions
                        -->
                        <AppAccountDropdown
                            @delete-account="handleDeleteAccountRequest"
                            @logout="handleLogoutRequest"
                            @update-username="handleUpdateUsernameRequest"
                        />
                    </li>
                </ul>
                <!--
                    Opens AppNotifications component on click
                -->
                <button
                    class="notifications btn pe-4 mt-2 mt-md-0 d-flex align-items-center justify-content-center"
                    type="button"
                    aria-controls="notifications"
                    @click="openNotifications"
                >
                    <div class="m-0 fs-4">
                        <i class="bi bi-bell-fill"></i>
                    </div>
                    <div class="notifications__count fs-5 align-self-center">
                        {{ unreadNotifications }}
                    </div>
                </button>
            </div>
        </div>
    </nav>
    <AppNotifications
        ref="notificationsRef"
        :offcanvas-id="'notifications'"
        :notifications="notifications"
        @open="handleOpenNotificationRequest"
        @close="closeMobileNav()"
    />
    <ConfirmDeletionModal
        v-if="showDeletionConfirmModal"
        :object="'account'"
        @cancel="showDeletionConfirmModal = false"
        @yes="deleteAccount"
    />
    <ConfirmLogoutModal
        v-if="showLogoutConfirmModal"
        @cancel="showLogoutConfirmModal = false"
        @yes="logOut"
    />
    <UpdateUsernameModal 
        v-if="showUpdateUsernameModal"
        @cancel="showUpdateUsernameModal = false"
        @save="saveUsername"
    />
</template>

<style scoped>

.notifications {
    flex-shrink: 0;
    color: var(--app-secondary);
    transition: color 0.2s ease, border-color 0.2s ease;
    &:hover {
        color: var(--app-secondary-hover);
    }
    &:active {
        border: none;
    }
    &:focus {
        border: none;
    }
}

.notifications__count {
    color: var(--app-text-muted);
   
}

.navbar__links {
    justify-content: space-between;
    min-width: 0;
    flex: 1;
    
}

.navbar__links > ul {
    min-width: 0;
}

@media (max-width: 991.98px) {
    .navbar__links {
        flex-basis: 100%;
        width: 100%;
        flex-grow: 1;
        justify-content: center;
    }

    .navbar__links > ul {
        width: 100%;
        justify-content: center;
    }

    .notifications {
        width: 100%;
    }
}

@media (min-width: 992px) {
    .navbar__links {
        justify-content: space-between;
    }
}

</style>