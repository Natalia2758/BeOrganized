package com.example.note.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.note.model.Note;
import com.example.user.model.UserEntity;

/**
 * 
 * <b>NoteRepository</b>
 * <br>
 * Repository interface for Note entities. Used to perform CRUD operations on notes data within the database.
 * 
 */
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Retrieves all notes that belong to the specific user
     * @param user  an object with user data
     * @return      a list of Note objects
     */
    @Query("SELECT n FROM Note n WHERE n.user = :user ")
    List<Note> findByUser(@Param("user") UserEntity user);

    /**
     * Finds a note that belongs to the specific user by its ID 
     * @param idNote    an ID of the requested note
     * @param idUser    an ID of the user
     * @return          a Note object
     */
    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.category WHERE n.idNote = :idNote AND n.user.idUser = :idUser")
    Optional<Note> findByIdAndIdUser(@Param("idNote") Long idNote, @Param("idUser") Long idUser);

    /**
     * Finds notes by a specified date.
     * @param date  a specific date
     * @param user  an object with user data
     * @return      a list of Note objects
     */
    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.category WHERE n.date = :date AND n.user = :user")
    List<Note> findByDateAndUser(@Param("date") LocalDate date, @Param("user") UserEntity user);

    /**
     * Retrieves a list of filtered notes according to criteria provided in parameters
     * @param user          an object with user data
     * @param idCategory    an ID of the category
     * @return              a list of filtered notes
     */
    @Query("""
            SELECT n FROM Note n
            WHERE n.user = :user
            AND (:idCategory IS NULL OR n.category.idCategory = :idCategory)
            ORDER BY n.date DESC
            """)
    List<Note> filterNotes(@Param("user") UserEntity user, @Param("idCategory") Long idCategory);

    /**
     * Deletes all user notes
     * @param user  an object with user data
     * @return      a number of removed records from the table
     */
    @Modifying
    @Query("DELETE FROM Note n WHERE n.user = :user")
    int deleteByUser(@Param("user") UserEntity user);

    /**
     * Removes category relationship from all notes assigned to the specific category
     * @param idCategory    an ID of the category that will be cleared
     * @param user          an object with user data
     */
    @Modifying
    @Query("UPDATE Note n SET n.category = null WHERE n.category.idCategory = :idCategory AND n.user = :user")
    void clearCategoryByCategoryAndUser(@Param("idCategory") Long idCategory, @Param("user") UserEntity user);

}
