package ru.acs.rag.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Запись журнала изменений
 */
@Entity
@Table(name = "change_log")
public class ChangeLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_type", nullable = false, length = 10)
    private String changeType;

    @Column(name = "entity_class", nullable = false, length = 100)
    private String entityClass;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(name = "message_id", nullable = false, unique = true, length = 100)
    private String messageId;

    public ChangeLog() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getEntityClass() { return entityClass; }
    public void setEntityClass(String entityClass) { this.entityClass = entityClass; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
}
