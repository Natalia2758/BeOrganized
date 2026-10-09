<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useCategories } from '../composables/useCategories';
import { ref } from 'vue';
import type { CategoryRequest } from '../types/category';
import CategoryForm from '../components/CategoryForm.vue';
import ConfirmCancellingModal from '@/common/components/ui/ConfirmCancellingModal.vue';

const router = useRouter()
const error = ref<string | null>(null)
const showModal = ref(false)

const {
    addCategory
} = useCategories()

/** Creates the category and returns to the category list on success. */
async function createCategory(request: CategoryRequest) {

    try {
        await addCategory(request)

        router.push({
            name: 'categories',
        })
    } catch { }
}

/** Opens confirmation before discarding category creation. */
function handleCancelRequest() {
    showModal.value = true
}

/** Leaves the creation view after cancellation is confirmed. */
function handleConfirmCancel() {
    router.back()
}

</script>
<template>
    <!-- Coordinates category creation and navigation when the form is cancelled. -->
    <div class="mt-sm-4 mt-lg-0">
        <h1 class="mb-4">Create Category</h1>
        <CategoryForm
            @create="createCategory"
            @cancel="handleCancelRequest"
        />
        <p v-if="error">
            {{ error }}
        </p>
    </div>
   
    <ConfirmCancellingModal
        v-if="showModal"
        :object="'category'"
        @yes="handleConfirmCancel"
        @no="showModal = false"
    />

</template>