package com.example.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.category.model.Category;
import com.example.category.repository.CategoryRepository;
import com.example.user.model.UserEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;

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
    void shouldFindAllCategoriesBelongingToUser() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        Category category2 = new Category();
        category2.setName("Personal");
        category2.setColor("Green");
        category2.setUser(user1);

        Category category3 = new Category();
        category3.setName("Shopping");
        category3.setColor("Red");
        category3.setUser(user2);

        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.persist(category3);
        entityManager.flush();

        List<Category> result = categoryRepo.findByUser(user1);

        assertTrue(
            result.size() == 2
            && result.contains(category1)
            && result.contains(category2)
            && !result.contains(category3)
        );
    }

    @Test
    void shouldFindCategoryByIdAndUserId() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        entityManager.persist(category1);
        entityManager.flush();

        Optional<Category> result = categoryRepo.findByIdAndIdUser(category1.getIdCategory(), user1.getIdUser());

        assertTrue(
            result.isPresent()
            && result.get().equals(category1)
            && result.get().getName().equals("Work")
            && result.get().getColor().equals("Blue")
        );
    }

    @Test
    void shouldNotFindCategoryBelongingToAnotherUser() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        entityManager.persist(category1);
        entityManager.flush();

        Optional<Category> result = categoryRepo.findByIdAndIdUser(category1.getIdCategory(), user2.getIdUser());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenCategoryNameExistsForUser() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        entityManager.persist(category1);
        entityManager.flush();

        Boolean result = categoryRepo.existsByName("Work", user1);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenCategoryNameDoesNotExistForUser() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        entityManager.persist(category1);
        entityManager.flush();

        Boolean result = categoryRepo.existsByName("Shopping", user1);

        assertTrue(!result);
    }

    @Test
    void shouldReturnFalseWhenCategoryNameExistsForAnotherUser() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        entityManager.persist(category1);
        entityManager.flush();

        Boolean result = categoryRepo.existsByName("Work", user2);

        assertTrue(!result);
    }

    @Test
    void shouldDeleteAllCategoriesBelongingToUser() {

        Category category1 = new Category();
        category1.setName("Work");
        category1.setColor("Blue");
        category1.setUser(user1);

        Category category2 = new Category();
        category2.setName("Personal");
        category2.setColor("Green");
        category2.setUser(user1);

        Category category3 = new Category();
        category3.setName("Shopping");
        category3.setColor("Red");
        category3.setUser(user2);

        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.persist(category3);
        entityManager.flush();

        int deleted = categoryRepo.deleteByUser(user1);

        entityManager.clear();

        Category result1 = entityManager.find(Category.class, category1.getIdCategory());
        Category result2 = entityManager.find(Category.class, category2.getIdCategory());
        Category result3 = entityManager.find(Category.class, category3.getIdCategory());

        assertTrue(
            deleted == 2
            && result1 == null
            && result2 == null
            && result3 != null
        );
    }
}

