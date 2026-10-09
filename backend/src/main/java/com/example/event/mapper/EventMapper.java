package com.example.event.mapper;

import com.example.config.MappingConfig;
import com.example.event.dto.EventResponse;
import com.example.event.model.Event;
import java.util.List;

import org.mapstruct.Mapper;

/**
 * 
 * <b>EventMapper</b>
 * <br>
 * Mapper interface responsible for mapping Event objects to DTOs
 */
@Mapper(config = MappingConfig.class)
public interface EventMapper {

    EventResponse toResponse(Event event);
    List<EventResponse> toResponseList(List<Event> events);
}
