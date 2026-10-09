package com.example.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.task.model.Subtask;
import com.example.task.model.Task;
import com.example.task.repository.SubtaskRepository;
import com.example.user.model.UserEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class SubtaskRepositoryTest {

    @Autowired
    private SubtaskRepository subtaskRepo;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user1;
    private UserEntity user2;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {

        user1 = new UserEntity(
                "user1@email.com",
                "user1",
                "password"
        );

        user2 = new UserEntity(
                "user2@email.com",
                "user2",
                "password"
        );

        entityManager.persist(user1);
        entityManager.persist(user2);

        task1 = new Task();
        task1.setTitle("Task 1");
        task1.setUser(user1);

        task2 = new Task();
        task2.setTitle("Task 2");
        task2.setUser(user1);

        task3 = new Task();
        task3.setTitle("Task 3");
        task3.setUser(user2);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);

        entityManager.flush();
    }

    @Test
    void shouldFindAllSubtasksAssignedToTask() {

        Subtask subtask1 = new Subtask("Subtask 1", false, task1);
        Subtask subtask2 = new Subtask("Subtask 2", true, task1);
        Subtask subtask3 = new Subtask("Subtask 3", false, task2);

        entityManager.persist(subtask1);
        entityManager.persist(subtask2);
        entityManager.persist(subtask3);
        entityManager.flush();

        List<Subtask> result = subtaskRepo.findByTask(task1);

        assertTrue(
            result.size() == 2
            && result.contains(subtask1)
            && result.contains(subtask2)
            && !result.contains(subtask3)
        );
    }

    @Test
    void shouldReturnEmptyListWhenTaskHasNoSubtasks() {

        Subtask subtask1 = new Subtask("Subtask 1", false, task1);

        entityManager.persist(subtask1);
        entityManager.flush();

        List<Subtask> result = subtaskRepo.findByTask(task2);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasksAssignedToTask() {

        Subtask subtask1 = new Subtask("Subtask 1", false, task1);
        Subtask subtask2 = new Subtask("Subtask 2", true, task1);
        Subtask subtask3 = new Subtask("Subtask 3", false, task2);

        entityManager.persist(subtask1);
        entityManager.persist(subtask2);
        entityManager.persist(subtask3);
        entityManager.flush();

        int deleted = subtaskRepo.deleteByTask(task1);

        entityManager.clear();

        Subtask result1 = entityManager.find(Subtask.class, subtask1.getIdSubtask());
        Subtask result2 = entityManager.find(Subtask.class, subtask2.getIdSubtask());
        Subtask result3 = entityManager.find(Subtask.class, subtask3.getIdSubtask());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }

    @Test
    void shouldDeleteNoSubtasksWhenTaskHasNone() {

        Subtask subtask1 = new Subtask("Subtask 1", false, task1);

        entityManager.persist(subtask1);
        entityManager.flush();

        int deleted = subtaskRepo.deleteByTask(task2);

        entityManager.clear();

        Subtask result = entityManager.find(Subtask.class, subtask1.getIdSubtask());

        assertTrue(
            deleted == 0
            && result != null
        );
    }

    @Test
    void shouldDeleteAllSubtasksBelongingToUser() {

        Subtask subtask1 = new Subtask("User 1 Subtask 1", false, task1);
        Subtask subtask2 = new Subtask("User 1 Subtask 2", true, task2);
        Subtask subtask3 = new Subtask("User 2 Subtask", false, task3);

        entityManager.persist(subtask1);
        entityManager.persist(subtask2);
        entityManager.persist(subtask3);
        entityManager.flush();

        int deleted = subtaskRepo.deleteByUser(user1);

        entityManager.clear();

        Subtask result1 = entityManager.find(Subtask.class, subtask1.getIdSubtask());
        Subtask result2 = entityManager.find(Subtask.class, subtask2.getIdSubtask());
        Subtask result3 = entityManager.find(Subtask.class, subtask3.getIdSubtask());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }

    @Test
    void shouldDeleteNoSubtasksWhenUserHasNone() {

        Subtask subtask1 = new Subtask("User 1 Subtask", false, task1);

        entityManager.persist(subtask1);
        entityManager.flush();

        int deleted = subtaskRepo.deleteByUser(user2);

        entityManager.clear();

        Subtask result = entityManager.find(Subtask.class, subtask1.getIdSubtask());

        assertTrue(
            deleted == 0
            && result != null
        );
    }
}

