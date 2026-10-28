package ru.acs.rag.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.acs.rag.entity.Document;

import java.util.List;
import java.util.Optional;


@Repository
public class DocumentDao {

    private final SessionFactory sessionFactory;

    public DocumentDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Текущая сессия Hibernate, привязанная к транзакции Spring.
     */
    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    public List<Document> findAll() {
        return session().createQuery("from Document d order by d.id", Document.class).list();
    }

    public Optional<Document> findById(Long id) {
        return Optional.ofNullable(session().get(Document.class, id));
    }

    public void save(Document document) {
        session().persist(document);
    }

    public void delete(Document document) {
        session().remove(document);
    }

    public void flush() {
        session().flush();
    }
}
