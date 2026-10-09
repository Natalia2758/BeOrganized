package com.example.task.mapper;

import com.example.config.MappingConfig;
import com.example.task.dto.TaskResponse;
import com.example.task.model.Task;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * 
 * <b>TaskMapper</b>
 * <br>
 * Mapper interface responsible for mapping Task objects to DTOs. It uses SubtaskMapper class to map Subtask objects assigned to the task.
 */
@Mapper(
    config = MappingConfig.class,
    uses = SubtaskMapper.class
)
public interface TaskMapper {

    TaskResponse toResponse(Task task);
    List<TaskResponse> toResponseList(List<Task> tasks);
}
