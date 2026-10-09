package com.example.task.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 
 * <b>Subtask</b><br>
 * 
 * Entity class representing subtask object within the system.
 * It is connected with the "subtasks" table in the database.
 * It stores subtask information: database ID, title, and completion status.
 * It has a "Many-to-one" relationsip with the Task entity, meaning that multiple subtasks can be assigned to one task.
 */
@Entity
@Table(name = "subtasks")
public class Subtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subtask")
    private Long idSubtask;

    private String title;

    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_task", nullable = false)
    private Task task;

    public Subtask(){}

    public Subtask(String title, boolean completed, Task task) {
        this.title = title;
        this.completed = completed;
        this.task = task;
    }

    public Long getIdSubtask() { return idSubtask; }
    public void setIdSubtask(Long idSubtask) { this.idSubtask = idSubtask; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

}
