package ru.acs.rag.web;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.service.ChunkService;

import java.io.Serializable;

@Named
@ViewScoped
public class ChunkEditBean implements Serializable {

    @EJB
    private ChunkService chunkService;

    private Long id;
    private Long docId;
    private Chunk chunk;

    public void load() {
        if (chunk != null) {
            return;
        }
        if (id != null) {
            chunk = chunkService.find(id);
        }
        if (chunk == null) {
            chunk = new Chunk();
        } else {
            docId = chunk.getDocument().getId();
        }
    }

    public String save() {
        try {
            if (chunk.getId() == null) {
                chunkService.create(docId, chunk);
                Messages.info("Чанк добавлен");
            } else {
                chunkService.update(chunk);
                Messages.info("Чанк сохранён");
            }
            Messages.keepAfterRedirect();
            return "chunks?faces-redirect=true&docId=" + docId;
        } catch (Exception e) {
            Messages.error(e);
            return null;
        }
    }

    public boolean isNew() {
        return chunk == null || chunk.getId() == null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public Chunk getChunk() { return chunk; }
}
