package com.example.event.repository;

import com.example.event.model.Event;
import com.example.user.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 
 * <b>EventRepository</b>
 * <br>
 * Repository interface for Event entities. Used to perform CRUD operations on events data within the database.
 * 
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Retrieves all events that belong to the specific user
     * @param user  an object with user data
     * @return      a list of Event objects
     */
    @Query("SELECT e FROM Event e WHERE e.user = :user")
    List<Event> findByUser(@Param("user") UserEntity user);

    /**
     * Retrieves a list of filtered events according to criteria provided in parameters
     * @param user          an object with user data
     * @param search        a keyword typed by user to which the titles are compared
     * @param idCategory    an ID of the category
     * @return              a list of filtered events
     */
    @Query("""
            SELECT e FROM Event e
            WHERE e.user = :user
            AND (:search IS NULL OR :search = '' OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:idCategory IS NULL OR e.category.idCategory = :idCategory)
                ORDER BY e.dateStart DESC
            """)
    List<Event> filterEvents(@Param("user") UserEntity user, @Param("search") String search, @Param("idCategory") Long idCategory);

    /**
     * Finds an event that belongs to the specific user by its ID 
     * @param idEvent   an ID of the requested event
     * @param idUser    an ID of the user
     * @return          a Event object
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.category WHERE e.idEvent = :idEvent AND e.user.idUser = :idUser")
    Optional<Event> findByIdAndIdUser(@Param("idEvent") Long idEvent, @Param("idUser") Long idUser);

    /**
     * Finds events by a specified date. The events returned start before or at the date and end after or at the date.
     * @param date  a specific date
     * @param user  an object with user data
     * @return      a list of Event objects
     */
    @Query("SELECT e FROM Event e WHERE e.user = :user AND (e.dateEnd IS NULL OR :date BETWEEN e.dateStart AND e.dateEnd)")
    List<Event> findByDateAndUser(@Param("date") LocalDate date, @Param("user") UserEntity user);

    /**
     * Finds events that start at the current or future date and time
     * @param user  an object with user data
     * @param date  a specific date
     * @param time  a specific time
     * @return      a list of events in presence and future
     */
    @Query("""
        SELECT e FROM Event e
        WHERE e.user = :user
        AND (e.dateStart IS NULL OR e.dateStart >= :date)
        AND (e.dateStart IS NULL OR e.dateStart > :date OR e.timeStart IS NULL OR e.timeStart > :time)
            """)
    List<Event> findWithFutureOrPresentDate(@Param("user") UserEntity user, @Param("date") LocalDate date, @Param("time") LocalTime time);

    /**
     * Removes category relationship from all events assigned to the specific category
     * @param idCategory    an ID of the category that will be cleared
     * @param user          an object with user data
     */
    @Modifying
    @Query("UPDATE Event e SET e.category = null WHERE e.category.idCategory = :idCategory AND e.user = :user")
    void clearCategoryByCategoryAndUser(@Param("idCategory") Long idCategory, @Param("user") UserEntity user);

    /**
     * Deletes all user events
     * @param user  an object with user data
     * @return      a number of removed records from the table
     */
    @Modifying
    @Query("DELETE FROM Event e WHERE e.user = :user")
    int deleteByUser(@Param("user") UserEntity user);


}
