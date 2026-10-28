package ru.acs.rag.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;

import java.util.List;


@Stateless
public class DocumentService {

    @PersistenceContext(unitName = "ragPU")
    private EntityManager em;

    public List<Document> findAll() {
        return em.createQuery(
                "SELECT DISTINCT d FROM Document d LEFT JOIN FETCH d.chunks ORDER BY d.id", Document.class)
                .getResultList();
    }

    public Document find(Long id) {
        return em.find(Document.class, id);
    }

    public Document create(Document document) {
        document.setTokenCount(SimpleTokenizer.countTokens(document.getContent()));
        em.persist(document);
        return document;
    }

    public Document update(Document changed) {
        Document document = em.find(Document.class, changed.getId());
        if (document == null) {
            throw new IllegalArgumentException("Документ " + changed.getId() + " не найден");
        }
        document.setTitle(changed.getTitle());
        document.setSource(changed.getSource());
        document.setLanguage(changed.getLanguage());
        document.setContent(changed.getContent());
        document.setTokenCount(SimpleTokenizer.countTokens(changed.getContent()));
        return document;
    }

    public void delete(Long id) {
        Document document = em.find(Document.class, id);
        if (document != null) {
            removeChunks(id);
            em.remove(document);
        }
    }

    public int rechunk(Long documentId, int chunkSize, int overlap) {
        Document document = em.find(Document.class, documentId);
        if (document == null) {
            throw new IllegalArgumentException("Документ " + documentId + " не найден");
        }
        List<String> parts = SimpleTokenizer.split(document.getContent(), chunkSize, overlap);


        removeChunks(documentId);
        em.flush();

        for (int i = 0; i < parts.size(); i++) {
            Chunk chunk = new Chunk();
            chunk.setDocument(document);
            chunk.setChunkIndex(i);
            chunk.setContent(parts.get(i));
            chunk.setTokenCount(SimpleTokenizer.countTokens(parts.get(i)));
            em.persist(chunk);
        }
        return parts.size();
    }

    private void removeChunks(Long documentId) {
        for (Chunk c : em.createQuery("SELECT c FROM Chunk c WHERE c.document.id = :id", Chunk.class)
                .setParameter("id", documentId).getResultList()) {
            em.remove(c);
        }
    }
}
