package ru.acs.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.acs.rag.dao.DocumentDao;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.entity.Document;

import java.util.List;

/**
 * Бизнес-логика работы с документами.
 */
@Service
@Transactional
public class DocumentService {

    private final DocumentDao documentDao;
    private final Tokenizer tokenizer;

    public DocumentService(DocumentDao documentDao, Tokenizer tokenizer) {
        this.documentDao = documentDao;
        this.tokenizer = tokenizer;
    }

    @Transactional(readOnly = true)
    public List<Document> findAll() {
        return documentDao.findAll();
    }

    @Transactional(readOnly = true)
    public Document get(Long id) {
        return documentDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Документ " + id + " не найден"));
    }

    public Document create(Document document) {
        document.setId(null);
        document.setTokenCount(tokenizer.countTokens(document.getContent()));
        documentDao.save(document);
        return document;
    }

    public Document update(Long id, Document changed) {
        Document document = get(id);
        document.setTitle(changed.getTitle());
        document.setSource(changed.getSource());
        document.setLanguage(changed.getLanguage());
        document.setContent(changed.getContent());
        document.setTokenCount(tokenizer.countTokens(changed.getContent()));
        return document;
    }

    public void delete(Long id) {
        documentDao.delete(get(id));
    }

    /**
     * Пересобрать чанки документа: старые удаляются, текст режется заново.
     */
    public int rechunk(Long id, int chunkSize, int overlap) {
        Document document = get(id);
        List<String> parts = tokenizer.split(document.getContent(), chunkSize, overlap);

        document.getChunks().clear();
        documentDao.flush();

        for (int i = 0; i < parts.size(); i++) {
            Chunk chunk = new Chunk();
            chunk.setDocument(document);
            chunk.setChunkIndex(i);
            chunk.setContent(parts.get(i));
            chunk.setTokenCount(tokenizer.countTokens(parts.get(i)));
            document.getChunks().add(chunk);
        }
        return parts.size();
    }
}
