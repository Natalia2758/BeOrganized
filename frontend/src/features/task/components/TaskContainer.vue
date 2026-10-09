<script setup lang="ts">
import CommonContainer from '@/common/components/layout/CommonContainer.vue';
import { Priority, type Subtask, type SubtaskCompletedRequest, type Task } from '../types/task';
import SubtaskList from './SubtaskList.vue';
import { formatDateForDisplay, formatTimeForDisplay } from '@/common/utils/dateTimeUtils.ts';

const props = defineProps<{
    /** Task whose details and progress are displayed. */
    task: Task
    /** Subtasks rendered in the task detail list. */
    subtasks: Subtask[]
    /** Optional progress value supplied by the parent view. */
    progress?: number
}>()

const emit = defineEmits<{
    /** Forwards a subtask completion change to the parent view. */
    checked: [subtask: SubtaskCompletedRequest]
}>()

/** Maps task priority to the matching badge color. */
function determineBadgeColor() {
    if (!props.task.priority || props.task.priority === null) return ''
    if (props.task.priority == Priority.HIGH) {
        return '#f74525'
    } else if (props.task.priority == Priority.MEDIUM) {
        return '#fabd16'
    } else if (props.task.priority == Priority.LOW){
        return '#53fa16'
    }
}

</script>
<template>
    <div>
        <div class="row">
            <div class="col">
                <!-- Displays the task details and related task actions. -->
                <CommonContainer :margin-bottom="true">
                    <template #1>
                        <dl>
                            <dt>Description</dt><dd>{{ props.task.description ?? 'No description' }}</dd>
                            <dt>Priority</dt><dd><span class="badge" :style="'background-color: ' + determineBadgeColor() + '; color: var(--app-text);'">{{ props.task.priority }}</span></dd>
                            <dt>{{ props.task.repeated ? 'From date' : 'Date' }}</dt><dd>{{ props.task.startDate ? formatDateForDisplay(props.task.startDate) : 'No date' }}</dd>
                            <dt v-if="props.task.duration">Time</dt><dd v-if="props.task.duration">{{ props.task.timeStart ? formatTimeForDisplay(props.task.timeStart) : '' }} - {{ props.task.timeEnd ? formatTimeForDisplay(props.task.timeEnd) : '' }}</dd>
                            <dt v-if="props.task.repeated">Repeated</dt><dd v-if="props.task.repeated">{{ props.task.repeatedFrequency }}</dd>
                        </dl>
                    </template>
                </CommonContainer> 
            </div>
                   
            <div v-if="props.task.subtasks.length !== 0" class="col">
                <CommonContainer :margin-bottom="true">
                    <template #1>
                        <h4>Subtasks</h4>
                        <SubtaskList
                            :subtasks="props.subtasks"
                            @checked="emit('checked', $event)"
                        />
                    </template>
                    
                </CommonContainer>
            </div>
            
        </div>
        <div class="row">
            <div class="col-12 col-sm-6">
                <CommonContainer
                    v-if="!props.task.finished"
                    :margin-bottom="true"
                >
                    <template #1>
                        <div class="d-flex flex-row justify-content-between align-items-center">
                            <div class="px-4">Progress</div>
                            <h4 class="task-progress text-center">{{ props.task.progress + '%' }}</h4>
                        </div>
                        
                    </template>
                </CommonContainer>
                <CommonContainer
                    v-else
                    :margin-bottom="true"
                >
                    <template #1>
                        <div class="d-flex justify-content-between text-success fw-bold">
                            <div>COMPLETED</div>
                            <i class="bi bi-check-square"></i>
                        </div>
                    </template>
                </CommonContainer>
            </div>
        </div>
    </div>
</template>
<style scoped>
    .task-progress {
        flex-basis: 50%;
    }

</style>