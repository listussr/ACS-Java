\encoding UTF8

INSERT INTO documents (id, title, source, language, content, token_count) VALUES
  (1, 'Что такое RAG', 'https://ru.wikipedia.org/wiki/Retrieval-augmented_generation', 'ru',
   'RAG (Retrieval-Augmented Generation) — это подход, при котором языковая модель отвечает на вопрос не только по своим внутренним знаниям, но и по найденным внешним документам. Сначала система ищет в базе знаний фрагменты текста, похожие на запрос пользователя. Затем эти фрагменты добавляются в промпт как контекст, и модель генерирует ответ с опорой на них. Такой подход уменьшает число галлюцинаций и позволяет обновлять знания без переобучения модели. Качество ответа сильно зависит от того, насколько удачно документы разбиты на фрагменты и насколько хорошо работает поиск.',
   96);
INSERT INTO documents (id, title, source, language, content, token_count) VALUES
  (2, 'Токенизация текста', 'Лекция по NLP, тема 2', 'ru',
   'Токенизация — это разбиение текста на минимальные единицы, с которыми работает модель. Токеном может быть слово, часть слова или отдельный символ. Современные LLM используют субсловные алгоритмы, например BPE или WordPiece: частые слова становятся одним токеном, а редкие делятся на несколько частей. Контекстное окно модели измеряется именно в токенах, поэтому длину документов и чанков удобно считать в токенах, а не в символах. Для русского текста токенов обычно получается больше, чем для английского текста той же длины.',
   88);
INSERT INTO documents (id, title, source, language, content, token_count) VALUES
  (3, 'Chunking strategies', 'internal wiki', 'en',
   'Chunking splits a long document into smaller pieces that fit into the context window. Fixed-size chunking cuts the text every N tokens. An overlap of several tokens between neighbouring chunks keeps sentences that cross a boundary available in both pieces. Smaller chunks give more precise retrieval, while larger chunks preserve more context. A typical starting point is 200 to 500 tokens with 10 to 20 percent overlap.',
   75);

INSERT INTO chunks (document_id, chunk_index, content, token_count) VALUES
  (1, 0, 'RAG (Retrieval-Augmented Generation) — это подход, при котором языковая модель отвечает на вопрос не только по своим внутренним знаниям, но и по найденным внешним документам. Сначала система ищет в базе знаний фрагменты текста', 40),
  (1, 1, 'документам. Сначала система ищет в базе знаний фрагменты текста, похожие на запрос пользователя. Затем эти фрагменты добавляются в промпт как контекст, и модель генерирует ответ с опорой на них. Такой подход уменьшает число галлюцинаций и', 40),
  (1, 2, 'опорой на них. Такой подход уменьшает число галлюцинаций и позволяет обновлять знания без переобучения модели. Качество ответа сильно зависит от того, насколько удачно документы разбиты на фрагменты и насколько хорошо работает поиск.', 36),
  (2, 0, 'Токенизация — это разбиение текста на минимальные единицы, с которыми работает модель. Токеном может быть слово, часть слова или отдельный символ. Современные LLM используют субсловные алгоритмы, например BPE или WordPiece: частые слова становятся одним', 40),
  (2, 1, ', например BPE или WordPiece: частые слова становятся одним токеном, а редкие делятся на несколько частей. Контекстное окно модели измеряется именно в токенах, поэтому длину документов и чанков удобно считать в токенах, а не в', 40),
  (2, 2, 'и чанков удобно считать в токенах, а не в символах. Для русского текста токенов обычно получается больше, чем для английского текста той же длины.', 28),
  (3, 0, 'Chunking splits a long document into smaller pieces that fit into the context window. Fixed-size chunking cuts the text every N tokens. An overlap of several tokens between neighbouring chunks keeps sentences that cross a boundary', 40),
  (3, 1, 'tokens between neighbouring chunks keeps sentences that cross a boundary available in both pieces. Smaller chunks give more precise retrieval, while larger chunks preserve more context. A typical starting point is 200 to 500 tokens with 10', 40),
  (3, 2, 'typical starting point is 200 to 500 tokens with 10 to 20 percent overlap.', 15);

-- id документам заданы явно, поэтому сдвигаем счётчик BIGSERIAL
SELECT setval('documents_id_seq', (SELECT max(id) FROM documents));

