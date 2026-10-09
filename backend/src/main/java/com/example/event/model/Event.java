package com.example.event.model;

import com.example.category.model.Category;
import com.example.common.enums.RepeatedFrequency;
import com.example.user.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * 
 * <b>Event</b><br>
 * 
 * Entity class representing event object within the system.
 * It is connected with the "events" table in the database.
 * It stores event information, including database ID, title, details, dates, timing, and user's data that it belongs to.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvent;

    @Column(nullable = false)
    private String title;
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", referencedColumnName = "id_category")
    private Category category;

    @Column(nullable = false)
    private LocalDate dateStart;

    @Column(nullable = false)
    private LocalDate dateEnd;

    private boolean duration;

    @Column(name = "time_start")
    private LocalTime timeStart;

    @Column(name = "time_end")
    private LocalTime timeEnd;
    private boolean repeated;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeated_frequency")
    private RepeatedFrequency repeatedFrequency;

    @ElementCollection
    private List<LocalDate> dates = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    public Event() {};

    public Long getIdEvent() { return idEvent; }
    public void setIdEvent(Long idEvent) { this.idEvent = idEvent; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getDateStart() { return dateStart; }
    public void setDateStart(LocalDate dateStart) { this.dateStart = dateStart; }

    public LocalDate getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDate dateEnd) { this.dateEnd = dateEnd; }

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

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}
