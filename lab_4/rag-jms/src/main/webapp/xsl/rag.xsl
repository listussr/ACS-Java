<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSL-преобразование XML-ответов REST API в HTML.
  Браузер скачивает XML, видит в начале <?xml-stylesheet href=".../xsl/rag.xsl"?>,
  скачивает этот файл и применяет его к XML: получается обычная HTML-страница.

  Структура: главный шаблон match="/" рисует каркас (шапка, меню, подвал),
  а содержимое рисует шаблон, подходящий под корневой элемент:
  documents | document | chunks | chunk | searchResult | error.
  Все ссылки берутся из атрибутов href/apiRoot, которые кладёт в XML сервер.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" encoding="UTF-8" indent="yes" doctype-system="about:legacy-compat"/>

    <!-- ============ Каркас страницы ============ -->
    <xsl:template match="/">
        <xsl:variable name="api" select="*/@apiRoot"/>
        <html lang="ru">
            <head>
                <meta charset="UTF-8"/>
                <title><xsl:apply-templates select="*" mode="title"/> — RAG REST API</title>
                <style>
                    body { font-family: "Segoe UI", Arial, sans-serif; margin: 0; background: #f5f6f8; color: #222; }
                    header { background: #2d3e50; color: #fff; padding: 10px 24px; display: flex; align-items: center; gap: 28px; flex-wrap: wrap; }
                    header h1 { margin: 0; font-size: 20px; }
                    header small { font-weight: normal; color: #ffc86b; font-size: 14px; }
                    header a { color: #cfe3ff; text-decoration: none; }
                    header form { display: flex; gap: 6px; }
                    main { padding: 16px 24px; max-width: 1200px; }
                    table.grid { border-collapse: collapse; width: 100%; background: #fff; }
                    table.grid th, table.grid td { border: 1px solid #d8dde3; padding: 6px 8px; vertical-align: top; text-align: left; }
                    table.grid th { background: #e9edf2; }
                    dl.fields { display: grid; grid-template-columns: max-content 1fr; gap: 4px 16px; background: #fff; padding: 12px; border: 1px solid #d8dde3; }
                    dl.fields dt { font-weight: 600; color: #555; }
                    dl.fields dd { margin: 0; }
                    .content { white-space: pre-wrap; background: #fff; border: 1px solid #d8dde3; padding: 12px; }
                    .small-text { font-size: 13px; white-space: pre-wrap; }
                    .links a { margin-right: 16px; }
                    .error { background: #fbe6e8; border-left: 4px solid #b00020; padding: 10px; }
                    footer { color: #777; font-size: 12px; padding: 16px 24px; }
                    code { background: #eceff3; padding: 1px 4px; }
                    .type-INSERT { color: #1e7b34; font-weight: 600; }
                    .type-UPDATE { color: #1f5fa8; font-weight: 600; }
                    .type-DELETE { color: #b00020; font-weight: 600; }
                    .small-link { font-size: 12px; }
                </style>
            </head>
            <body>
                <header>
                    <h1>RAG Knowledge Base <small>REST API</small></h1>
                    <a href="{$api}/documents">Документы</a>
                    <a href="{$api}/changes">Журнал изменений</a>
                    <!-- Обычная HTML-форма методом GET: браузер откроет /api/search?q=...&topK=... и снова получит XML -->
                    <form method="get" action="{$api}/search">
                        <input type="text" name="q" size="32" placeholder="Вопрос для поиска контекста"/>
                        <input type="number" name="topK" value="3" min="1" style="width:3em"/>
                        <button type="submit">Найти</button>
                    </form>
                </header>
                <main>
                    <xsl:apply-templates select="*"/>
                </main>
                <footer>
                    Это XML-ответ REST API, который браузер превратил в HTML с помощью XSLT
                    (<code>/xsl/rag.xsl</code>). Тот же адрес с заголовком
                    <code>Accept: application/json</code> вернёт JSON.
                </footer>
            </body>
        </html>
    </xsl:template>

    <!-- ============ Заголовки вкладки ============ -->
    <xsl:template match="documents" mode="title">Документы</xsl:template>
    <xsl:template match="document" mode="title"><xsl:value-of select="title"/></xsl:template>
    <xsl:template match="chunks" mode="title">Чанки: <xsl:value-of select="documentTitle"/></xsl:template>
    <xsl:template match="chunk" mode="title">Чанк №<xsl:value-of select="@index"/></xsl:template>
    <xsl:template match="searchResult" mode="title">Поиск: <xsl:value-of select="query"/></xsl:template>
    <xsl:template match="changes" mode="title">Журнал изменений</xsl:template>
    <xsl:template match="error" mode="title">Ошибка <xsl:value-of select="@status"/></xsl:template>

    <!-- ============ Список документов: GET /api/documents ============ -->
    <xsl:template match="documents">
        <h2>Документы базы знаний (<xsl:value-of select="@count"/>)</h2>
        <table class="grid">
            <tr>
                <th>ID</th><th>Название</th><th>Источник</th><th>Язык</th>
                <th>Токенов</th><th>Чанков</th><th>Создан</th>
            </tr>
            <xsl:for-each select="document">
                <tr>
                    <td><xsl:value-of select="@id"/></td>
                    <td><a href="{@href}"><xsl:value-of select="title"/></a></td>
                    <td><xsl:call-template name="source"/></td>
                    <td><xsl:value-of select="language"/></td>
                    <td><xsl:value-of select="tokenCount"/></td>
                    <td><a href="{chunksHref}"><xsl:value-of select="chunkCount"/></a></td>
                    <td><xsl:value-of select="translate(createdAt, 'T', ' ')"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ============ Один документ: GET /api/documents/{id} ============ -->
    <xsl:template match="document">
        <h2><xsl:value-of select="title"/></h2>
        <p class="links">
            <a href="{@apiRoot}/documents">← Все документы</a>
            <a href="{chunksHref}">Чанки с текстом (<xsl:value-of select="chunkCount"/>)</a>
            <a href="{@apiRoot}/changes?entity=Document&amp;entityId={@id}">История изменений</a>
        </p>
        <dl class="fields">
            <dt>ID</dt><dd><xsl:value-of select="@id"/></dd>
            <dt>Источник</dt><dd><xsl:call-template name="source"/></dd>
            <dt>Язык</dt><dd><xsl:value-of select="language"/></dd>
            <dt>Токенов</dt><dd><xsl:value-of select="tokenCount"/></dd>
            <dt>Создан</dt><dd><xsl:value-of select="translate(createdAt, 'T', ' ')"/></dd>
        </dl>
        <h3>Текст</h3>
        <div class="content"><xsl:value-of select="content"/></div>

        <h3>Чанки</h3>
        <xsl:choose>
            <xsl:when test="chunks/chunk">
                <table class="grid">
                    <tr><th>№</th><th>Токенов</th><th>Ссылка</th></tr>
                    <xsl:for-each select="chunks/chunk">
                        <tr>
                            <td><xsl:value-of select="@index"/></td>
                            <td><xsl:value-of select="tokenCount"/></td>
                            <td><a href="{@href}">Чанк №<xsl:value-of select="@index"/></a></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </xsl:when>
            <xsl:otherwise><p>Чанков нет.</p></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ============ Чанки документа: GET /api/documents/{id}/chunks ============ -->
    <xsl:template match="chunks">
        <h2>Чанки документа «<a href="{documentHref}"><xsl:value-of select="documentTitle"/></a>»</h2>
        <p class="links">
            <a href="{documentHref}">← К документу</a>
            <a href="{@apiRoot}/documents">Все документы</a>
        </p>
        <p>Чанков: <xsl:value-of select="@count"/>, токенов во всех чанках: <xsl:value-of select="totalTokens"/></p>
        <xsl:if test="chunk">
            <table class="grid">
                <tr><th>№</th><th>Текст</th><th>Токенов</th><th></th></tr>
                <xsl:for-each select="chunk">
                    <tr>
                        <td><xsl:value-of select="@index"/></td>
                        <td class="small-text"><xsl:value-of select="content"/></td>
                        <td><xsl:value-of select="tokenCount"/></td>
                        <td><a href="{@href}">Открыть</a></td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:if>
    </xsl:template>

    <!-- ============ Один чанк: GET /api/chunks/{id} ============ -->
    <xsl:template match="chunk">
        <h2>Чанк №<xsl:value-of select="@index"/> документа «<a href="{documentHref}"><xsl:value-of select="documentTitle"/></a>»</h2>
        <p class="links">
            <a href="{documentHref}">← К документу</a>
            <a href="{documentHref}/chunks">Все чанки документа</a>
        </p>
        <dl class="fields">
            <dt>ID</dt><dd><xsl:value-of select="@id"/></dd>
            <dt>Номер в документе</dt><dd><xsl:value-of select="@index"/></dd>
            <dt>Токенов</dt><dd><xsl:value-of select="tokenCount"/></dd>
        </dl>
        <h3>Текст</h3>
        <div class="content"><xsl:value-of select="content"/></div>
    </xsl:template>

    <!-- ============ Поиск: GET /api/search?q=... ============ -->
    <xsl:template match="searchResult">
        <h2>Поиск контекста: «<xsl:value-of select="query"/>»</h2>
        <p>
            Запрос: <xsl:value-of select="queryTokens"/> токенов.
            Найдено чанков: <xsl:value-of select="count(hit)"/> (Top-<xsl:value-of select="@topK"/>),
            контекст займёт <xsl:value-of select="contextTokens"/> токенов.
        </p>
        <xsl:choose>
            <xsl:when test="hit">
                <table class="grid">
                    <tr><th>Оценка</th><th>Документ</th><th>Чанк</th><th>Текст</th><th>Токенов</th></tr>
                    <xsl:for-each select="hit">
                        <tr>
                            <td><xsl:value-of select="@score"/></td>
                            <td><a href="{chunk/documentHref}"><xsl:value-of select="chunk/documentTitle"/></a></td>
                            <td><a href="{chunk/@href}">№<xsl:value-of select="chunk/@index"/></a></td>
                            <td class="small-text"><xsl:value-of select="chunk/content"/></td>
                            <td><xsl:value-of select="chunk/tokenCount"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </xsl:when>
            <xsl:otherwise><p>Ничего не найдено.</p></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ============ Журнал изменений: GET /api/changes (ЛР4) ============ -->
    <xsl:template match="changes">
        <h2>Журнал изменений (<xsl:value-of select="@count"/>)</h2>
        <p class="links">
            Фильтр:
            <a href="{@apiRoot}/changes">все</a>
            <a href="{@apiRoot}/changes?entity=Document">документы</a>
            <a href="{@apiRoot}/changes?entity=Chunk">чанки</a>
        </p>
        <table class="grid">
            <tr><th>№</th><th>Когда</th><th>Тип</th><th>Сущность</th><th>Новые значения полей (JSON)</th></tr>
            <xsl:for-each select="change">
                <tr>
                    <td><xsl:value-of select="@id"/></td>
                    <td><xsl:value-of select="translate(changedAt, 'T', ' ')"/></td>
                    <td class="type-{@type}"><xsl:value-of select="@type"/></td>
                    <td>
                        <a href="{entityHref}"><xsl:value-of select="entityClass"/>, id <xsl:value-of select="entityId"/></a>
                        <br/>
                        <a href="{/changes/@apiRoot}/changes?entity={entityClass}&amp;entityId={entityId}" class="small-link">история</a>
                    </td>
                    <td class="small-text"><xsl:value-of select="details"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ============ Ошибка ============ -->
    <xsl:template match="error">
        <div class="error">
            <h2>Ошибка <xsl:value-of select="@status"/></h2>
            <p><xsl:value-of select="message"/></p>
            <xsl:if test="detail">
                <ul>
                    <xsl:for-each select="detail"><li><xsl:value-of select="."/></li></xsl:for-each>
                </ul>
            </xsl:if>
        </div>
        <p><a href="{@apiRoot}/documents">← К списку документов</a></p>
    </xsl:template>

    <!-- Источник: если это URL — делаем ссылкой -->
    <xsl:template name="source">
        <xsl:choose>
            <xsl:when test="starts-with(source, 'http')">
                <a href="{source}"><xsl:value-of select="source"/></a>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="source"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
