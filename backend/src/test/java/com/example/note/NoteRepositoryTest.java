package com.example.note;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.category.model.Category;
import com.example.note.model.Note;
import com.example.note.repository.NoteRepository;
import com.example.user.model.UserEntity;

@DataJpaTest 
public class NoteRepositoryTest {

    @Autowired 
    private NoteRepository noteRepo;

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
    void shouldFindNoteListByUser() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user1);

        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user2);

        Note note3 = new Note();
        note3.setContent("List of ideas: ...");
        note3.setUser(user1);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.flush();

        List<Note> result = noteRepo.findByUser(user1);
        
        assertTrue(
            result.size() == 2
            && result.containsAll(List.of(note1, note3))
        );
    }

    @Test 
    void shouldFindNoteByIdAndUserId() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user1);


        entityManager.persist(note1);
        entityManager.flush();

        Optional<Note> result = noteRepo.findByIdAndIdUser(note1.getIdNote(), user1.getIdUser());
        
        assertTrue(
            result.isPresent()
            && result.get().equals(note1)
        );
    }

    @Test 
    void shouldNotFindNoteBelongingToAnotherUser() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user1);

        entityManager.persist(note1);
        entityManager.flush();

        Optional<Note> result = noteRepo.findByIdAndIdUser(note1.getIdNote(), user2.getIdUser());
        
        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldFindNotesByDateAndUser() {
        LocalDate date = LocalDate.of(2026, 10, 20);

        Note note1 = new Note();
        note1.setContent("Note on date");
        note1.setUser(user1);
        note1.setDate(date);

        Note note2 = new Note();
        note2.setContent("Future note");
        note2.setUser(user1);
        note2.setDate(date.plusDays(2));

        Note note3 = new Note();
        note3.setContent("Past note");
        note3.setUser(user1);
        note3.setDate(date.minusDays(3));

        Note note4 = new Note();
        note4.setContent("Other user note");
        note4.setUser(user2);
        note4.setDate(date);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.persist(note4);
        entityManager.flush();

        List<Note> result = noteRepo.findByDateAndUser(date, user1);

        assertTrue(
            result.size() == 1
            && result.contains(note1)
        );
    }

    @Test 
    void shouldFindFilteredNotesByCategory() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user2);
        note1.setCategory(category1);

        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user1);
        note2.setCategory(category1);

        Note note3 = new Note();
        note3.setContent("List of ideas: ...");
        note3.setUser(user1);
        note3.setCategory(category2);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.flush();

        List<Note> result = noteRepo.filterNotes(user1, category1.getIdCategory());
        
        assertTrue(
            result.size() == 1
            && result.contains(note2)
        );
    }

    @Test 
    void shouldFindFilteredNotesByCategoryOrderedByDateDesc() {
        LocalDate date = LocalDate.of(2026, 8, 10);

        // 3
        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user1);
        note1.setCategory(category1);
        note1.setDate(date);

        // 1
        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user1);
        note2.setCategory(category1);
        note2.setDate(date.plusDays(3));

        // 2
        Note note3 = new Note();
        note3.setContent("List of ideas: ...");
        note3.setUser(user1);
        note3.setCategory(category1);
        note3.setDate(date.plusDays(1));

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.flush();

        List<Note> result = noteRepo.filterNotes(user1, category1.getIdCategory());
        
        assertTrue(
            result.size() == 3
            && result.containsAll(List.of(note1, note2, note3))
            && result.get(0).equals(note2)
            && result.get(1).equals(note3)
        );
    }

    @Test 
    void shouldFindAllUserNotesWhenCategoryIsNull() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user2);
        note1.setCategory(category1);

        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user1);
        note2.setCategory(category1);

        Note note3 = new Note();
        note3.setContent("List of ideas: ...");
        note3.setUser(user1);
        note3.setCategory(category2);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.flush();

        List<Note> result = noteRepo.filterNotes(user1, null);
        
        assertTrue(
            result.size() == 2
            && result.containsAll(List.of(note2, note3))
        );
    }

    @Test 
    void shouldReturnEmptyListWhenNoNotesMatchFilters() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user2);
        note1.setCategory(category1);

        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user1);
        note2.setCategory(category2);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.flush();

        List<Note> result = noteRepo.filterNotes(user2, category2.getIdCategory());
        
        assertTrue(
            result.isEmpty()
        );
    }

    @Test
    void shouldDeleteAllNotesBelongingToUser() {

        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user1);

        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user1);

        Note note3 = new Note();
        note3.setContent("List of ideas: ...");
        note3.setUser(user2);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.flush();

        int deleted = noteRepo.deleteByUser(user1);

        entityManager.clear();

        Note result1 = entityManager.find(Note.class, note1.getIdNote());
        Note result2 = entityManager.find(Note.class, note2.getIdNote());
        Note result3 = entityManager.find(Note.class, note3.getIdNote());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }

    @Test 
    void shouldClearCategoryFromUserNotes() {
        Note note1 = new Note();
        note1.setContent("Grocery list: apples, tomatoes, milk");
        note1.setUser(user1);
        note1.setCategory(category1);

        Note note2 = new Note();
        note2.setContent("Note content");
        note2.setUser(user1);
        note2.setCategory(category1);

        Note note3 = new Note();
        note3.setContent("List of ideas: ...");
        note3.setUser(user1);
        note3.setCategory(category2);

        Note note4 = new Note();
        note4.setContent("List of ideas: ...");
        note4.setUser(user2);
        note4.setCategory(category1);

        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.persist(note3);
        entityManager.flush();

        noteRepo.clearCategoryByCategoryAndUser(category1.getIdCategory(), user1);

        entityManager.clear();

        Note result1 = entityManager.find(Note.class, note1.getIdNote());
        Note result2 = entityManager.find(Note.class, note2.getIdNote());
        Note result3 = entityManager.find(Note.class, note3.getIdNote());
        Note result4 = entityManager.find(Note.class, note4.getIdNote());

        assertTrue(
            result1.getCategory() == null
            && result2.getCategory() == null
            && result3.getCategory().equals(category2)
            && result4.getCategory().equals(category1)
        );
    }
}
