<script setup lang="ts">
import { reactive } from 'vue';
import { type CategoryRequest } from '../types/category';
import { ref } from 'vue';
import { colorPalette} from '../types/color';
import CategoryColorPicker from './CategoryColorPicker.vue';

/** Validation messages keyed by field name. */
const errors = ref<Record<string, string>>({})

/** Holds the new category's name and selected color. */
const form = reactive({
    name: '',
    color: colorPalette.blue.color
})

const emit = defineEmits<{
    /** Emits validated category details for creation. */
    create: [data: CategoryRequest]
    /** Requests that the parent cancel category creation. */
    cancel: []
}>()

/** Emits category details only after validation succeeds. */
function handleSubmit() {
    if (!validate()) return

    emit('create', {
        name: form.name,
        color: form.color
    })
}

/** Delegates cancellation to the parent view. */
function handleCancel() {
    emit('cancel')
}

/** Checks the required category name and color. */
function validate() {
    errors.value = {}

    if (!form.name) {
        errors.value.name = 'Name is required'
    } else if (form.name.length > 200) {
        errors.value.name = 'Name is too long'
    }

    if (!form.color) {
        errors.value.color = 'Color is required'
    }

    return Object.keys(errors.value).length === 0
}

</script>
<template>
    <!-- Collects category name and color and emits create or cancel actions. -->
    <form @submit.prevent="handleSubmit">
        <div>
            <div class="row mb-3">
                <div class="col col-6">
                    <label class="form-label" for="name">Name*</label>
                    <input
                        class="form-control"
                        id="name"
                        v-model="form.name"
                    />
                    <div v-if="errors.name" class="alert alert-danger mt-3">
                        {{ errors.name }}
                    </div>
                
                </div>
            </div>
            <div class="row mb-5">
                <div class="col col-6">
                    <label class="form-label" for="color">Choose color</label>
                            
                    <CategoryColorPicker
                        v-model="form.color"
                    />
                    
                    <div v-if="errors.color" class="alert alert-danger mt-3">
                        {{ errors.color }}
                    </div>
                        
                </div>
            </div>
        </div>
        <div class="row w-100">
            <div class="col">
                <button
                    class="btn btn-danger"
                    @click="handleCancel"
                    type="button"
                >
                    Cancel
                </button>
            </div>
            <div class="col col-3">
                <button 
                    class="btn btn-primary w-100"
                    type="submit"
                >
                    Create Category
                </button>
            </div>
        </div>
        
    </form>
</template>