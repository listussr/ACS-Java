package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Список документов. XML требует один корневой элемент, поэтому список обёрнут:
 * XML:  <documents count="3" href="..."> <document .../> <document .../> </documents>
 * JSON: {"count":3, "href":"...", "documents":[{...}, {...}]}
 */
@XmlRootElement(name = "documents")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonbPropertyOrder({"count", "href", "documents", "apiRoot"})
public class DocumentListDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private String href;

    @XmlAttribute
    private int count;

    @XmlElement(name = "document")
    @JsonbProperty("documents")
    private List<DocumentDto> items = new ArrayList<>();

    public DocumentListDto() {
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<DocumentDto> getItems() { return items; }
    public void setItems(List<DocumentDto> items) { this.items = items; }
}
