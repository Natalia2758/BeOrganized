package com.example.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.category.model.Category;
import com.example.task.enums.Priority;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import com.example.user.model.UserEntity;

@DataJpaTest 
public class TaskRepositoryTest {

    @Autowired 
    private TaskRepository taskRepo;

    @Autowired 
    private TestEntityManager entityManager;

    private UserEntity user1;
    private UserEntity user2;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        user1 = new UserEntity();
        entityManager.persist(user1);
        user2 = new UserEntity();
        entityManager.persist(user2);
        category1 = new Category();
        entityManager.persist(category1);
        category2 = new Category();
        entityManager.persist(category2);

        entityManager.flush();
    }

    @Test
    void shouldFindTasksWithSubtasksByStartDate() {
        LocalDate date = LocalDate.of(2026, 10, 20);

        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setStartDate(date);
        task1.setUser(user1);
        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setStartDate(date);
        task2.setUser(user1);
        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setStartDate(LocalDate.of(2026, 12, 23));
        task3.setUser(user1);
        Task task4 = new Task();
        task4.setTitle("Clean");
        task4.setStartDate(date);
        task4.setUser(user2);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.persist(task4);
        entityManager.flush();

        List<Task> result = taskRepo.findByStartDate(user1, date);

        assertTrue(
            result.size() == 2 
            && result.containsAll(List.of(task1, task2)) 
            && !result.contains(task3)
            && !result.contains(task4)
        );
    }

    @Test 
    void shouldFindFilteredTasksByFinishStatusSortedByPriority() {
        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);
        task1.setFinished(true);
        task1.setPriority(Priority.HIGH);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);
        task2.setFinished(true);
        task2.setPriority(Priority.LOW);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);
        task3.setFinished(false);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        List<Task> result = taskRepo.filterTasks(user1, true, null, null);

        assertTrue(
            result.size() == 2
            && result.containsAll(List.of(task1, task2))
            && result.get(0).equals(task1)
        );

    }

    @Test 
    void shouldFindFilteredTasksByCategorySortedByFinishStatusAndPriority() {
        // 2
        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);
        task1.setFinished(true);
        task1.setPriority(Priority.HIGH);
        task1.setCategory(category1);

        // 3
        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);
        task2.setFinished(false);
        task2.setPriority(Priority.LOW);
        task2.setCategory(category1);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);
        task3.setFinished(false);
        task3.setCategory(category2);

        // 1
        Task task4 = new Task();
        task4.setTitle("Clean");
        task4.setUser(user1);
        task4.setFinished(false);
        task4.setCategory(category1);
        task4.setPriority(Priority.LOW);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.persist(task4);
        entityManager.flush();

        List<Task> result = taskRepo.filterTasks(user1, null, category1.getIdCategory(), null);

        assertTrue(
            result.size() == 3 
            && result.containsAll(List.of(task1, task2, task4))
            && result.get(0).equals(task4)
            && result.get(1).equals(task1)
        );

    }

    @Test 
    void shouldFindFilteredTasksBySearch() {
        
        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        List<Task> result = taskRepo.filterTasks(user1, null, null, "exercise");

        assertTrue(
            result.size() == 1
            && result.get(0).equals(task3)
        );

    }

    @Test 
    void shouldFindFilteredTasksByAllFilters() {
        
        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);
        task1.setFinished(false);
        task1.setCategory(category2);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);
        task2.setFinished(true);
        task2.setCategory(category1);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);
        task3.setFinished(false);
        task3.setCategory(category1);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        List<Task> result = taskRepo.filterTasks(user1, true, category1.getIdCategory(), "o");

        assertTrue(
            result.size() == 1
            && result.get(0).equals(task2)
        );

    }

    @Test 
    void shouldReturnEmptyListIfNoUserTasks() {
        
        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        List<Task> result = taskRepo.filterTasks(user2, null, null, null);

        assertTrue(
            result.isEmpty()
        );
    }

    @Test 
    void shouldFindAllUserTasksSortedByFinishStatusAndPriority() {
        
        // 2
        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);
        task1.setFinished(true);
        task1.setPriority(Priority.HIGH);

        // 3
        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);
        task2.setFinished(true);
        task2.setPriority(Priority.LOW);

        // 1
        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);
        task3.setFinished(false);
        task2.setPriority(Priority.LOW);

        Task task4 = new Task();
        task4.setTitle("Exercise");
        task4.setUser(user2);
        task4.setFinished(false);
        task4.setCategory(category2);
        task4.setPriority(Priority.LOW);


        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.persist(task4);
        entityManager.flush();

        List<Task> result = taskRepo.filterTasks(user1, null, null, null);

        assertTrue(
            result.size() == 3
            && result.containsAll(List.of(task1, task2, task3))
            && result.get(0).equals(task3)
            && result.get(1).equals(task1)
        );
    }

    @Test 
    void shouldFindTaskByIdAndUserId() {
        Task task1 = new Task();
        task1.setCategory(category1);
        task1.setUser(user1);

        entityManager.persist(task1);
        entityManager.flush();

        Optional<Task> result = taskRepo.findByIdAndIdUser(task1.getIdTask(), user1.getIdUser());

        assertTrue(result.isPresent());
        assertTrue(result.get().equals(task1));
        assertTrue(result.get().getCategory().equals(category1));

    }

    @Test 
    void shouldNotFindTaskBelongingToAnotherUser() {
        Task task1 = new Task();
        task1.setCategory(category1);
        task1.setUser(user1);

        entityManager.persist(task1);
        entityManager.flush();

        Optional<Task> result = taskRepo.findByIdAndIdUser(task1.getIdTask(), user2.getIdUser());

        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldFindTaskByIdAndUserIdWithCategory() {
        Task task1 = new Task();
        task1.setCategory(category1);
        task1.setUser(user1);

        entityManager.persist(task1);
        entityManager.flush();

        Optional<Task> result = taskRepo.findByIdAndIdUserWithCategory(task1.getIdTask(), user1.getIdUser());

        assertTrue(result.isPresent());
        assertTrue(result.get().equals(task1));
        assertTrue(result.get().getCategory().equals(category1));

    }

    @Test
    void shouldFindFutureTasksByUserAndDate() {
        LocalDate now = LocalDate.now();
        LocalTime timeNow = LocalTime.now();

        LocalDate futureDate= LocalDate.now().plusDays(2);
        LocalDateTime futureDateTimeHours = LocalDateTime.now().plusHours(3);
        LocalDate pastDate = LocalDate.now().minusDays(2);
        LocalDateTime pastDateTimeHours = LocalDateTime.now().minusHours(3);

        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user2);
        task1.setStartDate(futureDate);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);
        task2.setStartDate(futureDate);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);
        task3.setStartDate(LocalDate.of(futureDateTimeHours.getYear(), futureDateTimeHours.getMonthValue(), futureDateTimeHours.getDayOfMonth()));
        task3.setTimeStart(LocalTime.of(futureDateTimeHours.getHour(), futureDateTimeHours.getMinute()));

        Task task4 = new Task();
        task4.setTitle("Clean");
        task4.setUser(user1);
        task4.setStartDate(pastDate);

        Task task5 = new Task();
        task5.setTitle("Iron");
        task5.setUser(user1);
        task5.setStartDate(LocalDate.of(pastDateTimeHours.getYear(), pastDateTimeHours.getMonthValue(), pastDateTimeHours.getDayOfMonth()));
        task5.setTimeStart(LocalTime.of(pastDateTimeHours.getHour(), pastDateTimeHours.getMinute()));

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.persist(task4);
        entityManager.persist(task5);
        entityManager.flush();

        List<Task> result = taskRepo.findWithFutureOrPresentDate(user1, now, timeNow);
        assertTrue(
            result.size() == 4
            && result.containsAll(List.of(task2, task3, task4, task5))
        );
    }


    @Test
    void shouldReturnTrueWhenOverlappingTimeBlocks() {

        LocalDate date = LocalDate.of(2026, 10, 20);

        Task task = new Task();
        task.setTitle("Existing task");
        task.setUser(user1);
        task.setStartDate(date);
        task.setTimeStart(LocalTime.of(10, 0));
        task.setTimeEnd(LocalTime.of(12, 0));

        entityManager.persist(task);
        entityManager.flush();

        boolean result = taskRepo.existsOverlappingTimeBlock(
            date,
            LocalTime.of(11, 0),
            LocalTime.of(13, 0)
        );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenTimeBlocksDoNotOverlap() {

        LocalDate date = LocalDate.of(2026, 10, 20);

        Task task = new Task();
        task.setTitle("Existing task");
        task.setUser(user1);
        task.setStartDate(date);
        task.setTimeStart(LocalTime.of(10, 0));
        task.setTimeEnd(LocalTime.of(12, 0));

        entityManager.persist(task);
        entityManager.flush();

        boolean result = taskRepo.existsOverlappingTimeBlock(
            date,
            LocalTime.of(12, 0),
            LocalTime.of(14, 0)
        );

        assertTrue(!result);
    }

    @Test
    void shouldReturnFalseWhenTaskIsOnAnotherDate() {

        LocalDate taskDate = LocalDate.of(2026, 10, 20);
        LocalDate requestedDate = LocalDate.of(2026, 10, 21);

        Task task = new Task();
        task.setTitle("Existing task");
        task.setUser(user1);
        task.setStartDate(taskDate);
        task.setTimeStart(LocalTime.of(10, 0));
        task.setTimeEnd(LocalTime.of(12, 0));

        entityManager.persist(task);
        entityManager.flush();

        boolean result = taskRepo.existsOverlappingTimeBlock(
            requestedDate,
            LocalTime.of(11, 0),
            LocalTime.of(13, 0)
        );

        assertTrue(!result);
    }

    @Test
    void shouldClearCategoryFromUserTasks() {

        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);
        task1.setCategory(category1);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);
        task2.setCategory(category1);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user1);
        task3.setCategory(category2);

        Task task4 = new Task();
        task4.setTitle("Clean");
        task4.setUser(user2);
        task4.setCategory(category1);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.persist(task4);
        entityManager.flush();

        taskRepo.clearCategoryByCategoryAndUser(category1.getIdCategory(), user1);

        entityManager.clear();

        Task result1 = entityManager.find(Task.class, task1.getIdTask());
        Task result2 = entityManager.find(Task.class, task2.getIdTask());
        Task result3 = entityManager.find(Task.class, task3.getIdTask());
        Task result4 = entityManager.find(Task.class, task4.getIdTask());

        assertTrue(
            result1.getCategory() == null
            && result2.getCategory() == null
            && result3.getCategory().equals(category2)
            && result4.getCategory().equals(category1)
        );
    }

    @Test
    void shouldDeleteAllTasksBelongingToUser() {

        Task task1 = new Task();
        task1.setTitle("Homework");
        task1.setUser(user1);

        Task task2 = new Task();
        task2.setTitle("Shopping");
        task2.setUser(user1);

        Task task3 = new Task();
        task3.setTitle("Exercise");
        task3.setUser(user2);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        int deleted = taskRepo.deleteByUser(user1);

        entityManager.clear();

        Task result1 = entityManager.find(Task.class, task1.getIdTask());
        Task result2 = entityManager.find(Task.class, task2.getIdTask());
        Task result3 = entityManager.find(Task.class, task3.getIdTask());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }

}
