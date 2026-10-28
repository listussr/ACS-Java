package ru.acs.rag.entity;

import jakarta.persistence.*;
import ru.acs.rag.events.EntityChangeListener;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;


@Entity
@EntityListeners(EntityChangeListener.class)
@Table(name = "chunks")
public class Chunk implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Min(value = 0, message = "Номер чанка не может быть отрицательным")
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @NotBlank(message = "Текст чанка не может быть пустым")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    public Chunk() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
}
