package ru.acs.rag.rest;

import jakarta.ws.rs.core.UriInfo;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;
import ru.acs.rag.rest.dto.ChunkDto;
import ru.acs.rag.rest.dto.ChunkListDto;
import ru.acs.rag.rest.dto.DocumentDto;

import java.util.List;

/**
 * Перевод сущностей в DTO и обратно.
 */
public class DtoMapper {

    private final String api;

    public DtoMapper(UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();
        this.api = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    public String api() { return api; }

    public String documentsHref() { return api + "/documents"; }
    public String documentHref(Long id) { return api + "/documents/" + id; }
    public String documentChunksHref(Long id) { return api + "/documents/" + id + "/chunks"; }
    public String chunkHref(Long id) { return api + "/chunks/" + id; }

    public DocumentDto toSummary(Document d, int chunkCount) {
        DocumentDto dto = new DocumentDto();
        dto.setId(d.getId());
        dto.setHref(documentHref(d.getId()));
        dto.setTitle(d.getTitle());
        dto.setSource(d.getSource());
        dto.setLanguage(d.getLanguage());
        dto.setTokenCount(d.getTokenCount());
        dto.setChunkCount(chunkCount);
        dto.setCreatedAt(d.getCreatedAt() == null ? null : d.getCreatedAt().withNano(0).toString());
        dto.setChunksHref(documentChunksHref(d.getId()));
        return dto;
    }

    public DocumentDto toDetail(Document d, List<Chunk> chunks) {
        DocumentDto dto = toSummary(d, chunks.size());
        dto.setApiRoot(api);
        dto.setContent(d.getContent());
        dto.setChunks(chunks.stream().map(this::toRef).toList());
        return dto;
    }

    public ChunkDto toDto(Chunk c) {
        ChunkDto dto = toRef(c);
        dto.setContent(c.getContent());
        dto.setDocumentId(c.getDocument().getId());
        dto.setDocumentTitle(c.getDocument().getTitle());
        dto.setDocumentHref(documentHref(c.getDocument().getId()));
        return dto;
    }

    public ChunkDto toRef(Chunk c) {
        ChunkDto dto = new ChunkDto();
        dto.setId(c.getId());
        dto.setIndex(c.getChunkIndex());
        dto.setHref(chunkHref(c.getId()));
        dto.setTokenCount(c.getTokenCount());
        return dto;
    }

    public ChunkListDto toChunkList(Document d, List<Chunk> chunks) {
        ChunkListDto list = new ChunkListDto();
        list.setApiRoot(api);
        list.setDocumentId(d.getId());
        list.setHref(documentChunksHref(d.getId()));
        list.setDocumentTitle(d.getTitle());
        list.setDocumentHref(documentHref(d.getId()));
        for (Chunk c : chunks) {
            ChunkDto dto = toRef(c);
            dto.setContent(c.getContent());
            list.getItems().add(dto);
            list.setTotalTokens(list.getTotalTokens() + c.getTokenCount());
        }
        list.setCount(chunks.size());
        return list;
    }

    public Document toEntity(DocumentDto dto) {
        Document d = new Document();
        d.setTitle(dto.getTitle());
        d.setSource(dto.getSource());
        d.setLanguage(dto.getLanguage() == null || dto.getLanguage().isBlank() ? "ru" : dto.getLanguage());
        d.setContent(dto.getContent());
        return d;
    }

    public Chunk toEntity(ChunkDto dto) {
        Chunk c = new Chunk();
        c.setChunkIndex(dto.getIndex());
        c.setContent(dto.getContent());
        return c;
    }
}
