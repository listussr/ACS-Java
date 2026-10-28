package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал изменений в REST API (GET /api/changes).
 */
@XmlRootElement(name = "changes")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonbPropertyOrder({"count", "href", "changes", "apiRoot"})
public class ChangeListDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private String href;

    @XmlAttribute
    private int count;

    @XmlElement(name = "change")
    @JsonbProperty("changes")
    private List<Change> items = new ArrayList<>();

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"entityClass", "entityId", "entityHref", "changedAt", "loggedAt", "details"})
    @JsonbPropertyOrder({"id", "type", "entityClass", "entityId", "entityHref", "changedAt", "loggedAt", "details"})
    public static class Change {
        @XmlAttribute
        private Long id;
        @XmlAttribute
        private String type;
        private String entityClass;
        private Long entityId;
        private String entityHref;
        private String changedAt;
        private String loggedAt;
        private String details;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEntityClass() { return entityClass; }
        public void setEntityClass(String entityClass) { this.entityClass = entityClass; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public String getEntityHref() { return entityHref; }
        public void setEntityHref(String entityHref) { this.entityHref = entityHref; }
        public String getChangedAt() { return changedAt; }
        public void setChangedAt(String changedAt) { this.changedAt = changedAt; }
        public String getLoggedAt() { return loggedAt; }
        public void setLoggedAt(String loggedAt) { this.loggedAt = loggedAt; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<Change> getItems() { return items; }
    public void setItems(List<Change> items) { this.items = items; }
}
