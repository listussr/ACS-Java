package ru.acs.rag.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.acs.rag.entity.Chunk;

import java.util.List;
import java.util.Optional;

/** DAO для чанков. */
@Repository
public class ChunkDao {

    private final SessionFactory sessionFactory;

    public ChunkDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    public List<Chunk> findAll() {
        return session().createQuery("from Chunk c", Chunk.class).list();
    }

    public List<Chunk> findByDocument(Long documentId) {
        return session()
                .createQuery("from Chunk c where c.document.id = :docId order by c.chunkIndex", Chunk.class)
                .setParameter("docId", documentId)
                .list();
    }

    public Optional<Chunk> findById(Long id) {
        return Optional.ofNullable(session().get(Chunk.class, id));
    }

    /** Следующий свободный номер чанка в документе (max + 1, или 0 если чанков нет). */
    public int nextIndex(Long documentId) {
        Integer max = session()
                .createQuery("select max(c.chunkIndex) from Chunk c where c.document.id = :docId", Integer.class)
                .setParameter("docId", documentId)
                .uniqueResult();
        return max == null ? 0 : max + 1;
    }

    public void save(Chunk chunk) {
        session().persist(chunk);
    }

    public void delete(Chunk chunk) {
        session().remove(chunk);
    }
}
