package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Чанки одного документа.
 */
@XmlRootElement(name = "chunks")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"documentTitle", "documentHref", "totalTokens", "items"})
@JsonbPropertyOrder({"documentId", "count", "href", "documentTitle", "documentHref", "totalTokens",
        "chunks", "apiRoot"})
public class ChunkListDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private Long documentId;

    @XmlAttribute
    private int count;

    @XmlAttribute
    private String href;

    private String documentTitle;
    private String documentHref;
    private int totalTokens;

    @XmlElement(name = "chunk")
    @JsonbProperty("chunks")
    private List<ChunkDto> items = new ArrayList<>();

    public ChunkListDto() {
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public String getDocumentHref() { return documentHref; }
    public void setDocumentHref(String documentHref) { this.documentHref = documentHref; }

    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

    public List<ChunkDto> getItems() { return items; }
    public void setItems(List<ChunkDto> items) { this.items = items; }
}
