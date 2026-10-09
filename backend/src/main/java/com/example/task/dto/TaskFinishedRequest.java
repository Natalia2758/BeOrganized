package com.example.task.dto;

/**
 * 
 * <b>TaskFinishedRequest</b><br>
 * 
 * A record used to transfer changed finish status of the task
 * 
 * @param idTask    ID of the task for which the status has been changed
 * @param finished  boolean value representing changed status
 */
public record TaskFinishedRequest(
    Long idTask,
    boolean finished
) {
}