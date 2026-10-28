package ru.acs.rag.mdb;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.acs.rag.entity.ChangeLog;
import ru.acs.rag.events.ChangeMessage;
import ru.acs.rag.events.InvalidChangeMessageException;

import java.time.LocalDateTime;
import java.util.logging.Logger;


@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/ragChanges"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "jms/ragConnectionFactory"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "rag-change-log"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "rag-change-log")
})
public class ChangeLogMdb implements MessageListener {

    private static final Logger LOG = Logger.getLogger(ChangeLogMdb.class.getName());

    @PersistenceContext(unitName = "ragPU")
    private EntityManager em;

    @Override
    public void onMessage(Message message) {
        try {
            ChangeMessage change = ChangeMessage.parse(message);
            String messageId = message.getJMSMessageID();

            Long existing = em.createQuery("SELECT COUNT(c) FROM ChangeLog c WHERE c.messageId = :id", Long.class)
                    .setParameter("id", messageId)
                    .getSingleResult();
            if (existing > 0) {
                LOG.info("Сообщение " + messageId + " уже записано в журнал, пропускаем");
                return;
            }

            ChangeLog entry = new ChangeLog();
            entry.setChangeType(change.getType().name());
            entry.setEntityClass(change.getEntity());
            entry.setEntityId(change.getEntityId());
            entry.setDetails(change.getData().toString());
            entry.setChangedAt(change.getChangedAt());
            entry.setLoggedAt(LocalDateTime.now());
            entry.setMessageId(messageId);
            em.persist(entry);
        } catch (InvalidChangeMessageException e) {
            LOG.warning("Отброшено некорректное сообщение: " + e.getMessage());
        } catch (JMSException e) {
            throw new EJBException(e);
        }
    }
}
