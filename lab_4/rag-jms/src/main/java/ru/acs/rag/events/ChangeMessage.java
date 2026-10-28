package ru.acs.rag.events;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * Формат JMS-сообщения об изменении сущности.
 */
public final class ChangeMessage {

    public enum Type { INSERT, UPDATE, DELETE }

    public static final String FORMAT = "rag-change/1";

    public static final String PROP_CHANGE_TYPE = "changeType";
    public static final String PROP_ENTITY = "entity";
    public static final String PROP_ENTITY_ID = "entityId";

    private static final Set<String> ENTITIES = Set.of("Document", "Chunk");

    private final Type type;
    private final String entity;
    private final long entityId;
    private final LocalDateTime changedAt;
    private final JsonObject data;

    private ChangeMessage(Type type, String entity, long entityId, LocalDateTime changedAt, JsonObject data) {
        this.type = type;
        this.entity = entity;
        this.entityId = entityId;
        this.changedAt = changedAt;
        this.data = data;
    }


    public static ChangeMessage of(Type type, Object entity) {
        if (entity instanceof Document d) {
            JsonObject data = Json.createObjectBuilder()
                    .add("id", d.getId())
                    .add("title", d.getTitle())
                    .add("source", nullable(d.getSource()))
                    .add("language", d.getLanguage())
                    .add("tokenCount", d.getTokenCount())
                    .add("content", d.getContent())
                    .build();
            return new ChangeMessage(type, "Document", d.getId(), LocalDateTime.now(), data);
        }
        if (entity instanceof Chunk c) {
            JsonObject data = Json.createObjectBuilder()
                    .add("id", c.getId())
                    .add("documentId", c.getDocument().getId())
                    .add("chunkIndex", c.getChunkIndex())
                    .add("tokenCount", c.getTokenCount())
                    .add("content", c.getContent())
                    .build();
            return new ChangeMessage(type, "Chunk", c.getId(), LocalDateTime.now(), data);
        }
        throw new IllegalArgumentException("Неизвестная сущность: " + entity.getClass().getName());
    }

    public String toJson() {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("format", FORMAT)
                .add("changeType", type.name())
                .add("entity", entity)
                .add("entityId", entityId)
                .add("changedAt", changedAt.toString())
                .add("data", data);
        return b.build().toString();
    }


    public static ChangeMessage parse(Message message) throws InvalidChangeMessageException, JMSException {
        if (!(message instanceof TextMessage textMessage)) {
            throw new InvalidChangeMessageException("ожидался TextMessage, получен "
                    + message.getClass().getSimpleName());
        }
        String text = textMessage.getText();
        if (text == null || text.isBlank()) {
            throw new InvalidChangeMessageException("пустое тело сообщения");
        }
        JsonObject o;
        try {
            o = Json.createReader(new StringReader(text)).readObject();
        } catch (JsonException | IllegalStateException e) {
            throw new InvalidChangeMessageException("тело не является JSON-объектом: " + e.getMessage());
        }
        if (!FORMAT.equals(string(o, "format"))) {
            throw new InvalidChangeMessageException("неизвестный формат: " + string(o, "format"));
        }
        Type type;
        try {
            type = Type.valueOf(string(o, "changeType"));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidChangeMessageException("неверный changeType: " + string(o, "changeType"));
        }
        String entity = string(o, "entity");
        if (!ENTITIES.contains(entity)) {
            throw new InvalidChangeMessageException("неизвестная сущность: " + entity);
        }
        if (!(o.get("entityId") instanceof JsonNumber id)) {
            throw new InvalidChangeMessageException("нет числового entityId");
        }
        LocalDateTime changedAt;
        try {
            changedAt = LocalDateTime.parse(string(o, "changedAt"));
        } catch (DateTimeParseException | NullPointerException e) {
            throw new InvalidChangeMessageException("неверный changedAt: " + string(o, "changedAt"));
        }
        if (!(o.get("data") instanceof JsonObject data) || !data.containsKey("tokenCount")) {
            throw new InvalidChangeMessageException("нет объекта data с новыми значениями полей");
        }
        if (!type.name().equals(message.getStringProperty(PROP_CHANGE_TYPE))
                || !entity.equals(message.getStringProperty(PROP_ENTITY))) {
            throw new InvalidChangeMessageException("свойства сообщения не совпадают с телом");
        }
        return new ChangeMessage(type, entity, id.longValue(), changedAt, data);
    }

    private static String string(JsonObject o, String key) {
        JsonValue v = o.get(key);
        return v != null && v.getValueType() == JsonValue.ValueType.STRING ? o.getString(key) : null;
    }

    private static JsonValue nullable(String s) {
        return s == null ? JsonValue.NULL : Json.createValue(s);
    }

    public Type getType() { return type; }
    public String getEntity() { return entity; }
    public long getEntityId() { return entityId; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public JsonObject getData() { return data; }
}
