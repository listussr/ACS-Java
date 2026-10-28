package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.*;

/**
 * Представление чанка в REST.
 */
@XmlRootElement(name = "chunk")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tokenCount", "content", "documentId", "documentTitle", "documentHref"})
@JsonbPropertyOrder({"id", "index", "href", "tokenCount", "content", "documentId", "documentTitle",
        "documentHref", "apiRoot"})
public class ChunkDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private Long id;

    @XmlAttribute
    @Min(value = 0, message = "index: номер чанка не может быть отрицательным")
    private Integer index;

    @XmlAttribute
    private String href;

    private Integer tokenCount;

    @NotBlank(message = "content: текст чанка обязателен")
    private String content;

    private Long documentId;
    private String documentTitle;
    private String documentHref;

    public ChunkDto() {
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getIndex() { return index; }
    public void setIndex(Integer index) { this.index = index; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public String getDocumentHref() { return documentHref; }
    public void setDocumentHref(String documentHref) { this.documentHref = documentHref; }
}
