package com.example.note.model;

import java.time.LocalDate;

import com.example.category.model.Category;
import com.example.user.model.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 
 * <b>Note</b><br>
 * 
 * Entity class representing note object within the system.
 * It is connected with the "notes" table in the database.
 * It stores note information, including database ID, title, note content, date, and user's data that it belongs to.
 */
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Long idNote;

    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", referencedColumnName = "id_category")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    public Note(){}

    public Long getIdNote() { return idNote; }
    public void setIdNote(Long idNote) { this.idNote = idNote; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

}
