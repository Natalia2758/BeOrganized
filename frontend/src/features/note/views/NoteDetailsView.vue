<script setup lang="ts">
import { watch } from 'vue';
import { useNote } from '../composables/useNote';
import { useRoute } from 'vue-router';
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import CategoryDot from '@/features/category/components/CategoryDot.vue';
import { useIsMobile } from '@/common/composables/useIsMobile';

const route = useRoute()

const {
    isMobile
} = useIsMobile()

const {
    note,
    loading,
    error,
    loadNote,
    invalidNoteId
} = useNote()

/** Reloads the note when navigation changes its route ID. */
watch(
    () => route.params.idNote,
    async (idNoteParam) => {
        const idNote = Number(idNoteParam)

        if (Number.isNaN(idNote)) {
            invalidNoteId()
            return
        }

        await loadNote(idNote)
    },
    { immediate: true}
)

</script>

<template>
    <!-- Loads and displays a note with its edit and delete actions. -->
    <div class="mt-5 mt-lg-3">
        <div class="d-flex flex-row align-items-center justify-content-between gap-2">
            <div class="d-flex flex-row gap-3 gap-md-5 align-items-center w-100">
                <RouterLink
                    class="btn btn-subtle"
                    :to="{ name: 'notes'}"
                >
                    <i class="bi bi-arrow-left"></i>
                </RouterLink> 
                <div class="note__title-date d-flex flex-row flex-wrap">
                    <div v-if="loading">Loading note...</div>
                    <div v-else class="note__title-container d-flex flex-row justify-content-center align-items-center gap-0 gap-md-2 col-12 col-sm-8">
                        <div :class="isMobile ? ' h4' : ' h2'">{{ note?.title ?? 'No title' }}</div>
                        <CategoryDot 
                            :color="note?.category?.color ?? null"
                            :size="isMobile ? 'md' : 'lg'"
                        />
                    </div>
                    
                    <h3 v-if="note?.date" class="note__date col-12 col-sm-4 mt-1 text-center">{{ note?.date }}</h3>
                </div>
            </div>
            
            <RouterLink
                class="icon-button btn btn-secondary"
                :to="{
                    name: 'note-update',
                    params: {
                        idNote: note?.idNote
                    }
                }"
            >
                <span class="d-none d-md-inline">Edit</span>
                <span class="d-inline d-md-none"><i class="bi bi-pencil-square"></i></span>
            </RouterLink>
        </div>
           
        <CommonContainer :margin-bottom="true">
            <template #1>
                <div v-if="loading">Loading note...</div>
                <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
                <div v-else>
                    {{ note?.content ?? 'No content available' }}
                </div>
            </template>
        </CommonContainer>  
    </div>
</template>

<style scoped>
.note__date {
    color: var(--app-text-muted);
}

.note__title-container {
    min-width: 0;
    flex: 1 1 auto;
}

.note__title-date {
    flex: 1 0 70%;
    min-width: 0;
    width: 100%;
}

@media(max-width: 768px) {

    .icon-button {
        min-width: fit-content;
        flex: 0 0 auto;
    }

    .note__date {
        font-size: large;
    }
}
</style>