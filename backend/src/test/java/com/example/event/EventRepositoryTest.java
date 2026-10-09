package com.example.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.category.model.Category;
import com.example.event.model.Event;
import com.example.event.repository.EventRepository;
import com.example.user.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepo;

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
    void shouldFindAllEventsBelongingToUser() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Exercise");
        event3.setUser(user2);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();

        List<Event> result = eventRepo.findByUser(user1);

        assertTrue(
            result.size() == 2
            && result.contains(event1)
            && result.contains(event2)
            && !result.contains(event3)
        );
    }

    @Test
    void shouldFindFilteredEventsBySearch() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Team Meeting");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Exercise");
        event3.setUser(user1);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, "meeting", null);

        assertTrue(
            result.size() == 1
            && result.get(0).equals(event1)
        );
    }

    @Test
    void shouldFindFilteredEventsByCategory() {

        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setCategory(category1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setCategory(category2);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Exercise");
        event3.setUser(user1);
        event3.setCategory(category1);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        Event event4 = new Event();
        event4.setTitle("Other user event");
        event4.setUser(user2);
        event4.setCategory(category1);
        event4.setDateStart(date);
        event4.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.persist(event4);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, null, category1.getIdCategory());

        assertTrue(
            result.size() == 2
            && result.contains(event1)
            && result.contains(event3)
            && !result.contains(event2)
            && !result.contains(event4)
        );
    }

    @Test
    void shouldFindFilteredEventsBySearchAndCategory() {

        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Team Meeting");
        event1.setUser(user1);
        event1.setCategory(category1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Team Meeting");
        event2.setUser(user1);
        event2.setCategory(category2);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Shopping");
        event3.setUser(user1);
        event3.setCategory(category1);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, "meeting", category1.getIdCategory());

        assertTrue(
            result.size() == 1
            && result.get(0).equals(event1)
        );
    }

    @Test
    void shouldFindFilteredEventsCaseInsensitively() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Team Meeting");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, "MEETING", null);

        assertTrue(
            result.size() == 1
            && result.get(0).equals(event1)
        );
    }

    @Test
    void shouldFindAllUserEventsWhenSearchAndCategoryAreNull() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Exercise");
        event3.setUser(user2);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, null, null);

        assertTrue(
            result.size() == 2
            && result.contains(event1)
            && result.contains(event2)
            && !result.contains(event3)
        );
    }

    @Test
    void shouldFindAllUserEventsWhenSearchIsEmpty() {

        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, "", null);

        assertTrue(
            result.size() == 2
            && result.contains(event1)
            && result.contains(event2)
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsMatchFilters() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setCategory(category1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, "Shopping", category1.getIdCategory());

        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnFilteredEventsSortedByStartDateDescending() {

        Event event1 = new Event();
        event1.setTitle("Older");
        event1.setUser(user1);
        event1.setDateStart(LocalDate.of(2026, 10, 1));
        event1.setDateEnd(LocalDate.of(2026, 10, 1));

        Event event2 = new Event();
        event2.setTitle("Newer");
        event2.setUser(user1);
        event2.setDateStart(LocalDate.of(2026, 10, 20));
        event2.setDateEnd(LocalDate.of(2026, 10, 20));

        Event event3 = new Event();
        event3.setTitle("Middle");
        event3.setUser(user1);
        event3.setDateStart(LocalDate.of(2026, 10, 10));
        event3.setDateEnd(LocalDate.of(2026, 10, 10));

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();

        List<Event> result = eventRepo.filterEvents(user1, null, null);

        assertTrue(
            result.size() == 3
            && result.get(0).equals(event2)
            && result.get(1).equals(event3)
            && result.get(2).equals(event1)
        );
    }


    @Test
    void shouldFindEventByIdAndUserId() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setCategory(category1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.flush();

        Optional<Event> result = eventRepo.findByIdAndIdUser(event1.getIdEvent(), user1.getIdUser());

        assertTrue(
            result.isPresent()
            && result.get().equals(event1)
            && result.get().getCategory().equals(category1)
        );
    }


    @Test
    void shouldNotFindEventBelongingToAnotherUser() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Private event");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.flush();

        Optional<Event> result = eventRepo.findByIdAndIdUser(event1.getIdEvent(), user2.getIdUser());

        assertTrue(result.isEmpty());
    }


    @Test
    void shouldFindEventsByDateAndUser() {

        LocalDate date = LocalDate.of(2026, 10, 20);

        Event event1 = new Event();
        event1.setTitle("Event on date");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Ongoing event");
        event2.setUser(user1);
        event2.setDateStart(date.minusDays(2));
        event2.setDateEnd(date.plusDays(2));

        Event event3 = new Event();
        event3.setTitle("Future event");
        event3.setUser(user1);
        event3.setDateStart(date.plusDays(5));
        event3.setDateEnd(date.plusDays(6));

        Event event4 = new Event();
        event4.setTitle("Other user event");
        event4.setUser(user2);
        event4.setDateStart(date.minusDays(1));
        event4.setDateEnd(date.plusDays(1));

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.persist(event4);
        entityManager.flush();

        List<Event> result = eventRepo.findByDateAndUser(date, user1);

        assertTrue(
            result.size() == 2
            && result.contains(event1)
            && result.contains(event2)
            && !result.contains(event3)
            && !result.contains(event4)
        );
    }


    @Test
    void shouldFindFutureOrPresentEventsByUserAndDate() {

        LocalDate date = LocalDate.of(2026, 10, 20);
        LocalTime time = LocalTime.of(14, 0);

        Event event1 = new Event();
        event1.setTitle("Future date");
        event1.setUser(user1);
        event1.setDateStart(date.plusDays(1));
        event1.setDateEnd(date.plusDays(1));
        event1.setTimeStart(LocalTime.of(10, 0));

        Event event2 = new Event();
        event2.setTitle("Later today");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);
        event2.setTimeStart(LocalTime.of(16, 0));

        Event event3 = new Event();
        event3.setTitle("Earlier today");
        event3.setUser(user1);
        event3.setDateStart(date);
        event3.setDateEnd(date);
        event3.setTimeStart(LocalTime.of(10, 0));

        Event event4 = new Event();
        event4.setTitle("Past date");
        event4.setUser(user1);
        event4.setDateStart(date.minusDays(1));
        event4.setDateEnd(date.minusDays(1));
        event4.setTimeStart(LocalTime.of(10, 0));

        Event event5 = new Event();
        event5.setTitle("Other user future event");
        event5.setUser(user2);
        event5.setDateStart(date.plusDays(1));
        event5.setDateEnd(date.plusDays(1));
        event5.setTimeStart(LocalTime.of(10, 0));

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.persist(event4);
        entityManager.persist(event5);
        entityManager.flush();

        List<Event> result = eventRepo.findWithFutureOrPresentDate(user1, date, time);

        assertTrue(
            result.size() == 2
            && result.contains(event1)
            && result.contains(event2)
            && !result.contains(event3)
            && !result.contains(event4)
            && !result.contains(event5)
        );
    }


    @Test
    void shouldFindEventsWithNullStartDate() {

        LocalDate date = LocalDate.of(2026, 10, 20);
        LocalTime time = LocalTime.of(14, 0);

        Event event1 = new Event();
        event1.setTitle("Event without date");
        event1.setUser(user1);
        event1.setDateStart(null);
        event1.setDateEnd(null);
        event1.setTimeStart(null);

        Event event2 = new Event();
        event2.setTitle("Past event");
        event2.setUser(user1);
        event2.setDateStart(date.minusDays(1));
        event2.setDateEnd(date.minusDays(1));
        event2.setTimeStart(LocalTime.of(10, 0));

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();

        List<Event> result = eventRepo.findWithFutureOrPresentDate(user1, date, time);

        assertTrue(
            result.size() == 1
            && result.get(0).equals(event1)
        );
    }

    @Test
    void shouldClearCategoryFromUserEvents() {

        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setCategory(category1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setCategory(category1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Exercise");
        event3.setUser(user1);
        event3.setCategory(category2);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        Event event4 = new Event();
        event4.setTitle("Other user event");
        event4.setUser(user2);
        event4.setCategory(category1);
        event4.setDateStart(date);
        event4.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.persist(event4);
        entityManager.flush();

        eventRepo.clearCategoryByCategoryAndUser(category1.getIdCategory(), user1);

        entityManager.clear();

        Event result1 = entityManager.find(Event.class, event1.getIdEvent());
        Event result2 = entityManager.find(Event.class, event2.getIdEvent());
        Event result3 = entityManager.find(Event.class, event3.getIdEvent());
        Event result4 = entityManager.find(Event.class, event4.getIdEvent());

        assertTrue(
            result1.getCategory() == null
            && result2.getCategory() == null
            && result3.getCategory().equals(category2)
            && result4.getCategory().equals(category1)
        );
    }

    @Test
    void shouldDeleteAllEventsBelongingToUser() {
        LocalDate date = LocalDate.now().minusDays(1);

        Event event1 = new Event();
        event1.setTitle("Meeting");
        event1.setUser(user1);
        event1.setDateStart(date);
        event1.setDateEnd(date);

        Event event2 = new Event();
        event2.setTitle("Shopping");
        event2.setUser(user1);
        event2.setDateStart(date);
        event2.setDateEnd(date);

        Event event3 = new Event();
        event3.setTitle("Exercise");
        event3.setUser(user2);
        event3.setDateStart(date);
        event3.setDateEnd(date);

        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();

        int deleted = eventRepo.deleteByUser(user1);

        entityManager.clear();

        Event result1 = entityManager.find(Event.class, event1.getIdEvent());
        Event result2 = entityManager.find(Event.class, event2.getIdEvent());
        Event result3 = entityManager.find(Event.class, event3.getIdEvent());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }
}
