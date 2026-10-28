import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.MapMessage;
import jakarta.jms.Topic;

import java.time.LocalDateTime;

/**
 * Отдельный JMS-клиент для демонстрации ЛР4 (не часть приложения).
 * Подключается к брокеру OpenMQ, встроенному в GlassFish (localhost:7676),
 * и публикует сообщения в физический топик RagChangesTopic.
 *
 *   run-test-sender.cmd bad    — 3 некорректных сообщения: MDB должны их отбросить (см. server.log)
 *   run-test-sender.cmd valid  — 1 корректное сообщение о «большом» документе id 999:
 *                                запись в журнале + письмо (удобно для проверки durable-подписки)
 */
public class TestSender {

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "bad";

        ConnectionFactory cf = new ConnectionFactory();
        cf.setProperty(ConnectionConfiguration.imqAddressList, "mq://localhost:7676");
        Topic topic = new com.sun.messaging.Topic("RagChangesTopic");

        try (JMSContext ctx = cf.createContext("admin", "admin")) {
            if ("valid".equals(mode)) {
                String json = "{\"format\":\"rag-change/1\",\"changeType\":\"UPDATE\",\"entity\":\"Document\","
                        + "\"entityId\":999,\"changedAt\":\"" + LocalDateTime.now() + "\","
                        + "\"data\":{\"id\":999,\"title\":\"Сообщение от TestSender\",\"source\":null,"
                        + "\"language\":\"ru\",\"tokenCount\":500,\"content\":\"...\"}}";
                producer(ctx, "UPDATE", "Document").send(topic, json);
                System.out.println("Отправлено корректное сообщение (Document 999, 500 токенов)");
                return;
            }

            // 1. Неверный ТИП сообщения: MapMessage вместо TextMessage
            MapMessage map = ctx.createMapMessage();
            map.setString("changeType", "INSERT");
            map.setStringProperty("changeType", "INSERT");
            map.setStringProperty("entity", "Document");
            ctx.createProducer().send(topic, map);
            System.out.println("1. MapMessage вместо TextMessage");

            // 2. Тело — не JSON
            producer(ctx, "INSERT", "Document").send(topic, "это не JSON");
            System.out.println("2. TextMessage с телом не в формате JSON");

            // 3. JSON, но без обязательных полей и с неизвестным changeType
            producer(ctx, "UPDATE", "Document").send(topic,
                    "{\"format\":\"rag-change/1\",\"changeType\":\"RENAME\",\"entity\":\"Document\"}");
            System.out.println("3. JSON с неверным changeType и без entityId/data");
        }
    }

    private static JMSProducer producer(JMSContext ctx, String changeType, String entity) {
        return ctx.createProducer()
                .setProperty("changeType", changeType)
                .setProperty("entity", entity);
    }
}
