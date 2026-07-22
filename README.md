# Task Tracker - Distributed Task Management System

Распределённая система управления задачами, состоящая из двух независимых микросервисов на Java / Spring Boot. Проект выполнен в рамках учебной практики (Java Backend).

## Содержание

- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Структура репозитория](#структура-репозитория)
- [Как запустить проект](#как-запустить-проект)
  - [Вариант 1 - Docker Compose (рекомендуется)](#вариант-1--docker-compose-рекомендуется)
  - [Вариант 2 - локальный запуск через Maven](#вариант-2--локальный-запуск-через-maven)
- [API-документация (Swagger)](#api-документация-swagger)
- [Примеры запросов](#примеры-запросов)
- [Тестирование](#тестирование)
- [Документация и диаграммы](#документация-и-диаграммы)
- [Git-флоу проекта](#git-флоу-проекта)
- [Автор](#автор)

---

## Архитектура

Проект состоит из двух полностью независимых микросервисов:

| Сервис | Порт | Назначение | БД |
|---|---|---|---|
| **user_service** | 8081 | Управление пользователями | PostgreSQL (`userdb`) |
| **task_service** | 8082 | Управление задачами, делегирование | PostgreSQL (`taskdb`) |

**Ключевое архитектурное правило:** сервисы полностью изолированы друг от друга. У каждого своя база данных, между базами нет внешних ключей (FK). Любое взаимодействие между сервисами происходит исключительно по сети - `task_service` обращается к `user_service` через HTTP (`RestClient`) для проверки существования пользователя при делегировании задачи.

## Технологический стек

- **Java 17**
- **Spring Boot 3.x** (Web, Data JPA, Validation)
- **PostgreSQL 16** - основная СУБД (H2 - только для тестов)
- **Liquibase** - управление миграциями схемы БД
- **RestClient** - межсервисное HTTP-взаимодействие
- **springdoc-openapi** - автогенерация Swagger UI
- **JUnit 5 + MockMvc + Mockito** - unit-тестирование
- **Docker / Docker Compose** - контейнеризация и оркестрация
- **GitLab CI** - автоматическая сборка и проверка при каждом MR

## Структура репозитория

```
task-tracker-monorepo/
├── .gitlab-ci.yml            # Пайплайн CI (сборка user_service и task_service)
├── docker-compose.yml        # Запуск всей системы одной командой
├── docker/
│   └── postgres/
│       └── init-multiple-databases.sql   # Создание userdb и taskdb в одном контейнере
├── docs/                     # PlantUML-диаграммы аналитики
│   ├── task-tracker-usecase.puml
│   ├── task-delegate-sequence.puml
│   ├── er-user.puml
│   ├── tasks-er.puml
│   └── PROMPTS.md            # Промпты, использованные при генерации диаграмм
├── user_service/              # Микросервис пользователей
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── task_service/               # Микросервис задач
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── README.md
```

## Как запустить проект

### Способ 1 - Docker Compose

Требования: установленный **Docker Desktop**.

```bash
git clone https://gitlab.com/AnastasiaSurzhikowa/task-tracker-monorepo.git
cd task-tracker-monorepo
docker-compose up --build
```

Эта команда поднимет:
- PostgreSQL с двумя базами (`userdb`, `taskdb`) в одном контейнере;
- `user_service` на `http://localhost:8081`;
- `task_service` на `http://localhost:8082`.

Для полной остановки и очистки (включая удаление данных БД):
```bash
docker-compose down -v
```
Без флага `-v` - данные в PostgreSQL сохранятся между перезапусками (используется именованный volume).

### Способ 2 - локальный запуск через Maven

Требования: JDK 17, Maven, локально запущенный PostgreSQL (либо временный контейнер).

Поднять временный PostgreSQL:
```bash
docker run --name temp-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=userdb -p 5432:5432 -d postgres:16
```

Запустить каждый сервис в отдельном терминале:
```bash
cd user_service
./mvnw spring-boot:run
```
```bash
cd task_service
./mvnw spring-boot:run
```

По умолчанию (без переменных окружения) сервисы подключаются к `localhost:5432`.

## API-документация (Swagger)

После запуска доступна автоматически сгенерированная документация:

- User Service: http://localhost:8081/swagger-ui.html
- Task Service: http://localhost:8082/swagger-ui.html

## Примеры запросов

**Создание пользователя:**
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Anna","lastName":"Ivanova","email":"anna@test.com"}'
```

**Создание задачи:**
```bash
curl -X POST http://localhost:8082/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Write report","creatorId":1}'
```

**Делегирование задачи:**
```bash
curl -X POST "http://localhost:8082/tasks/1/delegate?assigneeId=1"
```

**Получение списка задач пользователя:**
```bash
curl http://localhost:8082/tasks/user/1
```

## Тестирование

Запуск unit-тестов для каждого сервиса:
```bash
cd user_service && ./mvnw test
cd task_service && ./mvnw test
```

Полная сборка с тестами (аналогично CI):
```bash
./mvnw clean verify
```

Покрытие тестами:
- `@WebMvcTest` + `MockMvc` - тестирование контроллеров в изоляции (сервисы замокированы через `@MockitoBean`);
- `Mockito` (`@ExtendWith(MockitoExtension.class)`) - тестирование бизнес-логики `TaskService`, включая проверку того, что при отсутствии задачи или её завершённом статусе обращение к `user_service` не происходит.

## Документация и диаграммы

В папке [`docs/`](./docs) находятся PlantUML-диаграммы, подготовленные на этапе системной аналитики:
- **Use Case Diagram** - сценарии использования системы;
- **Sequence Diagram** - процесс делегирования задачи с сетевым взаимодействием между сервисами;
- **ER-диаграммы** - независимые схемы баз данных `user_service` и `task_service`.

Файл [`docs/PROMPTS.md`](./docs/PROMPTS.md) содержит промпты, использованные при генерации диаграмм через ИИ-агента.

## Git-флоу проекта

- Прямые коммиты в `main` запрещены - вся разработка ведётся через feature-ветки и Merge Request'ы.
- Каждый MR проходит автоматическую проверку через GitLab CI (`mvn clean verify`) перед слиянием.
- Схема именования веток: `feature/*` - новая функциональность, `fix/*` - исправления, `test/*` - тесты, `chore/*` - конфигурация/инфраструктура.
- Коммиты оформлены по стандарту Conventional Commits.

## Автор

**Суржикова Анастасия Сергеевна, группа 4314 (СПБГУАП)**
Практика Java Backend (Java / Spring Boot), 2026
