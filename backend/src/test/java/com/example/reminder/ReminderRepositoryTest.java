package com.example.reminder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.reminder.enums.ReminderStatus;
import com.example.reminder.enums.ReminderType;
import com.example.reminder.enums.Timing;
import com.example.reminder.model.Reminder;
import com.example.reminder.repository.ReminderRepository;
import com.example.user.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ReminderRepositoryTest {

    @Autowired
    private ReminderRepository reminderRepo;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {

        user1 = new UserEntity();
        entityManager.persist(user1);

        user2 = new UserEntity();
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void shouldFindAllRemindersBelongingToUser() {

        Reminder reminder1 = new Reminder();
        reminder1.setContent("Meeting reminder");
        reminder1.setType(ReminderType.NONE);
        reminder1.setRemindAt(Timing.SET_MANUALLY);
        reminder1.setRemindDate(LocalDate.of(2026, 10, 20));
        reminder1.setRemindTime(LocalTime.of(10, 0));
        reminder1.setStatus(ReminderStatus.PENDING);
        reminder1.setUser(user1);

        Reminder reminder2 = new Reminder();
        reminder2.setContent("Task reminder");
        reminder2.setType(ReminderType.NONE);
        reminder2.setRemindAt(Timing.SET_MANUALLY);
        reminder2.setRemindDate(LocalDate.of(2026, 10, 21));
        reminder2.setRemindTime(LocalTime.of(12, 0));
        reminder2.setStatus(ReminderStatus.TRIGGERED);
        reminder2.setUser(user1);

        Reminder reminder3 = new Reminder();
        reminder3.setContent("Other user reminder");
        reminder3.setType(ReminderType.NONE);
        reminder3.setRemindAt(Timing.SET_MANUALLY);
        reminder3.setRemindDate(LocalDate.of(2026, 10, 22));
        reminder3.setRemindTime(LocalTime.of(14, 0));
        reminder3.setStatus(ReminderStatus.PENDING);
        reminder3.setUser(user2);

        entityManager.persist(reminder1);
        entityManager.persist(reminder2);
        entityManager.persist(reminder3);
        entityManager.flush();

        List<Reminder> result = reminderRepo.findByUser(user1);

        assertTrue(
            result.size() == 2
            && result.contains(reminder1)
            && result.contains(reminder2)
            && !result.contains(reminder3)
        );
    }

    @Test
    void shouldFindReminderByIdAndUserId() {

        Reminder reminder = new Reminder();
        reminder.setContent("Meeting reminder");
        reminder.setType(ReminderType.NONE);
        reminder.setRemindAt(Timing.SET_MANUALLY);
        reminder.setRemindDate(LocalDate.of(2026, 10, 20));
        reminder.setRemindTime(LocalTime.of(10, 0));
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setUser(user1);

        entityManager.persist(reminder);
        entityManager.flush();

        Optional<Reminder> result = reminderRepo.findByIdAndIdUser(reminder.getIdReminder(), user1.getIdUser());

        assertTrue(
            result.isPresent()
            && result.get().equals(reminder)
        );
    }

    @Test
    void shouldNotFindReminderBelongingToAnotherUser() {

        Reminder reminder = new Reminder();
        reminder.setContent("Private reminder");
        reminder.setType(ReminderType.NONE);
        reminder.setRemindAt(Timing.SET_MANUALLY);
        reminder.setRemindDate(LocalDate.of(2026, 10, 20));
        reminder.setRemindTime(LocalTime.of(10, 0));
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setUser(user1);

        entityManager.persist(reminder);
        entityManager.flush();

        Optional<Reminder> result = reminderRepo.findByIdAndIdUser(reminder.getIdReminder(), user2.getIdUser());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindRemindersByDateAndUser() {

        LocalDate date = LocalDate.of(2026, 10, 20);

        Reminder reminder1 = new Reminder();
        reminder1.setContent("Reminder on date");
        reminder1.setType(ReminderType.NONE);
        reminder1.setRemindAt(Timing.SET_MANUALLY);
        reminder1.setRemindDate(date);
        reminder1.setRemindTime(LocalTime.of(10, 0));
        reminder1.setStatus(ReminderStatus.PENDING);
        reminder1.setUser(user1);

        Reminder reminder2 = new Reminder();
        reminder2.setContent("Second reminder on date");
        reminder2.setType(ReminderType.NONE);
        reminder2.setRemindAt(Timing.SET_MANUALLY);
        reminder2.setRemindDate(date);
        reminder2.setRemindTime(LocalTime.of(14, 0));
        reminder2.setStatus(ReminderStatus.TRIGGERED);
        reminder2.setUser(user1);

        Reminder reminder3 = new Reminder();
        reminder3.setContent("Reminder on another date");
        reminder3.setType(ReminderType.NONE);
        reminder3.setRemindAt(Timing.SET_MANUALLY);
        reminder3.setRemindDate(date.plusDays(1));
        reminder3.setRemindTime(LocalTime.of(10, 0));
        reminder3.setStatus(ReminderStatus.PENDING);
        reminder3.setUser(user1);

        Reminder reminder4 = new Reminder();
        reminder4.setContent("Other user reminder");
        reminder4.setType(ReminderType.NONE);
        reminder4.setRemindAt(Timing.SET_MANUALLY);
        reminder4.setRemindDate(date);
        reminder4.setRemindTime(LocalTime.of(12, 0));
        reminder4.setStatus(ReminderStatus.PENDING);
        reminder4.setUser(user2);

        entityManager.persist(reminder1);
        entityManager.persist(reminder2);
        entityManager.persist(reminder3);
        entityManager.persist(reminder4);
        entityManager.flush();

        List<Reminder> result = reminderRepo.findByDateAndUser(date, user1);

        assertTrue(
            result.size() == 2
            && result.contains(reminder1)
            && result.contains(reminder2)
            && !result.contains(reminder3)
            && !result.contains(reminder4)
        );
    }

    @Test
    void shouldFindPendingAndTriggeredRemindersByDateAndTime() {

        LocalDate date = LocalDate.of(2026, 10, 20);
        LocalTime time = LocalTime.of(14, 0);

        Reminder reminder1 = new Reminder();
        reminder1.setContent("Pending before time");
        reminder1.setType(ReminderType.NONE);
        reminder1.setRemindAt(Timing.SET_MANUALLY);
        reminder1.setRemindDate(date);
        reminder1.setRemindTime(LocalTime.of(10, 0));
        reminder1.setStatus(ReminderStatus.PENDING);
        reminder1.setUser(user1);

        Reminder reminder2 = new Reminder();
        reminder2.setContent("Triggered before time");
        reminder2.setType(ReminderType.NONE);
        reminder2.setRemindAt(Timing.SET_MANUALLY);
        reminder2.setRemindDate(date);
        reminder2.setRemindTime(LocalTime.of(12, 0));
        reminder2.setStatus(ReminderStatus.TRIGGERED);
        reminder2.setUser(user1);

        Reminder reminder3 = new Reminder();
        reminder3.setContent("Pending exactly at time");
        reminder3.setType(ReminderType.NONE);
        reminder3.setRemindAt(Timing.SET_MANUALLY);
        reminder3.setRemindDate(date);
        reminder3.setRemindTime(time);
        reminder3.setStatus(ReminderStatus.PENDING);
        reminder3.setUser(user1);

        Reminder reminder4 = new Reminder();
        reminder4.setContent("Pending after time");
        reminder4.setType(ReminderType.NONE);
        reminder4.setRemindAt(Timing.SET_MANUALLY);
        reminder4.setRemindDate(date);
        reminder4.setRemindTime(LocalTime.of(16, 0));
        reminder4.setStatus(ReminderStatus.PENDING);
        reminder4.setUser(user1);

        Reminder reminder5 = new Reminder();
        reminder5.setContent("Cancelled reminder");
        reminder5.setType(ReminderType.NONE);
        reminder5.setRemindAt(Timing.SET_MANUALLY);
        reminder5.setRemindDate(date);
        reminder5.setRemindTime(LocalTime.of(10, 0));
        reminder5.setStatus(ReminderStatus.CANCELLED);
        reminder5.setUser(user1);

        Reminder reminder6 = new Reminder();
        reminder6.setContent("Pending other date");
        reminder6.setType(ReminderType.NONE);
        reminder6.setRemindAt(Timing.SET_MANUALLY);
        reminder6.setRemindDate(date.plusDays(1));
        reminder6.setRemindTime(LocalTime.of(10, 0));
        reminder6.setStatus(ReminderStatus.PENDING);
        reminder6.setUser(user1);

        entityManager.persist(reminder1);
        entityManager.persist(reminder2);
        entityManager.persist(reminder3);
        entityManager.persist(reminder4);
        entityManager.persist(reminder5);
        entityManager.persist(reminder6);
        entityManager.flush();

        List<Reminder> result = reminderRepo.findByStatusAndRemindDateTime(
                    ReminderStatus.PENDING,
                    ReminderStatus.TRIGGERED,
                    date,
                    time
                );

        assertTrue(
            result.size() == 3
            && result.contains(reminder1)
            && result.contains(reminder2)
            && result.contains(reminder3)
            && !result.contains(reminder4)
            && !result.contains(reminder5)
            && !result.contains(reminder6)
        );
    }

    @Test
    void shouldNotFindRemindersWithWrongStatus() {

        LocalDate date = LocalDate.of(2026, 10, 20);
        LocalTime time = LocalTime.of(14, 0);

        Reminder reminder = new Reminder();
        reminder.setContent("Cancelled reminder");
        reminder.setType(ReminderType.NONE);
        reminder.setRemindAt(Timing.SET_MANUALLY);
        reminder.setRemindDate(date);
        reminder.setRemindTime(LocalTime.of(10, 0));
        reminder.setStatus(ReminderStatus.CANCELLED);
        reminder.setUser(user1);

        entityManager.persist(reminder);
        entityManager.flush();

        List<Reminder> result = reminderRepo.findByStatusAndRemindDateTime(
                    ReminderStatus.PENDING,
                    ReminderStatus.TRIGGERED,
                    date,
                    time
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindRemindersByUserAndStatus() {

        Reminder reminder1 = new Reminder();
        reminder1.setContent("Pending reminder 1");
        reminder1.setType(ReminderType.NONE);
        reminder1.setRemindAt(Timing.SET_MANUALLY);
        reminder1.setRemindDate(LocalDate.of(2026, 10, 20));
        reminder1.setRemindTime(LocalTime.of(10, 0));
        reminder1.setStatus(ReminderStatus.PENDING);
        reminder1.setUser(user1);

        Reminder reminder2 = new Reminder();
        reminder2.setContent("Pending reminder 2");
        reminder2.setType(ReminderType.NONE);
        reminder2.setRemindAt(Timing.SET_MANUALLY);
        reminder2.setRemindDate(LocalDate.of(2026, 10, 21));
        reminder2.setRemindTime(LocalTime.of(12, 0));
        reminder2.setStatus(ReminderStatus.PENDING);
        reminder2.setUser(user1);

        Reminder reminder3 = new Reminder();
        reminder3.setContent("Triggered reminder");
        reminder3.setType(ReminderType.NONE);
        reminder3.setRemindAt(Timing.SET_MANUALLY);
        reminder3.setRemindDate(LocalDate.of(2026, 10, 22));
        reminder3.setRemindTime(LocalTime.of(14, 0));
        reminder3.setStatus(ReminderStatus.TRIGGERED);
        reminder3.setUser(user1);

        Reminder reminder4 = new Reminder();
        reminder4.setContent("Other user pending reminder");
        reminder4.setType(ReminderType.NONE);
        reminder4.setRemindAt(Timing.SET_MANUALLY);
        reminder4.setRemindDate(LocalDate.of(2026, 10, 23));
        reminder4.setRemindTime(LocalTime.of(16, 0));
        reminder4.setStatus(ReminderStatus.PENDING);
        reminder4.setUser(user2);

        entityManager.persist(reminder1);
        entityManager.persist(reminder2);
        entityManager.persist(reminder3);
        entityManager.persist(reminder4);
        entityManager.flush();

        List<Reminder> result = reminderRepo.findByUserAndStatus(user1, ReminderStatus.PENDING);

        assertTrue(
            result.size() == 2
            && result.contains(reminder1)
            && result.contains(reminder2)
            && !result.contains(reminder3)
            && !result.contains(reminder4)
        );
    }

    @Test
    void shouldDeleteAllRemindersBelongingToUser() {

        Reminder reminder1 = new Reminder();
        reminder1.setContent("Meeting reminder");
        reminder1.setType(ReminderType.NONE);
        reminder1.setRemindAt(Timing.SET_MANUALLY);
        reminder1.setRemindDate(LocalDate.of(2026, 10, 20));
        reminder1.setRemindTime(LocalTime.of(10, 0));
        reminder1.setStatus(ReminderStatus.PENDING);
        reminder1.setUser(user1);

        Reminder reminder2 = new Reminder();
        reminder2.setContent("Task reminder");
        reminder2.setType(ReminderType.NONE);
        reminder2.setRemindAt(Timing.SET_MANUALLY);
        reminder2.setRemindDate(LocalDate.of(2026, 10, 21));
        reminder2.setRemindTime(LocalTime.of(12, 0));
        reminder2.setStatus(ReminderStatus.TRIGGERED);
        reminder2.setUser(user1);

        Reminder reminder3 = new Reminder();
        reminder3.setContent("Other user reminder");
        reminder3.setType(ReminderType.NONE);
        reminder3.setRemindAt(Timing.SET_MANUALLY);
        reminder3.setRemindDate(LocalDate.of(2026, 10, 22));
        reminder3.setRemindTime(LocalTime.of(14, 0));
        reminder3.setStatus(ReminderStatus.PENDING);
        reminder3.setUser(user2);

        entityManager.persist(reminder1);
        entityManager.persist(reminder2);
        entityManager.persist(reminder3);
        entityManager.flush();

        int deleted = reminderRepo.deleteByUser(user1);

        entityManager.clear();

        Reminder result1 = entityManager.find(Reminder.class, reminder1.getIdReminder());
        Reminder result2 = entityManager.find(Reminder.class, reminder2.getIdReminder());
        Reminder result3 = entityManager.find(Reminder.class, reminder3.getIdReminder());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }
}
