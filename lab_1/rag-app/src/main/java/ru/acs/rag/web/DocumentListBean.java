package ru.acs.rag.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import ru.acs.rag.entity.Document;
import ru.acs.rag.service.DocumentService;

import java.util.List;

/**
 * Бин страницы documents.xhtml (список документов).
 */
@Named
@RequestScoped
public class DocumentListBean {

    @EJB
    private DocumentService documentService;

    private List<Document> documents;

    public List<Document> getDocuments() {
        if (documents == null) {
            documents = documentService.findAll();
        }
        return documents;
    }

    public String delete(Long id) {
        try {
            documentService.delete(id);
            Messages.info("Документ удалён");
        } catch (Exception e) {
            Messages.error(e);
        }
        documents = null;
        return null;
    }
}
