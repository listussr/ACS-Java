package ru.acs.rag.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.acs.rag.entity.Document;
import ru.acs.rag.service.DocumentService;

/**
 * Контроллер Spring MVC для документов.
 */
@Controller
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/documents";
    }

    /** GET /documents — список. Данные кладём в Model, шаблон их выводит. */
    @GetMapping("/documents")
    public String list(Model model) {
        model.addAttribute("documents", documentService.findAll());
        return "documents/list"; // -> templates/documents/list.html
    }

    /** GET /documents/new — пустая форма. */
    @GetMapping("/documents/new")
    public String createForm(Model model) {
        model.addAttribute("document", new Document());
        return "documents/form";
    }

    /**
     * POST /documents — создание.
     */
    @PostMapping("/documents")
    public String create(@Valid @ModelAttribute("document") Document document, BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "documents/form";
        }
        documentService.create(document);
        redirect.addFlashAttribute("message", "Документ создан. Не забудьте разбить его на чанки.");
        return "redirect:/documents";
    }

    @GetMapping("/documents/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("document", documentService.get(id));
        return "documents/form";
    }

    @PostMapping("/documents/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("document") Document document,
                         BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            document.setId(id);
            return "documents/form";
        }
        documentService.update(id, document);
        redirect.addFlashAttribute("message", "Документ сохранён. Если текст менялся — пересоберите чанки.");
        return "redirect:/documents";
    }

    @PostMapping("/documents/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        documentService.delete(id);
        redirect.addFlashAttribute("message", "Документ удалён");
        return "redirect:/documents";
    }
}
