# Промпты для Kilo Code

## Проверка навыка
Промпт: Я только что установила plantuml-skill. Можешь ли ты подтвердить, что у тебя есть доступ к этому навыку и ты можешь создавать диаграммы PlantUML для моего проекта микросервисов?
Результат: Навык загружен — plantuml-skill доступен и прочитан.

## Use Case Diagram
Промпт: Создай PlantUML use case diagram для системы Task Tracker. Два актора: User и Admin. Основные сценарии для User: регистрация, авторизация, создание задачи, просмотр списка своих задач, делегирование задачи другому пользователю. Для Admin: просмотр всех задач, управление пользователями. Выдели цветом #E6F3FF (светло-голубой) сценарии, которые относятся к будущему масштабированию проекта: уведомления о новых задачах, комментарии к задачам, история изменений статуса, роли и права доступа. Используй стандартный синтаксис PlantUML usecase diagram.
Результат: Файлы task-tracker-usecase.puml и task-tracker-usecase.png созданы.

## Sequence Diagram 
Промпт: Создай PlantUML sequence diagram для процесса делегирования задачи в микросервисной архитектуре. Участники: Client, TaskService, UserService. Сценарий: Client отправляет POST /tasks/{taskId}/delegate?assigneeId=X в TaskService. TaskService делает HTTP GET запрос в UserService по адресу /users/{assigneeId}, чтобы проверить существование пользователя. Покажи это как отдельный сетевой вызов между двумя сервисами (не внутри одного блока). Добавь блоки alt/else со следующими сценариями:
alt Успех: пользователь найден (200 OK от UserService), задача обновляется, TaskService возвращает Client 200 OK.
else Пользователь не найден: UserService возвращает 404, TaskService возвращает Client 404 Not Found.
else Задача не найдена: TaskService сразу возвращает Client 404 Not Found (без обращения к UserService).
else Некорректный статус задачи: если задача уже в статусе DONE, TaskService возвращает Client 400 Bad Request.
Используй activate/deactivate для отображения времени жизни вызовов.
Результат: Файлы task-delegate-sequence.puml и task-delegate-sequence.png созданы.

## ER-диаграммы (две отдельные, по одной на сервис)
Промпт №1: Создай PlantUML entity relationship diagram только для одной таблицы: users. Поля: id (Long, primary key, auto increment), first_name (varchar), last_name (varchar), email (varchar, unique). Это отдельная изолированная база данных микросервиса user_service, без связей с другими таблицами.
Результат: Файлы users-er.puml и users-er.png созданы.

Промпт №2: (отдельным запросом, не в том же чате-контексте) Создай PlantUML entity relationship diagram только для одной таблицы: tasks. Поля: id (Long, primary key, auto increment), title (varchar), status (enum: TODO, IN_PROGRESS, DONE), creator_id (Long — логическая ссылка на пользователя, БЕЗ foreign key constraint), assignee_id (Long — логическая ссылка на пользователя, БЕЗ foreign key constraint, может быть null). Это отдельная изолированная база данных микросервиса task_service.
Результат: Файлы tasks-er.puml и tasks-er.png созданы.

