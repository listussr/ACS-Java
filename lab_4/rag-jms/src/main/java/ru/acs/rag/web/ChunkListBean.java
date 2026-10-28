package ru.acs.rag.web;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;
import ru.acs.rag.service.ChunkService;
import ru.acs.rag.service.DocumentService;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ChunkListBean implements Serializable {

    @EJB
    private DocumentService documentService;
    @EJB
    private ChunkService chunkService;

    private Long docId;
    private Document document;
    private List<Chunk> chunks;

    private int chunkSize = 40;
    private int overlap = 10;

    public void load() {
        document = documentService.find(docId);
        chunks = document == null ? List.of() : chunkService.findByDocument(docId);
    }

    public String rechunk() {
        try {
            int n = documentService.rechunk(docId, chunkSize, overlap);
            Messages.info("Документ разбит на " + n + " чанк(ов)");
        } catch (Exception e) {
            Messages.error(e);
        }
        load();
        return null;
    }

    public String delete(Long chunkId) {
        try {
            chunkService.delete(chunkId);
            Messages.info("Чанк удалён");
        } catch (Exception e) {
            Messages.error(e);
        }
        load();
        return null;
    }

    public int getTotalTokens() {
        return chunks.stream().mapToInt(Chunk::getTokenCount).sum();
    }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public Document getDocument() { return document; }
    public List<Chunk> getChunks() { return chunks; }

    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }

    public int getOverlap() { return overlap; }
    public void setOverlap(int overlap) { this.overlap = overlap; }
}
