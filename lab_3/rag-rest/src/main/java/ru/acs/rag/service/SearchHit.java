package ru.acs.rag.service;

import ru.acs.rag.entity.Chunk;

import java.io.Serializable;

/** Результат поиска: найденный чанк и его оценка релевантности. */
public class SearchHit implements Serializable {

    private final Chunk chunk;
    private final int score;

    public SearchHit(Chunk chunk, int score) {
        this.chunk = chunk;
        this.score = score;
    }

    public Chunk getChunk() { return chunk; }

    public int getScore() { return score; }
}
