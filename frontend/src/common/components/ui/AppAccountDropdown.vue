<script setup lang="ts">
import { Dropdown } from 'bootstrap'
import { onMounted, ref } from 'vue'

/**
 * Renders the account-actions dropdown and emits the selected action to its parent.
 */
const emit = defineEmits<{
    /**
     * Emits a logout request to the parent.
     */
    'logout': []
    /**
     * Emits a request to delete an account.
     */
    'deleteAccount': []
    /**
     * Emits a request to update username
     */
    'updateUsername': []
}>()

/**
 * References the account button used to initialize and control the Bootstrap dropdown.
 */
const accountButton = ref<HTMLElement | null>(null)

/**
 * Initializes the Bootstrap dropdown and prevents the anchor from navigating on click.
 */
onMounted(() => {
    if (!accountButton.value) return

    /**
     * Dropdown menu with user account actions.
     */
    const dropdown = Dropdown.getOrCreateInstance(accountButton.value)

    accountButton.value.addEventListener('click', (event) => {
        event.preventDefault()
        event.stopPropagation()

        dropdown.toggle()
    })
})
</script>
<template>
     <a
        class="dropdown-toggle nav-link fs-4"
        role="button"
        href="#"
        aria-expanded="false"
        ref="accountButton"
        data-bs-toggle="dropdown"
    >
        <i class="bi bi-person-fill"></i>
    </a>
    <ul class="dropdown-menu">
        <!--
            Triggered on click
            @event logout
        -->
        <li><button class="dropdown-item" @click="emit('logout')">Log out</button></li>
        <!--
            Triggered on click
            @event updateUsername
        -->
        <li><button class="dropdown-item" @click="emit('updateUsername')">Edit username</button></li>
        <!--
            Triggered on click
            @event deleteAccount
        -->
        <li><button class="dropdown-item" @click="emit('deleteAccount')">Delete account</button></li>
    </ul>
</template>