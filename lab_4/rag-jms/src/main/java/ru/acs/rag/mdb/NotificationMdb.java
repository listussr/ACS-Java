package ru.acs.rag.mdb;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.json.JsonObject;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import ru.acs.rag.events.ChangeMessage;
import ru.acs.rag.events.InvalidChangeMessageException;

import java.util.Date;
import java.util.logging.Logger;

/**
 * MDB оповещений.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/ragChanges"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "jms/ragConnectionFactory"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "rag-notifier"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "rag-notifier"),
        @ActivationConfigProperty(propertyName = "messageSelector",
                propertyValue = "entity = 'Document' AND changeType IN ('INSERT', 'UPDATE')")
})
public class NotificationMdb implements MessageListener {

    private static final Logger LOG = Logger.getLogger(NotificationMdb.class.getName());

    @Resource(lookup = "mail/ragMail")
    private Session mailSession;

    static final String RECIPIENTS_JNDI = "rag/notify/recipients";
    static final String MAX_TOKENS_JNDI = "rag/notify/maxDocumentTokens";
    private static final String DEFAULT_RECIPIENTS = "kb-admin@rag.local";
    private static final int DEFAULT_MAX_TOKENS = 150;

    @Resource(name = "notify/appUrl")
    private String appUrl = "http://localhost:8080/rag-jms";

    @Override
    public void onMessage(Message message) {
        ChangeMessage change;
        try {
            change = ChangeMessage.parse(message);
        } catch (InvalidChangeMessageException e) {
            LOG.warning("Отброшено некорректное сообщение: " + e.getMessage());
            return;
        } catch (JMSException e) {
            throw new EJBException(e);
        }

        JsonObject doc = change.getData();
        int tokens = doc.getInt("tokenCount");
        int maxDocumentTokens = config(MAX_TOKENS_JNDI, DEFAULT_MAX_TOKENS, Integer.class);
        if (tokens <= maxDocumentTokens) {
            return;
        }
        sendMail(change, doc, tokens, maxDocumentTokens, config(RECIPIENTS_JNDI, DEFAULT_RECIPIENTS, String.class));
    }

    private static <T> T config(String jndiName, T defaultValue, Class<T> type) {
        try {
            return type.cast(new InitialContext().lookup(jndiName));
        } catch (NamingException | ClassCastException e) {
            LOG.warning("Настройка " + jndiName + " не найдена на сервере, используется " + defaultValue);
            return defaultValue;
        }
    }

    private void sendMail(ChangeMessage change, JsonObject doc, int tokens, int maxDocumentTokens,
                          String recipients) {
        String title = doc.getString("title");
        String action = change.getType() == ChangeMessage.Type.INSERT ? "добавлен" : "изменён";
        String subject = "[RAG KB] Документ «" + title + "» не помещается в контекст: "
                + tokens + " токенов (порог " + maxDocumentTokens + ")";
        String body = "Здравствуйте!\n\n"
                + "В базе знаний " + action + " документ, размер которого превышает порог.\n\n"
                + "Документ:   " + title + " (id " + change.getEntityId() + ")\n"
                + "Источник:   " + (doc.isNull("source") ? "—" : doc.getString("source")) + "\n"
                + "Токенов:    " + tokens + " (порог " + maxDocumentTokens + ", превышение на "
                + (tokens - maxDocumentTokens) + ")\n"
                + "Изменение:  " + change.getType() + " в " + change.getChangedAt().withNano(0) + "\n\n"
                + "Такой документ нельзя целиком подать модели как контекст — его нужно разбить на чанки:\n"
                + appUrl + "/chunks.xhtml?docId=" + change.getEntityId() + "\n\n"
                + "Документ в REST API: " + appUrl + "/api/documents/" + change.getEntityId() + "\n\n"
                + "--\nСистема оповещений RAG Knowledge Base (ЛР4)";
        try {
            MimeMessage mail = new MimeMessage(mailSession);
            mail.setFrom();
            mail.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(recipients));
            mail.setSubject(subject, "UTF-8");
            mail.setText(body, "UTF-8");
            mail.setSentDate(new Date());
            Transport.send(mail);
            LOG.info("Отправлено оповещение о документе " + change.getEntityId() + " для " + recipients);
        } catch (MessagingException e) {
            LOG.severe("Не удалось отправить письмо о документе " + change.getEntityId() + ": " + e.getMessage());
        }
    }
}
