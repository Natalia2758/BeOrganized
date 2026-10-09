package com.example.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task.model.Subtask;
import com.example.task.model.Task;
import com.example.user.model.UserEntity;

/**
 * 
 * <b>SubtaskRepository</b>
 * <br>
 * Repository interface for Subtask entities. Used to perform CRUD operations on subtasks data within the database.
 * 
 */
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    /**
     * Finds all subtasks assigned to the specific task
     * @param task  a Task object
     * @return      a list of Subtask objects
     */
    List<Subtask> findByTask(Task task);

    /**
     * Deletes all subtasks assigned to the specific task
     * @param task  a Task object
     * @return      a number of deleted records
     */
    @Modifying
    @Query("DELETE FROM Subtask s WHERE s.task = :task")
    int deleteByTask(@Param("task") Task task);

    /**
     * Deletes all subtasks assigned to the user tasks
     * @param user  an object with user data
     * @return      a number of removed records from the table
     */
    @Modifying
    @Query ("DELETE FROM Subtask s WHERE s.task.user = :user")
    int deleteByUser(@Param("user") UserEntity user);
}
