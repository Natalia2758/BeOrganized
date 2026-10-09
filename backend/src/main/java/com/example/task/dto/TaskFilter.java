package com.example.task.dto;

/**
 * 
 * <b>TaskFilter</b><br>
 * A DTO record used to transfer parameters to filter tasks
 * 
 * @param finished      task's finish status
 * @param idCategory    ID of the selected category
 * @param search        characters by which task titles are filtered
 */
public record TaskFilter(
    Boolean finished,
    Long idCategory,
    String search
) {

}
