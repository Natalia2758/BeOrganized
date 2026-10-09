package com.example.event.dto;

/**
 * 
 * <b>EventFilter</b><br>
 * A DTO record used to transfer parameters to filter events
 * 
 * 
 * @param idCategory    ID of the selected category
 * @param search        characters by which event titles are filtered
 */
public record EventFilter(
    Long idCategory,
    String search
) {

}
