<script setup lang="ts">
import { useIsMobile } from '@/common/composables/useIsMobile';

const { isMobile } = useIsMobile()

const props = defineProps<{
    /** Visible date range, or null until schedule data is available. */
    weekStart: Date | null
    weekEnd: Date | null
    /** Selected day shown in the compact mobile header. */
    today: Date | null
}>()

const emit = defineEmits<{
    /** Requests week navigation on larger screens. */
    previousWeek: []
    nextWeek: []
    /** Requests day navigation on mobile screens. */
    previousDay: []
    nextDay: []
}>()

/** Chooses day or week navigation based on the current viewport. */
function handleNext() {
    isMobile.value ? emit('nextDay') : emit('nextWeek')
}

/** Chooses day or week navigation based on the current viewport. */
function handlePrevious() {
    isMobile.value ? emit('previousDay') : emit('previousWeek')
}

</script>

<template>
    <!-- Displays the schedule date range and week or day navigation controls. -->
    <header class="schedule__header ">
        <div class="schedule__toolbar d-flex flex-row gap-2 justify-content-between">
           
            <button
                class="btn"
                @click="handlePrevious"
            >
                <i class="bi bi-caret-left-fill"></i>
            </button>

            <h2 v-if="weekStart && weekEnd" class="d-md-block d-none">
                {{ weekStart.toLocaleDateString('en-GB') }} - {{ weekEnd.toLocaleDateString('en-GB') }}
            </h2>
            <h2 class="d-md-none d-block">
                {{ props.today?.toLocaleDateString('en-GB') }}
            </h2>
            
            <button
                @click="handleNext"
                class="btn"  
            >
                <i class="bi bi-caret-right-fill"></i>
            </button>
        </div>
    </header>
</template>