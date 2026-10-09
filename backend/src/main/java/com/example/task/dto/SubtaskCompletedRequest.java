package com.example.task.dto;

/**
 * 
 * <b>SubtaskCompletedRequest</b><br>
 * 
 * A record used to transfer changed completion status of the subtask
 * 
 * @param idSubtask ID of the subtask for which the status has been changed
 * @param completed boolean value representing changed status
 */
public record SubtaskCompletedRequest(
    Long idSubtask,
    Boolean completed
) {

}
