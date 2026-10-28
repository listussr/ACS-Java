package ru.acs.rag.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Упрощённый токенизатор (та же логика, что в ЛР1).
 * Токен = непрерывная последовательность букв/цифр ИЛИ один отдельный знак.
 */
@Component
public class Tokenizer {

    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}]+|[^\\s\\p{L}\\p{N}]");

    /** Токен вместе с позицией в исходном тексте. */
    public record Token(String text, int start, int end) {
        boolean isWord() {
            return Character.isLetterOrDigit(text.charAt(0));
        }
    }

    public List<Token> tokenize(String text) {
        List<Token> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        Matcher m = TOKEN.matcher(text);
        while (m.find()) {
            result.add(new Token(m.group(), m.start(), m.end()));
        }
        return result;
    }

    public int countTokens(String text) {
        return tokenize(text).size();
    }

    /** Множество слов в нижнем регистре без знаков препинания. */
    public Set<String> words(String text) {
        Set<String> words = new LinkedHashSet<>();
        for (Token t : tokenize(text)) {
            if (t.isWord()) {
                words.add(t.text().toLowerCase(Locale.ROOT));
            }
        }
        return words;
    }

    /**
     * Нарезка на чанки фиксированного размера с перекрытием.
     */
    public List<String> split(String text, int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Размер чанка должен быть больше 0");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("Перекрытие должно быть от 0 до (размер чанка - 1)");
        }
        List<Token> tokens = tokenize(text);
        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        for (int i = 0; i < tokens.size(); i += step) {
            int j = Math.min(i + chunkSize, tokens.size());
            chunks.add(text.substring(tokens.get(i).start(), tokens.get(j - 1).end()));
            if (j == tokens.size()) {
                break;
            }
        }
        return chunks;
    }
}
