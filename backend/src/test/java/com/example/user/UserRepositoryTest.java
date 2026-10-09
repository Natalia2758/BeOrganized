package com.example.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindUserByEmail() {

        UserEntity user1 = new UserEntity(
                    "user1@email.com",
                    "user1",
                    "password"
                );

        entityManager.persist(user1);
        entityManager.flush();

        Optional<UserEntity> result = userRepo.findByEmail("user1@email.com");

        assertTrue(
            result.isPresent()
            && result.get().equals(user1)
        );
    }

    @Test
    void shouldNotFindUserWhenEmailDoesNotExist() {

        UserEntity user1 = new UserEntity(
                    "user1@email.com",
                    "user1",
                    "password"
                );

        entityManager.persist(user1);
        entityManager.flush();

        Optional<UserEntity> result = userRepo.findByEmail("unknown@email.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenUserWithEmailExists() {

        UserEntity user1 = new UserEntity(
                    "user1@email.com",
                    "user1",
                    "password"
                );

        entityManager.persist(user1);
        entityManager.flush();

        boolean result = userRepo.existsByEmail("user1@email.com");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUserWithEmailDoesNotExist() {

        UserEntity user1 = new UserEntity(
                    "user1@email.com",
                    "user1",
                    "password"
                );

        entityManager.persist(user1);
        entityManager.flush();

        boolean result = userRepo.existsByEmail("unknown@email.com");

        assertTrue(!result);
    }
}

