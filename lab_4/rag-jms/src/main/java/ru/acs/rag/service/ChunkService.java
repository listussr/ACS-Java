package ru.acs.rag.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Stateless
public class ChunkService {

    @PersistenceContext(unitName = "ragPU")
    private EntityManager em;

    public List<Chunk> findByDocument(Long documentId) {
        return em.createQuery(
                "SELECT c FROM Chunk c WHERE c.document.id = :docId ORDER BY c.chunkIndex", Chunk.class)
                .setParameter("docId", documentId)
                .getResultList();
    }

    public Chunk find(Long id) {
        return em.find(Chunk.class, id);
    }

    public Chunk create(Long documentId, Chunk chunk) {
        Document document = em.find(Document.class, documentId);
        if (document == null) {
            throw new IllegalArgumentException("Документ " + documentId + " не найден");
        }
        if (chunk.getChunkIndex() == null) {
            chunk.setChunkIndex(nextIndex(documentId));
        }
        chunk.setDocument(document);
        chunk.setTokenCount(SimpleTokenizer.countTokens(chunk.getContent()));
        em.persist(chunk);
        return chunk;
    }

    public Chunk update(Chunk changed) {
        Chunk chunk = em.find(Chunk.class, changed.getId());
        if (chunk == null) {
            throw new IllegalArgumentException("Чанк " + changed.getId() + " не найден");
        }
        if (changed.getChunkIndex() != null) {
            chunk.setChunkIndex(changed.getChunkIndex());
        }
        chunk.setContent(changed.getContent());
        chunk.setTokenCount(SimpleTokenizer.countTokens(changed.getContent()));
        return chunk;
    }

    public void delete(Long id) {
        Chunk chunk = em.find(Chunk.class, id);
        if (chunk != null) {
            em.remove(chunk);
        }
    }

    public List<SearchHit> search(String query, int topK) {
        Set<String> queryWords = SimpleTokenizer.words(query);
        List<SearchHit> hits = new ArrayList<>();
        if (queryWords.isEmpty()) {
            return hits;
        }
        for (Chunk chunk : em.createQuery("SELECT c FROM Chunk c", Chunk.class).getResultList()) {
            Set<String> chunkWords = SimpleTokenizer.words(chunk.getContent());
            int score = 0;
            for (String w : queryWords) {
                if (chunkWords.contains(w)) {
                    score++;
                }
            }
            if (score > 0) {
                hits.add(new SearchHit(chunk, score));
            }
        }
        hits.sort(Comparator.comparingInt(SearchHit::getScore).reversed());
        return hits.size() > topK ? new ArrayList<>(hits.subList(0, topK)) : hits;
    }

    private int nextIndex(Long documentId) {
        Integer max = em.createQuery(
                "SELECT MAX(c.chunkIndex) FROM Chunk c WHERE c.document.id = :docId", Integer.class)
                .setParameter("docId", documentId)
                .getSingleResult();
        return max == null ? 0 : max + 1;
    }
}
