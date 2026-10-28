package ru.acs.rag.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.acs.rag.service.ChunkService;
import ru.acs.rag.service.SearchHit;
import ru.acs.rag.service.Tokenizer;

import java.util.List;

/** Поиск чанков, которые RAG-система отправила бы в LLM как контекст. */
@Controller
public class SearchController {

    private final ChunkService chunkService;
    private final Tokenizer tokenizer;

    public SearchController(ChunkService chunkService, Tokenizer tokenizer) {
        this.chunkService = chunkService;
        this.tokenizer = tokenizer;
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "3") int topK,
                         Model model) {
        model.addAttribute("q", q);
        model.addAttribute("topK", topK);
        if (q != null && !q.isBlank()) {
            List<SearchHit> hits = chunkService.search(q, topK);
            model.addAttribute("hits", hits);
            model.addAttribute("queryTokens", tokenizer.countTokens(q));
            model.addAttribute("contextTokens", hits.stream().mapToInt(h -> h.getChunk().getTokenCount()).sum());
        }
        return "search";
    }
}
