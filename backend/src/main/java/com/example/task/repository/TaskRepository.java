package com.example.task.repository;

import com.example.task.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.user.model.UserEntity;

/**
 * 
 * <b>TaskRepository</b>
 * <br>
 * Repository interface for Task entities. Used to perform CRUD operations on tasks data within the database.
 * 
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    
     /**
     * Retrieves all tasks that belong to the specific user
     * @param user  an object with user data
     * @return      a list of Task objects
     */
    List<Task> findByUser(UserEntity user);

    /**
     * Finds tasks that start on a specific date
     * @param date  a specific date
     * @param user  an object with user data
     * @return      a list of Task objects
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.subtasks WHERE t.startDate = :startDate AND t.user = :user")
    List<Task> findByStartDate(@Param("user") UserEntity user, @Param("startDate") LocalDate startDate);

    /**
     * Retrieves a list of filtered tasks according to criteria provided in parameters
     * @param user              an object with user data
     * @param finished          a finished status
     * @param idCategory        an ID of the category
     * @param search            a keyword typed by user to which the titles are compared
     * @return                  a list of filtered tasks
     */
    @Query("""
            SELECT t FROM Task t
            LEFT JOIN FETCH t.subtasks
            WHERE t.user = :user
            AND (:finished IS NULL OR t.finished = :finished)
            AND (:idCategory IS NULL OR t.category.idCategory = :idCategory)
            AND (:search IS NULL OR :search = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY
            CASE WHEN t.finished = false THEN 0 ELSE 1 END,
            CASE t.priority
                WHEN com.example.task.enums.Priority.HIGH THEN 0
                WHEN com.example.task.enums.Priority.MEDIUM THEN 1
                WHEN com.example.task.enums.Priority.LOW THEN 2
                ELSE 3
            END,
            t.startDate DESC
            """)
    List<Task> filterTasks(@Param("user") UserEntity user, @Param("finished") Boolean finished, @Param("idCategory") Long idCategory, @Param("search") String search);

    /**
     * Finds a task with fetched subtasks that belongs to the specific user by its ID
     * @param idTask   an ID of the requested task
     * @param idUser   an ID of the user
     * @return         a Task object
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.subtasks WHERE t.idTask = :idTask AND t.user.idUser = :idUser")
    Optional<Task> findByIdAndIdUser(@Param("idTask") Long idTask, @Param("idUser") Long idUser);

    /**
     * Finds a task with fetched category that belongs to the specific user by its ID
     * @param idTask    an ID of the requested task
     * @param idUser    an ID of the user
     * @return
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category WHERE t.idTask = :idTask AND t.user.idUser = :idUser")
    Optional<Task> findByIdAndIdUserWithCategory(@Param("idTask") Long idTask, @Param("idUser") Long idUser);

    /**
     * Finds tasks that start at the current or future date and time
     * @param user  an object with user data
     * @param date  a specific date
     * @param time  a specific time
     * @return      a list of tasks in presence and future
     */
    @Query("""
            SELECT t FROM Task t
            LEFT JOIN FETCH t.subtasks
            WHERE t.user = :user
            AND (t.startDate IS NULL OR t.startDate >= :date)
            AND (t.startDate IS NULL OR t.startDate > :date OR t.timeStart IS NULL OR t.timeStart > :time)
            """)
    List<Task> findWithFutureOrPresentDate(@Param("user") UserEntity user, @Param("date") LocalDate date, @Param("time") LocalTime time);

    /**
     * Checks if any other task already takes place during a specific time block
     * @param startDate a date
     * @param timeStart a start time
     * @param timeEnd   an end time
     * @return          true if a task within this time block already exists, false if there is no task during this time
     */
    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
            FROM Task t
            WHERE t.startDate = :startDate
            AND t.timeStart < :timeEnd
            AND t.timeEnd > :timeStart
            """)
    boolean existsOverlappingTimeBlock(@Param("startDate") LocalDate startDate, @Param("timeStart") LocalTime timeStart, @Param("timeEnd") LocalTime timeEnd);

    /**
     * Removes category relationship from all tasks assigned to the specific category
     * @param idCategory    an ID of the category that will be cleared
     * @param user          an object with user data
     */
    @Modifying
    @Query("UPDATE Task t SET t.category = null WHERE t.category.idCategory = :idCategory AND t.user = :user")
    void clearCategoryByCategoryAndUser(@Param("idCategory") Long idCategory, @Param("user") UserEntity user);

    /**
     * Deletes all user tasks
     * @param user  an object with user data
     * @return      a number of removed records from the table
     */
    @Modifying
    @Query("DELETE FROM Task t WHERE t.user = :user")
    int deleteByUser(@Param("user") UserEntity user);
}
