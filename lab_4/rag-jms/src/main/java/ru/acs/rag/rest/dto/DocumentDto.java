package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;

import java.util.List;


@XmlRootElement(name = "document")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"title", "source", "language", "tokenCount", "chunkCount", "createdAt",
        "content", "chunksHref", "chunks"})
@JsonbPropertyOrder({"id", "href", "title", "source", "language", "tokenCount", "chunkCount",
        "createdAt", "content", "chunksHref", "chunks", "apiRoot"})
public class DocumentDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private Long id;

    @XmlAttribute
    private String href;

    @NotBlank(message = "title: название обязательно")
    @Size(max = 200, message = "title: не длиннее 200 символов")
    private String title;

    @Size(max = 500, message = "source: не длиннее 500 символов")
    private String source;

    @Size(max = 10, message = "language: не длиннее 10 символов")
    private String language;

    private Integer tokenCount;
    private Integer chunkCount;
    private String createdAt;

    @NotBlank(message = "content: текст документа обязателен")
    private String content;

    private String chunksHref;

    @XmlElementWrapper(name = "chunks")
    @XmlElement(name = "chunk")
    private List<ChunkDto> chunks;

    public DocumentDto() {
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getChunksHref() { return chunksHref; }
    public void setChunksHref(String chunksHref) { this.chunksHref = chunksHref; }

    public List<ChunkDto> getChunks() { return chunks; }
    public void setChunks(List<ChunkDto> chunks) { this.chunks = chunks; }
}
