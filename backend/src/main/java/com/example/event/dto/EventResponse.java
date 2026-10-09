package com.example.event.dto;

import com.example.category.dto.CategoryResponse;
import com.example.common.enums.RepeatedFrequency;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 
 * <b>EventResponse</b><br>
 * 
 * A DTO record used to send event data to the frontend.
 * 
 * @param idEvent           database ID of the event
 * @param title             title of the event
 * @param details           details about the event
 * @param category          category DTO object with category data to which the event is assigned
 * @param dateStart         date when the event starts
 * @param dateEnd           date when the event ends
 * @param duration          boolean value that determines if the event has specific time
 * @param timeStart         time when the event starts
 * @param timeEnd           time when the event ends
 * @param repeated          boolean value set to true if the event is repeated in time
 * @param repeatedFrequency enumeration representing the frequency with which the event is repeated in time
 * @param dates             all dates when an event is repeated until the end of year
 */
public record EventResponse(
    Long idEvent,
    String title,
    String details,
    CategoryResponse category,
    LocalDate dateStart,
    LocalDate dateEnd,
    Boolean duration,
    LocalTime timeStart,
    LocalTime timeEnd,
    Boolean repeated,
    RepeatedFrequency repeatedFrequency,
    List<LocalDate> dates
) {

}
