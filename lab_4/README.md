# ЛР4. JMS: журнал изменений и оповещения

Взял приложение из ЛР3 и добавил две вещи:
- журнал: каждое добавление, изменение и удаление документа или чанка пишется в таблицу
  `change_log`;
- письма: если документ получился больше порога в токенах, на заданные адреса уходит письмо.

Обе вещи работают через JMS. Брокер сообщений (OpenMQ) уже встроен в GlassFish, ставить отдельно
ничего не пришлось. Приложение при изменении данных только публикует сообщение в топик, а разбирают
его два MDB. MDB - это компонент, который никто не вызывает напрямую: сервер сам передаёт ему
сообщения, когда они приходят. Один MDB пишет журнал, другой решает, слать ли письмо.

Адреса:
- http://localhost:8080/rag-jms/changes.xhtml - журнал (JSF);
- http://localhost:8080/rag-jms/api/changes - журнал через REST (в браузере через XSLT);
- http://localhost:8025 - почтовый ящик Mailpit, туда падают все письма.

## Таблица журнала

Скрипт `db/03_change_log.sql`:

```sql
CREATE TABLE change_log (
    id           BIGSERIAL PRIMARY KEY,
    change_type  VARCHAR(10)  NOT NULL CHECK (change_type IN ('INSERT','UPDATE','DELETE')),
    entity_class VARCHAR(100) NOT NULL,
    entity_id    BIGINT       NOT NULL,
    details      TEXT         NOT NULL,
    changed_at   TIMESTAMP    NOT NULL,
    logged_at    TIMESTAMP    NOT NULL DEFAULT now(),
    message_id   VARCHAR(100) NOT NULL UNIQUE
);
```

В `details` лежат все поля сущности после изменения, для удаления - последние значения перед ним.
`message_id` уникальный, чтобы одно сообщение не записалось дважды, если брокер доставит его
повторно. По `changed_at` и `logged_at` видно, что журнал пишется
асинхронно: разница в миллисекунды.

На существующей базе таблица создаётся так (от владельца базы):

```
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U rag_user -h localhost -d rag_lab -f db\03_change_log.sql
```

`00_init.sql` при полной переинициализации тоже его вызывает.

## Объекты на сервере

Всё создаётся скриптом `deploy\setup-jms.cmd` через `asadmin`:
- физический топик `RagChangesTopic` в брокере OpenMQ - `create-jmsdest --desttype topic`;
- фабрика соединений `jms/ragConnectionFactory` - `create-jms-resource`;
- ресурс `jms/ragChanges`, через который код находит топик - `create-jms-resource --restype jakarta.jms.Topic`;
- почтовая сессия `mail/ragMail`, смотрит на Mailpit (127.0.0.1:1025) - `create-mail-resource`;
- две настройки для писем: порог `rag/notify/maxDocumentTokens` и адреса `rag/notify/recipients` -
  `create-custom-resource`.


Почему топик, а не очередь. Из очереди каждое сообщение забирает только один получатель, а у меня на одно событие должны отреагировать двое:
журнал и почта. С очередью сообщения поделились бы между двумя MDB, и в журнал попала бы примерно
половина изменений. В топике каждый подписчик получает свою копию.

Обычная подписка на топик теряет сообщения, пока подписчик отключён. Поэтому у обоих MDB подписка
durable (`subscriptionDurability = Durable` плюс `clientId` и `subscriptionName`): брокер копит для
них сообщения, пока приложение лежит, и отдаёт после запуска.

## Сообщение

### Когда отправляется

На `Document` и `Chunk` повешен JPA-слушатель `EntityChangeListener`
(`@EntityListeners`). JPA сама дёргает его методы `@PostPersist`, `@PostUpdate`, `@PostRemove` после каждой операции с таблицей, откуда бы ни пришло изменение.

Слушатель отдаёт сущность в `ChangePublisher`, тот отправляет сообщение через `JMSContext`, который
внедряет контейнер. Изменения идут внутри транзакции EJB, и отправка тоже попадает в эту транзакцию:
брокер выпускает сообщение только после commit. Откатилась транзакция - сообщения нет. 

### Тип и структура

TextMessage с JSON в теле:

```json
{
  "format":     "rag-change/1",
  "changeType": "UPDATE",
  "entity":     "Document",
  "entityId":   25,
  "changedAt":  "2026-09-29T18:51:33.412",
  "data": { "id": 25, "title": "...", "source": "ЛР4", "language": "ru", "tokenCount": 192, "content": "..." }
}
```

У чанка в `data` поля `id`, `documentId`, `chunkIndex`, `tokenCount`, `content`. `format` - версия
схемы сообщения на всякий случай ради приличия.

Формат описан в одном классе, `ChangeMessage`: там и сборка сообщения, и его проверка у получателя.

## MDB журнала

`ChangeLogMdb` подписан на топик. На каждое сообщение он:
1. Валидирует его (`ChangeMessage.parse`). Тип должен быть TextMessage, тело - JSON-объект с
   известным `format`. `changeType` - одно из INSERT/UPDATE/DELETE, `entity` - Document или Chunk,
   есть числовой `entityId`, дата и объект `data`, свойства совпадают с телом.
2. Проверяет, не записано ли уже сообщение с таким `JMSMessageID`.
3. Пишет строку в `change_log`.

Приём сообщения и запись в базу идут в одной транзакции. Если запись упала, брокер доставит
сообщение ещё раз, а после нескольких неудач переложит его в системную очередь `mq.sys.dmq`.

Заведомо битое сообщение MDB обратно не возвращает, иначе оно крутилось бы по кругу, а просто пишет
предупреждение в `server.log`.

Для проверки есть отдельная программка `test-client\TestSender.java`, она шлёт сообщения прямо в
брокер. `test-client\run-test-sender.cmd bad` отправляет три плохих сообщения: MapMessage, текст не
в JSON и JSON с неверным `changeType`. Оба MDB их отбросили, в логе было:

```
Отброшено некорректное сообщение: ожидался TextMessage, получен DirectMapPacket
Отброшено некорректное сообщение: тело не является JSON-объектом: Unexpected char ...
Отброшено некорректное сообщение: неверный changeType: RENAME
```

## Оповещения

Событие выбрал такое: документ создан или изменён, и в нём больше токенов, чем порог (по умолчанию
150). Такой документ не влезет в контекстное окно целиком, его надо резать на чанки. По заданию это
вариант «атрибут вышел за границы».

`NotificationMdb` фильтрует в два шага. Сначала селектор
`entity = 'Document' AND changeType IN ('INSERT', 'UPDATE')`, так что сообщения про чанки и удаления
брокер ему вообще не присылает. Потом в коде та же проверка формата и сравнение `tokenCount` с
порогом.

Письмо уходит через почтовую сессию сервера (`@Resource(lookup = "mail/ragMail")`). Выглядит так:

```
От:   rag-kb@localhost
Кому: kb-admin@rag.local, lecturer@rag.local
Тема: [RAG KB] Документ «Большой тест JMS» не помещается в контекст: 192 токенов (порог 150)

В базе знаний добавлен документ, размер которого превышает порог.
Документ:   Большой тест JMS (id 16)
Токенов:    192 (порог 150, превышение на 42)
...
```

Порог и адреса - JNDI-ресурсы сервера, MDB читает их на каждом сообщении.

```
asadmin set resources.custom-resource.rag/notify/maxDocumentTokens.property.value=100
asadmin set "resources.custom-resource.rag/notify/recipients.property.value=me@rag.local,boss@rag.local"
```


Mailpit - локальный тестовый почтовый сервер: принимает письма на 127.0.0.1:1025 и показывает их на
http://localhost:8025, наружу ничего не уходит. Скачан с https://github.com/axllent/mailpit/releases
(v1.31.3), лежит в `tools/mailpit`, в git не хранится. Чтобы слать на настоящую почту, достаточно
поменять сессию `mail/ragMail` на сервере, код трогать не нужно.


## Запуск

Один раз (пул `jdbc/ragDS` и драйвер уже настроены в ЛР1):

```
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U rag_user -h localhost -d rag_lab -f db\03_change_log.sql
..\lab_1\server\glassfish7\bin\asadmin.bat start-domain domain1
deploy\setup-jms.cmd
```

Потом каждый раз:

```
start-mailpit.cmd
deploy\deploy.cmd
```

Окно Mailpit не закрывать, пока он нужен.

Как показать работу:
1. На http://localhost:8080/rag-jms/ добавить документ с длинным текстом (больше 150 токенов).
2. В «Журнале изменений» появится INSERT, на http://localhost:8025 письмо.
3. «Чанки» > «Разбить на чанки заново»: в журнале INSERT на каждый чанк, писем нет.
4. Удалить документ: в журнале DELETE документа и всех чанков.
5. `test-client\run-test-sender.cmd bad`: в `server.log` предупреждения, журнал не меняется.
6. Durable-подписка: `asadmin disable rag-jms`, потом `test-client\run-test-sender.cmd valid`, потом
   `asadmin enable rag-jms`. После включения появятся запись про Document 999 и письмо.

## Что проверял

Основное гонял отдельным скриптом через REST API и API Mailpit:
- маленький документ: INSERT в журнале, письма нет;
- документ на 192 токена: INSERT и письмо на два адреса;
- правка документа: UPDATE с новыми значениями, у большого ещё и письмо;
- нарезка на 4 чанка: 4 INSERT для Chunk, писем нет;
- удаление документа с 4 чанками: 5 DELETE, писем нет;
- запрос с ошибкой (400): в журнале ничего нового;
- три плохих сообщения: оба MDB отбросили все три, журнал не изменился;
- сообщение, отправленное при выключенном приложении, дошло после включения;
- смена порога через `asadmin set` подействовала сразу;
- дублей в журнале нет.
