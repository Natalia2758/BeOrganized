package com.example.category.repository;

import com.example.category.model.Category;
import com.example.user.model.UserEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 
 * <b>CategoryRepository</b>
 * <br>
 * Repository interface for Category entities. Used to perform CRUD operations on categories data within the database.
 * 
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Retrieves all categories that belong to the specific user
     * @param user  an object with user data
     * @return      a list of Category objects
     */
    @Query("SELECT c FROM Category c WHERE c.user = :user")
    List<Category> findByUser(@Param("user") UserEntity user);

    /**
     * Finds a category that belongs to the specific user by its ID 
     * @param idCategory    an ID of the requested category
     * @param idUser        an ID of the user
     * @return              a Category object
     */
    @Query("SELECT c FROM Category c WHERE c.idCategory = :idCategory AND c.user.idUser = :idUser")
    Optional<Category> findByIdAndIdUser(@Param("idCategory") Long idCategory, @Param("idUser") Long idUser);

    /**
     * Verifies if a category with the given name already exists
     * @param name  a name to be checked
     * @return      true if it exists, false if it does not exist
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name AND c.user = :user")
    Boolean existsByName(@Param("name") String name, @Param("user") UserEntity user);

    /**
     * Removes all user categories from the database
     * @param user  a user object
     * @return      a number of deleted records
     */
    @Modifying 
    @Query("DELETE FROM Category c WHERE c.user = :user")
    int deleteByUser(@Param("user") UserEntity user);
}
