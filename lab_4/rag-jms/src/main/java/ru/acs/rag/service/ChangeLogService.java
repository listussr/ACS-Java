package ru.acs.rag.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ru.acs.rag.entity.ChangeLog;

import java.util.List;

@Stateless
public class ChangeLogService {

    @PersistenceContext(unitName = "ragPU")
    private EntityManager em;

    public List<ChangeLog> find(String entityClass, Long entityId, int limit) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM ChangeLog c WHERE 1 = 1");
        if (entityClass != null && !entityClass.isBlank()) {
            jpql.append(" AND c.entityClass = :cls");
        }
        if (entityId != null) {
            jpql.append(" AND c.entityId = :eid");
        }
        jpql.append(" ORDER BY c.id DESC");
        TypedQuery<ChangeLog> q = em.createQuery(jpql.toString(), ChangeLog.class).setMaxResults(limit);
        if (entityClass != null && !entityClass.isBlank()) {
            q.setParameter("cls", entityClass);
        }
        if (entityId != null) {
            q.setParameter("eid", entityId);
        }
        return q.getResultList();
    }
}
