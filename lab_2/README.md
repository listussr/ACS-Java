# ЛР2. Приложение на Spring

То же самое, что в ЛР1 (документы, чанки, токены, поиск), только на Spring. База та же, `rag_lab`,
скрипты в `db/` скопировал из первой лабы без изменений.


Стек:
- Spring Boot 3.5.16 (Spring Framework 6.2), Tomcat встроен прямо в приложение;
- Hibernate 6, причём через его собственный API: `SessionFactory`, `Session`, HQL;
- Spring MVC и шаблоны Thymeleaf;
- Maven, на выходе исполняемый `rag-spring.jar`;
- JUnit 5 для тестов токенизатора.

Порт 8081, чтобы не мешать GlassFish из первой лабы на 8080.

## Данные и Hibernate

Сущности `Document` и `Chunk` почти такие же, как в ЛР1. Добавил две собственные:
- `@CreationTimestamp` - дату создания проставляет сам Hibernate;
- `@Formula` для количества чанков у документа. Столбца такого нет, Hibernate подставляет подзапрос
  `select count(*) ...` прямо в SELECT (видно в логе).

По заданию слой данных надо было подготовить под Hibernate. Поэтому в `pom.xml` подключены `hibernate-core` и `spring-orm` напрямую, а автонастройку JPA в Spring Boot выключил (`exclude = HibernateJpaAutoConfiguration.class`).

Настройка Hibernate в двух местах:
- `config/HibernateConfig.java` создаёт `SessionFactory` (через `LocalSessionFactoryBean`) и
  менеджер транзакций `HibernateTransactionManager`. Соединения идут из пула HikariCP, его Spring
  Boot поднимает сам по `spring.datasource.*` из `application.properties`;
- `hibernate.cfg.xml` - классический конфиг Hibernate: список сущностей, `format_sql` и
  `hbm2ddl.auto = validate`. На старте Hibernate сверяет классы со схемой в базе и падает, если
  что-то не сходится. Таблицы он не создаёт, это делает SQL-скрипт.

С базой работают DAO-классы (`DocumentDao`, `ChunkDao`, помечены `@Repository`). Они берут сессию
через `sessionFactory.getCurrentSession()` и пишут запросы на HQL, например
`from Chunk c where c.document.id = :docId order by c.chunkIndex`. HQL - это SQL, только по классам
и полям вместо таблиц и столбцов. Текущая сессия есть только внутри транзакции, поэтому дёрнуть DAO
в обход сервисов не получится.

`Session` в Hibernate работает как unit of work. Объект, загруженный в сессии, отслеживается, и при коммите изменения уходят в базу сами. Поэтому в
`update()` нет никакого явного «сохранить», я просто меняю поля.

## Логика

`DocumentService` и `ChunkService` помечены `@Service` и `@Transactional`. Spring оборачивает их в
прокси: перед методом открывает транзакцию, после делает commit, при исключении rollback. На методах
чтения стоит `readOnly = true`.


Правила те же, что в ЛР1: токены считает сервер, нарезка с перекрытием в одной транзакции, новый
чанк без номера встаёт в конец, поиск по совпадению слов. Если объект не найден, кидается
`NotFoundException` с `@ResponseStatus(404)`, и Spring отдаёт 404.

## Интерфейс на Spring MVC

Контроллеры: `DocumentController`, `ChunkController`, `SearchController`. Методы привязаны к адресам
через `@GetMapping` / `@PostMapping`, параметры через `@PathVariable` и `@RequestParam`. Данные формы Spring сам собирает в объект
(`@ModelAttribute`) и проверяет (`@Valid`). Если есть ошибки, форма возвращается с сообщениями.
После успешного сохранения делается redirect с flash-сообщением, чтобы F5 не отправил форму ещё раз.

Основные адреса:
- `/documents` - список;
- `/documents/new`, `/documents/{id}/edit` - формы документа;
- `/documents/{id}/delete` - удаление (POST);
- `/documents/{id}/chunks` - чанки документа;
- `/documents/{id}/rechunk` - нарезка (POST);
- `/chunks/{id}/edit` - форма чанка;
- `/search?q=...` - поиск.

Удаление сделано POST-формами, а не ссылками: GET-запросы не должны менять данные.

Шаблоны в `src/main/resources/templates`. Общая шапка вынесена в `fragments.html` и подключается
через `th:replace`.

## Запуск

Нужен PostgreSQL с базой `rag_lab` (см. ЛР1). Дальше:

```
run.cmd
```

Скрипт собирает проект вместе с тестами и запускает jar. Java берётся из `JAVA_HOME`, потому что в
PATH у меня до сих пор восьмая. Вручную то же самое:

```
cd rag-spring
mvn package
java -jar target\rag-spring.jar
```

Открывать http://localhost:8081/, останавливать Ctrl+C. Отдельный сервер не нужен: Tomcat лежит
внутри jar, поэтому он и весит около 50 МБ.

## Что проверял

- на старте Hibernate проходит `validate`, то есть схема совпадает с классами;
- создание, правка, удаление документов и чанков, нарезка, поиск (результаты те же, что в ЛР1);
- ошибки валидации на формах;
- неверные параметры нарезки - сообщение, чанки не тронуты;
- несуществующий id даёт 404;
- `mvn package` гоняет 4 теста токенизатора.

