package com.example.task.model;

import com.example.category.model.Category;
import com.example.common.enums.RepeatedFrequency;
import com.example.task.enums.Priority;
import com.example.user.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * 
 * <b>Task</b><br>
 * 
 * Entity class representing task object within the system.
 * It is connected with the "tasks" table in the database.
 * It stores task information, including database ID, title, description, priority, dates, assigned subtasks etc.
 */
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_task")
    private Long idTask;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", referencedColumnName = "id_category")
    private Category category;

    @Column(name = "start_date")
    private LocalDate startDate;

    private boolean duration = false;

    @Column(name = "time_start")
    private LocalTime timeStart;

    @Column(name = "time_end")
    private LocalTime timeEnd;

    private boolean repeated = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeated_frequency")
    private RepeatedFrequency repeatedFrequency;

    @ElementCollection
    private List<LocalDate> dates = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Subtask> subtasks = new ArrayList<>();

    private long progress;

    private boolean finished = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    public Task(){}

    public Long getIdTask() { return idTask; }
    public void setIdTask(Long idTask) { this.idTask = idTask; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public boolean hasDuration() { return duration; }
    public void setDuration(boolean duration) { this.duration = duration; }

    public LocalTime getTimeStart() { return timeStart; }
    public void setTimeStart(LocalTime timeStart) { this.timeStart = timeStart; }

    public LocalTime getTimeEnd() { return timeEnd; }
    public void setTimeEnd(LocalTime timeEnd) { this.timeEnd = timeEnd; }

    public boolean isRepeated() { return repeated; }
    public void setRepeated(boolean repeated) { this.repeated = repeated; }

    public RepeatedFrequency getRepeatedFrequency() { return repeatedFrequency; }
    public void setRepeatedFrequency(RepeatedFrequency repeatedFrequency) { this.repeatedFrequency = repeatedFrequency; }

    public List<LocalDate> getDates() { return dates; }
    public void setDates(List<LocalDate> dates) { this.dates = dates; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public long getProgress() { return progress; }
    public void setProgress(long progress) { this.progress = progress; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}
