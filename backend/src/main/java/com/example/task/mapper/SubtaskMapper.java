package com.example.task.mapper;

import org.mapstruct.Mapper;

import com.example.config.MappingConfig;
import com.example.task.dto.SubtaskResponse;
import com.example.task.model.Subtask;

/**
 * 
 * <b>SubtaskMapper</b>
 * <br>
 * Mapper interface responsible for mapping Subtask objects to DTOs
 */
@Mapper (
    config = MappingConfig.class
)
public interface SubtaskMapper {
    SubtaskResponse toResponse(Subtask subtask);
}
