package ru.acs.rag.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.service.ChunkService;
import ru.acs.rag.service.DocumentService;

import java.util.List;

/** Контроллер для чанков документа и их автоматической нарезки. */
@Controller
public class ChunkController {

    private final DocumentService documentService;
    private final ChunkService chunkService;

    public ChunkController(DocumentService documentService, ChunkService chunkService) {
        this.documentService = documentService;
        this.chunkService = chunkService;
    }

    /** GET /documents/1/chunks — чанки документа + форма нарезки. */
    @GetMapping("/documents/{docId}/chunks")
    public String list(@PathVariable Long docId,
                       @RequestParam(defaultValue = "40") int chunkSize,
                       @RequestParam(defaultValue = "10") int overlap,
                       Model model) {
        List<Chunk> chunks = chunkService.findByDocument(docId);
        model.addAttribute("document", documentService.get(docId));
        model.addAttribute("chunks", chunks);
        model.addAttribute("totalTokens", chunks.stream().mapToInt(Chunk::getTokenCount).sum());
        model.addAttribute("chunkSize", chunkSize);
        model.addAttribute("overlap", overlap);
        return "chunks/list";
    }

    @PostMapping("/documents/{docId}/rechunk")
    public String rechunk(@PathVariable Long docId, @RequestParam int chunkSize, @RequestParam int overlap,
                          RedirectAttributes redirect) {
        try {
            int n = documentService.rechunk(docId, chunkSize, overlap);
            redirect.addFlashAttribute("message", "Документ разбит на " + n + " чанк(ов)");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        redirect.addAttribute("chunkSize", chunkSize);
        redirect.addAttribute("overlap", overlap);
        return "redirect:/documents/" + docId + "/chunks";
    }

    @GetMapping("/documents/{docId}/chunks/new")
    public String createForm(@PathVariable Long docId, Model model) {
        model.addAttribute("document", documentService.get(docId));
        model.addAttribute("chunk", new Chunk());
        return "chunks/form";
    }

    @PostMapping("/documents/{docId}/chunks")
    public String create(@PathVariable Long docId, @Valid @ModelAttribute("chunk") Chunk chunk,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("document", documentService.get(docId));
            return "chunks/form";
        }
        chunkService.create(docId, chunk);
        redirect.addFlashAttribute("message", "Чанк добавлен");
        return "redirect:/documents/" + docId + "/chunks";
    }

    @GetMapping("/chunks/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Chunk chunk = chunkService.get(id);
        model.addAttribute("chunk", chunk);
        model.addAttribute("document", chunk.getDocument());
        return "chunks/form";
    }

    @PostMapping("/chunks/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("chunk") Chunk chunk,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        Chunk existing = chunkService.get(id);
        if (errors.hasErrors()) {
            chunk.setId(id);
            model.addAttribute("document", existing.getDocument());
            return "chunks/form";
        }
        chunkService.update(id, chunk);
        redirect.addFlashAttribute("message", "Чанк сохранён");
        return "redirect:/documents/" + existing.getDocument().getId() + "/chunks";
    }

    @PostMapping("/chunks/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Long docId = chunkService.delete(id);
        redirect.addFlashAttribute("message", "Чанк удалён");
        return "redirect:/documents/" + docId + "/chunks";
    }
}
