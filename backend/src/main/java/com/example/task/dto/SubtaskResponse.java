package com.example.task.dto;

/**
 * 
 * <b>SubtaskResponse<b><br>
 * 
 * A DTO record used to send subtask data to the frontend.
 * 
 * @param idSubtask database ID of the subtask
 * @param title     title of the subtask
 * @param completed completion status of the subtask
 */
public record SubtaskResponse(
    Long idSubtask,
    String title,
    boolean completed
) {

}
