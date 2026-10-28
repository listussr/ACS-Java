package ru.acs.rag.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import ru.acs.rag.service.ChunkService;
import ru.acs.rag.service.SearchHit;
import ru.acs.rag.service.SimpleTokenizer;

import java.util.List;

/** Бин страницы search.xhtml: поиск чанков, которые RAG-система отправила бы в LLM как контекст. */
@Named
@RequestScoped
public class SearchBean {

    @EJB
    private ChunkService chunkService;

    private String query;
    private int topK = 3;
    private List<SearchHit> hits;

    public String search() {
        hits = chunkService.search(query, topK);
        return null;
    }

    public int getContextTokens() {
        return hits == null ? 0 : hits.stream().mapToInt(h -> h.getChunk().getTokenCount()).sum();
    }

    public int getQueryTokens() {
        return SimpleTokenizer.countTokens(query);
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }

    public List<SearchHit> getHits() { return hits; }
}
