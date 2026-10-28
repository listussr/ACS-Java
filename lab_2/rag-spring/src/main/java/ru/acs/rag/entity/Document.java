package ru.acs.rag.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Документ базы знаний (таблица documents).
 */
@Entity
@Table(name = "documents")
public class Document implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название обязательно")
    @Size(max = 200, message = "Название длиннее 200 символов")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 500, message = "Источник длиннее 500 символов")
    @Column(length = 500)
    private String source;

    @NotBlank(message = "Укажите язык")
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String language = "ru";

    @NotBlank(message = "Текст документа не может быть пустым")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Считается в бизнес-слое, пользователь его не вводит. */
    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Formula("(select count(*) from chunks c where c.document_id = id)")
    private int chunkCount;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("chunkIndex")
    private List<Chunk> chunks = new ArrayList<>();

    public Document() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getChunkCount() { return chunkCount; }

    public List<Chunk> getChunks() { return chunks; }
    public void setChunks(List<Chunk> chunks) { this.chunks = chunks; }
}
