package com.example.reminder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reminder.enums.ReminderStatus;
import com.example.reminder.model.Reminder;
import com.example.user.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 
 * <b>RemindertRepository</b>
 * <br>
 * Repository interface for Reminder entities. Used to perform CRUD operations on reminders data within the database.
 * 
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Retrieves all reminders that belong to the specific user
     * @param user  an object with user data
     * @return      a list of Reminder objects
     */
    @Query("SELECT r FROM Reminder r WHERE r.user = :user")
    List<Reminder> findByUser(@Param("user") UserEntity user);

    /**
     * Deletes all user reminders
     * @param user  an object with user data
     * @return      a number of removed records from the table
     */
    @Modifying
    @Query("DELETE FROM Reminder r WHERE r.user = :user")
    int deleteByUser(@Param("user") UserEntity user);

    /**
     * Finds a reminder that belongs to the specific user by its ID 
     * @param idReminder    an ID of the requested reminder
     * @param idUser        an ID of the user
     * @return              a Reminder object
     */
    @Query("SELECT r FROM Reminder r WHERE r.idReminder = :idReminder AND r.user.idUser = :idUser")
    Optional<Reminder> findByIdAndIdUser(@Param("idReminder") Long idReminder, @Param("idUser") Long idUser);

    /**
     * Finds reminders by a specified date
     * @param date  a specific date
     * @param user  an object with user data
     * @return      a list of Reminder objects
     */
    @Query("SELECT r FROM Reminder r WHERE r.user = :user AND r.remindDate = :remindDate")
    List<Reminder> findByDateAndUser(@Param("remindDate") LocalDate remindDate, @Param("user") UserEntity user);

    /**
     * Finds all reminders with pending or triggered status and a specific date and time
     * @param statusPending     a pending status
     * @param statusTriggered   a triggered status
     * @param date              a specific date
     * @param time              a specific time
     * @return                  a list of reminders with pending or triggered status by specific date and time
     */
    @Query("SELECT r FROM Reminder r WHERE (r.status = :statusPending OR r.status = :statusTriggered) AND remindDate = :date AND remindTime <= :time")
    List<Reminder> findByStatusAndRemindDateTime(@Param("statusPending") ReminderStatus statusPending, @Param("statusTriggered") ReminderStatus statusTriggered, @Param("date") LocalDate date, @Param("time") LocalTime time);

    /**
     * Finda all reminders that belong to a specific user and have a particular status
     * @param user      an object with user data
     * @param status    a reminder status
     * @return          a list of Reminder objects by user and reminder status
     */
    @Query("SELECT r FROM Reminder r WHERE r.user = :user AND r.status = :status")
    List<Reminder> findByUserAndStatus(@Param("user") UserEntity user, @Param("status") ReminderStatus status);
}
