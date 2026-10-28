package ru.acs.rag.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Модульные тесты бизнес-логики токенизатора (без Spring и без БД). Запускаются при mvn package. */
class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void countsWordsNumbersAndPunctuation() {
        // RAG | - | система | , | 2024 | .
        assertEquals(6, tokenizer.countTokens("RAG-система, 2024."));
        assertEquals(0, tokenizer.countTokens("   "));
    }

    @Test
    void wordsAreLowercasedWithoutPunctuation() {
        assertEquals(Set.of("чанки", "и", "токены"), tokenizer.words("Чанки и ТОКЕНЫ!"));
    }

    @Test
    void splitsWithOverlap() {
        // 7 токенов, размер 3, перекрытие 1 -> шаг 2: [a b c] [c d e] [e f g]
        List<String> chunks = tokenizer.split("a b c d e f g", 3, 1);
        assertEquals(List.of("a b c", "c d e", "e f g"), chunks);
    }

    @Test
    void rejectsBadParameters() {
        assertThrows(IllegalArgumentException.class, () -> tokenizer.split("a b", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> tokenizer.split("a b", 3, 3));
    }
}
