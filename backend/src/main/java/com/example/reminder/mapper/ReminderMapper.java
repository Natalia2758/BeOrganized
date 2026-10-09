package com.example.reminder.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.config.MappingConfig;
import com.example.reminder.dto.ReminderCreationRequest;
import com.example.reminder.dto.ReminderResponse;
import com.example.reminder.model.Reminder;

/**
 * 
 * <b>ReminderMapper</b>
 * <br>
 * Mapper interface responsible for mapping Reminder objects to DTOs
 */
@Mapper(
    config = MappingConfig.class
)
public interface ReminderMapper {

    ReminderResponse toResponse(Reminder reminder);

    Reminder fromCreationRequesttoEntity(ReminderCreationRequest request);

    List<ReminderResponse> toResponseList(List<Reminder> reminders);

    
}
