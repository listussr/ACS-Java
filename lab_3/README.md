# ЛР3. REST веб-сервис

Адреса после деплоя:
- http://localhost:8080/rag-rest/api/documents - REST API (в браузере показывается как HTML через XSLT);
- http://localhost:8080/rag-rest/ - старые JSF-страницы, в меню добавил ссылку на API.

## JAX-RS или Spring REST

Выбрал JAX-RS. Причины:
1. Приложение из ЛР1 уже крутится на GlassFish, а там JAX-RS есть из коробки. REST
   просто вызывает готовые EJB.
2. Для XML и JSON хватает стандартов JAXB и JSON-B, они уже в сервере. В Spring ради XML пришлось бы подключать и настраивать отдельный модуль Jackson.
3. Тег со ссылкой на XSL (задание 5) делается стандартными средствами JAX-RS и JAXB.
4. На этом же приложении будет ЛР4 про JMS, а там нужны объекты на сервере приложений и MDB.


## API

Базовый адрес `http://localhost:8080/rag-rest/api`.

| Метод | Адрес | Что делает |
|---|---|---|
| GET | `/documents` | список документов |
| POST | `/documents` | создать документ, ответ 201 и заголовок `Location` |
| GET | `/documents/{id}` | документ с текстом и ссылками на чанки |
| PUT | `/documents/{id}` | изменить документ |
| DELETE | `/documents/{id}` | удалить вместе с чанками, ответ 204 |
| GET | `/documents/{id}/chunks` | чанки документа |
| POST | `/documents/{id}/chunks` | добавить чанк (без `index` - в конец) |
| POST | `/documents/{id}/rechunk?size=40&overlap=10` | нарезать заново |
| GET | `/chunks/{id}` | чанк |
| PUT | `/chunks/{id}` | изменить чанк |
| DELETE | `/chunks/{id}` | удалить чанк |
| GET | `/search?q=...&topK=3` | поиск |

`rechunk` - это действие, поэтому сделал его через POST на отдельный адрес. Поля, которые считает сервер (`id`, `tokenCount`, `chunkCount`, `createdAt`, ссылки), при вводе просто
игнорируются.


Пример документа в XML:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/rag-rest/xsl/rag.xsl"?>
<document apiRoot="http://localhost:8080/rag-rest/api" id="1" href="http://localhost:8080/rag-rest/api/documents/1">
    <title>Что такое RAG</title>
    <source>https://ru.wikipedia.org/wiki/Retrieval-augmented_generation</source>
    <language>ru</language>
    <tokenCount>96</tokenCount>
    <chunkCount>3</chunkCount>
    <createdAt>2026-09-24T16:48:16</createdAt>
    <content>RAG (Retrieval-Augmented Generation) - это подход...</content>
    <chunksHref>http://localhost:8080/rag-rest/api/documents/1/chunks</chunksHref>
    <chunks>
        <chunk id="1" index="0" href="http://localhost:8080/rag-rest/api/chunks/1">
            <tokenCount>40</tokenCount>
        </chunk>
        ...
    </chunks>
</document>
```

Тот же адрес с `Accept: application/json` отдаёт то же самое в JSON.

## XML и JSON

На ресурсах стоят `@Produces` и `@Consumes` сразу с двумя типами. Формат ответа JAX-RS выбирает по
заголовку `Accept`, формат тела запроса - по `Content-Type`. Если `Accept` не указан (`*/*`, как у
curl по умолчанию), отдаётся JSON. Можно прислать XML и получить JSON, это тоже работает.

Браузер шлёт `Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8`. HTML мы не
отдаём, поэтому выигрывает XML, у него вес 0.9 против 0.8 у `*/*`. В итоге браузер сам получает XML.

Наружу отдаюстя не сущности, а отдельные классы DTO (пакет `rest/dto`). Отдавать сущности напрямую неудобно: документ ссылается на чанки, чанк на
документ, и сериализация уходит в бесконечную рекурсию. Плюс формат API тогда намертво привязан к
таблицам. На каждом DTO висят аннотации сразу двух библиотек: JAXB (`@XmlRootElement`,
`@XmlAttribute`, `@XmlElement`) для XML и JSON-B (`@JsonbPropertyOrder`, `@JsonbProperty`) для JSON.


У XML должен быть один корневой элемент, поэтому списки обёрнуты (`<documents count="3">...`). В JSON
сделал так же, объект со счётчиком и массивом, чтобы форматы не расходились.

Ошибки тоже приходят в XML или JSON, этим занимаются `ExceptionMapper`-ы в `ErrorMappers`:
- нет объекта - 404;
- не прошла проверка полей (`@Valid` на DTO) - 400 со списком ошибок;
- неверные параметры нарезки - 400. Из EJB исключение приходит обёрнутым в `EJBException`, внутри
  лежит `IllegalArgumentException`, приходится разворачивать;
- битый JSON - тоже 400. Сначала тут было 500: Jersey заворачивает ошибку парсинга в
  `ProcessingException`, и ловить пришлось именно его;
- неподдерживаемый `Content-Type` - 415.

## XSL (задание 4)

Файл `src/main/webapp/xsl/rag.xsl` лежит в статической части приложения, адрес
http://localhost:8080/rag-rest/xsl/rag.xsl. В `web.xml` для `.xsl` пришлось прописать тип
`text/xsl`, иначе браузер не применяет преобразование.


## Тег xml-stylesheet (задание 5)

Стандартный XML-конвертер JAX-RS такой тег не пишет, поэтому написал свой, `XmlStylesheetWriter` (это `MessageBodyWriter` с `@Provider`). Он выключает у JAXB его собственную первую строку
(`JAXB_FRAGMENT`), сам пишет `<?xml version="1.0" encoding="UTF-8"?>`, за ней
`<?xml-stylesheet type="text/xsl" href=".../xsl/rag.xsl"?>` и только потом сам объект. Порядок важен: этот тег должен стоять после объявления XML и до корневого элемента, иначе браузер ругается.
Путь к XSL берётся из контекстного пути приложения, а не зашит в код. Работает для всех XML-ответов, включая ошибки.

## Запуск

Сервер и JDBC-ресурс общие с ЛР1.

```
..\lab_1\server\glassfish7\bin\asadmin.bat start-domain domain1
deploy\deploy.cmd
```

Примеры запросов (из папки `lab_3`, `curl.exe` в Windows есть). Тела запросов лежат в `examples/`:

```
curl.exe http://localhost:8080/rag-rest/api/documents
curl.exe -H "Accept: application/xml" http://localhost:8080/rag-rest/api/documents

curl.exe -i -X POST -H "Content-Type: application/json" --data-binary "@examples\document.json" http://localhost:8080/rag-rest/api/documents
curl.exe -i -X POST -H "Content-Type: application/xml" -H "Accept: application/xml" --data-binary "@examples\document.xml" http://localhost:8080/rag-rest/api/documents

curl.exe -X PUT -H "Content-Type: application/json" --data-binary "@examples\document.json" http://localhost:8080/rag-rest/api/documents/6
curl.exe -X POST "http://localhost:8080/rag-rest/api/documents/6/rechunk?size=15&overlap=5"
curl.exe -i -X POST -H "Content-Type: application/json" --data-binary "@examples\chunk.json" http://localhost:8080/rag-rest/api/documents/6/chunks
curl.exe -i -X DELETE http://localhost:8080/rag-rest/api/documents/6

curl.exe -i -X POST -H "Content-Type: application/json" --data-binary "@examples\invalid-document.json" http://localhost:8080/rag-rest/api/documents
curl.exe -i http://localhost:8080/rag-rest/api/documents/99999
```

## Что проверял

Проверял в основном отдельным скриптом, который гоняет запросы к API:
- создание, чтение, изменение в JSON: 201 и `Location`, токены считает сервер;
- создание документа и чанка в XML, в ответе XML с тегом xml-stylesheet;
- XML на входе и JSON на выходе;
- нарезка, добавление чанка без номера, удаление (204, потом 404);
- поиск даёт те же результаты, что в ЛР1 и ЛР2;
- ошибки: пустые поля, неверная нарезка, битый JSON и XML - 400, `text/plain` - 415;
- с `Accept` как у браузера приходит XML, `rag.xsl` отдаётся как `text/xsl`;
- в браузере прошёл список → документ → чанк → поиск → страница 404, всё рисуется через XSL.
