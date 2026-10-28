package ru.acs.rag.events;

import jakarta.inject.Inject;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

/**
 * JPA-слушатель изменений сущностей. Подключается к Document и Chunk аннотацией @EntityListeners.
 */
public class EntityChangeListener {

    @Inject
    private ChangePublisher publisher;

    @PostPersist
    void inserted(Object entity) {
        publisher.publish(ChangeMessage.Type.INSERT, entity);
    }

    @PostUpdate
    void updated(Object entity) {
        publisher.publish(ChangeMessage.Type.UPDATE, entity);
    }

    @PostRemove
    void removed(Object entity) {
        publisher.publish(ChangeMessage.Type.DELETE, entity);
    }
}
