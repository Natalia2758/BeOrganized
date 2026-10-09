package com.example.category.model;

import com.example.user.model.UserEntity;

import jakarta.persistence.*;

/**
 * 
 * <b>Category</b><br>
 * 
 * Entity class representing category object within the system.
 * It is connected with the "categories" table in the database.
 * It stores category information, including database ID, name, color, and user that it belongs to.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_category")
    private Long idCategory;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    public Category(){}

    public Category(String title, String color) {
        this.name = title;
        this.color = color;
    }

    public Long getIdCategory() { return idCategory; }
    public void setIdCategory(Long idCategory) { this.idCategory = idCategory; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}
