package ru.acs.rag.web;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import ru.acs.rag.entity.Document;
import ru.acs.rag.service.DocumentService;

import java.io.Serializable;

/**
 * Бин страницы document-edit.xhtml (создание и редактирование документа).
 */
@Named
@ViewScoped
public class DocumentEditBean implements Serializable {

    @EJB
    private DocumentService documentService;

    private Long id;
    private Document document;

    /** Вызывается перед показом страницы (f:viewAction). */
    public void load() {
        if (document != null) {
            return;
        }
        if (id == null) {
            document = new Document();
        } else {
            document = documentService.find(id);
            if (document == null) {
                Messages.info("Документ " + id + " не найден");
                document = new Document();
                id = null;
            }
        }
    }

    public String save() {
        try {
            if (document.getId() == null) {
                documentService.create(document);
                Messages.info("Документ создан. Не забудьте разбить его на чанки.");
            } else {
                documentService.update(document);
                Messages.info("Документ сохранён. Если текст менялся — пересоберите чанки.");
            }
            Messages.keepAfterRedirect();
            return "documents?faces-redirect=true";
        } catch (Exception e) {
            Messages.error(e);
            return null;
        }
    }

    public boolean isNew() {
        return document == null || document.getId() == null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
}
