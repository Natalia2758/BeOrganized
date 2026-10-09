package com.example.user.repository;

import com.example.user.model.UserEntity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 
 * <b>UserRepository</b><br>
 * 
 * Repository interface for UserEntity objects. Used to perform CRUD operations on user data within the database.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Retrieves user object by the email
     * @param email an email of the user
     * @return      a UserEntity object with user data
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks if the user with the provided email address already exists in the database
     * @param email an email to be checked
     * @return      true if the user with the particular email address exists, false if not
     */
    boolean existsByEmail(String email);
}
