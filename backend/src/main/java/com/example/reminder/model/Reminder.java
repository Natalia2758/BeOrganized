package com.example.reminder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.reminder.enums.Timing;
import com.example.user.model.UserEntity;
import com.example.reminder.enums.ReminderType;
import com.example.reminder.enums.ReminderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reminder")
    private Long idReminder;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReminderType type;

    @Column(name = "id_event")
    private Long idEvent;

    @Column(name = "id_task")
    private Long idTask;

    @Column(name = "repeat_for_all")
    private boolean repeatForAll;

    @Column(name = "remind_at", nullable = false)
    @Enumerated(EnumType.STRING)
    private Timing remindAt;

    @Column(name = "remind_date", nullable = false)
    private LocalDate remindDate;

    @Column(name = "remind_time", nullable = false)
    private LocalTime remindTime;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;

    public Reminder(){}

    public Long getIdReminder() { return idReminder; }
    public void setIdReminder(Long idReminder) { this.idReminder = idReminder; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }

    public Long getIdEvent() { return idEvent; }
    public void setIdEvent(Long idEvent) { this.idEvent = idEvent; }

    public Long getIdTask() { return idTask; }
    public void setIdTask(Long idTask) { this.idTask = idTask; }

    public boolean isRepeatForAll() { return repeatForAll; }
    public void setRepeatForAll(boolean repeatForAll) { this.repeatForAll = repeatForAll; }

    public Timing getRemindAt() { return remindAt; }
    public void setRemindAt(Timing remindAt) { this.remindAt = remindAt; }

    public LocalDate getRemindDate() { return remindDate; }
    public void setRemindDate(LocalDate remindDate) { this.remindDate = remindDate; }

    public LocalTime getRemindTime() { return remindTime; }
    public void setRemindTime(LocalTime remindTime) { this.remindTime = remindTime; }

    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }

    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

}
