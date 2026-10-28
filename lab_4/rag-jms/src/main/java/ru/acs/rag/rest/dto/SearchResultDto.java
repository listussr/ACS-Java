package ru.acs.rag.rest.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "searchResult")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"query", "queryTokens", "contextTokens", "hits"})
@JsonbPropertyOrder({"query", "topK", "href", "queryTokens", "contextTokens", "hits", "apiRoot"})
public class SearchResultDto {

    @XmlAttribute
    private String apiRoot;

    @XmlAttribute
    private int topK;

    @XmlAttribute
    private String href;

    private String query;
    private int queryTokens;
    private int contextTokens;

    @XmlElement(name = "hit")
    @JsonbProperty("hits")
    private List<Hit> hits = new ArrayList<>();

    @XmlAccessorType(XmlAccessType.FIELD)
    @JsonbPropertyOrder({"score", "chunk"})
    public static class Hit {
        @XmlAttribute
        private int score;
        private ChunkDto chunk;

        public Hit() {
        }

        public Hit(int score, ChunkDto chunk) {
            this.score = score;
            this.chunk = chunk;
        }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public ChunkDto getChunk() { return chunk; }
        public void setChunk(ChunkDto chunk) { this.chunk = chunk; }
    }

    public SearchResultDto() {
    }

    public String getApiRoot() { return apiRoot; }
    public void setApiRoot(String apiRoot) { this.apiRoot = apiRoot; }

    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public int getQueryTokens() { return queryTokens; }
    public void setQueryTokens(int queryTokens) { this.queryTokens = queryTokens; }

    public int getContextTokens() { return contextTokens; }
    public void setContextTokens(int contextTokens) { this.contextTokens = contextTokens; }

    public List<Hit> getHits() { return hits; }
    public void setHits(List<Hit> hits) { this.hits = hits; }
}
