package ru.acs.rag.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import ru.acs.rag.entity.ChangeLog;
import ru.acs.rag.service.ChangeLogService;

import java.util.List;

@Named
@RequestScoped
public class ChangeLogBean {

    @EJB
    private ChangeLogService changeLogService;

    private String entity;
    private List<ChangeLog> entries;

    public List<ChangeLog> getEntries() {
        if (entries == null) {
            entries = changeLogService.find(entity, null, 100);
        }
        return entries;
    }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
}
