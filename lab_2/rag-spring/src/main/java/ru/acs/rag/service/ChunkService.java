package ru.acs.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.acs.rag.dao.ChunkDao;
import ru.acs.rag.dao.DocumentDao;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Бизнес-логика работы с чанками и поиск контекста. */
@Service
@Transactional
public class ChunkService {

    private final ChunkDao chunkDao;
    private final DocumentDao documentDao;
    private final Tokenizer tokenizer;

    public ChunkService(ChunkDao chunkDao, DocumentDao documentDao, Tokenizer tokenizer) {
        this.chunkDao = chunkDao;
        this.documentDao = documentDao;
        this.tokenizer = tokenizer;
    }

    @Transactional(readOnly = true)
    public List<Chunk> findByDocument(Long documentId) {
        return chunkDao.findByDocument(documentId);
    }

    @Transactional(readOnly = true)
    public Chunk get(Long id) {
        return chunkDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Чанк " + id + " не найден"));
    }

    public Chunk create(Long documentId, Chunk chunk) {
        Document document = documentDao.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Документ " + documentId + " не найден"));
        if (chunk.getChunkIndex() == null) {
            chunk.setChunkIndex(chunkDao.nextIndex(documentId));
        }
        chunk.setId(null);
        chunk.setDocument(document);
        chunk.setTokenCount(tokenizer.countTokens(chunk.getContent()));
        chunkDao.save(chunk);
        return chunk;
    }

    public Chunk update(Long id, Chunk changed) {
        Chunk chunk = get(id);
        if (changed.getChunkIndex() != null) {
            chunk.setChunkIndex(changed.getChunkIndex());
        }
        chunk.setContent(changed.getContent());
        chunk.setTokenCount(tokenizer.countTokens(changed.getContent()));
        return chunk;
    }

    /** Удаляет чанк и возвращает id его документа (чтобы контроллер знал, куда вернуться). */
    public Long delete(Long id) {
        Chunk chunk = get(id);
        Long documentId = chunk.getDocument().getId();
        chunkDao.delete(chunk);
        return documentId;
    }

    /**
     * Простейший retrieval (буква R в RAG): оценка чанка = сколько разных слов запроса в нём встречается. Возвращаем topK лучших.
     */
    @Transactional(readOnly = true)
    public List<SearchHit> search(String query, int topK) {
        Set<String> queryWords = tokenizer.words(query);
        List<SearchHit> hits = new ArrayList<>();
        if (queryWords.isEmpty()) {
            return hits;
        }
        for (Chunk chunk : chunkDao.findAll()) {
            Set<String> chunkWords = tokenizer.words(chunk.getContent());
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
}
