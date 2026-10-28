package ru.acs.rag.events;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Topic;

import java.util.logging.Logger;

/**
 * Отправляет JMS-сообщения об изменениях в топик jms/ragChanges.
 */
@ApplicationScoped
public class ChangePublisher {

    private static final Logger LOG = Logger.getLogger(ChangePublisher.class.getName());

    @Inject
    @JMSConnectionFactory("jms/ragConnectionFactory")
    private JMSContext jms;

    @Resource(lookup = "jms/ragChanges")
    private Topic topic;

    public void publish(ChangeMessage.Type type, Object entity) {
        ChangeMessage message = ChangeMessage.of(type, entity);
        jms.createProducer()
                .setProperty(ChangeMessage.PROP_CHANGE_TYPE, message.getType().name())
                .setProperty(ChangeMessage.PROP_ENTITY, message.getEntity())
                .setProperty(ChangeMessage.PROP_ENTITY_ID, message.getEntityId())
                .send(topic, message.toJson());
        LOG.fine(() -> "Отправлено: " + type + " " + message.getEntity() + "#" + message.getEntityId());
    }
}
