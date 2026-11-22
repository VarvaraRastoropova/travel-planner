# Реализованные возможности и инструкция по запуску

## Реализовано

- Создание, просмотр, переименование и удаление поездок.
- Анализ пожеланий пользователя через Groq.
- Определение города по переданным координатам.
- Поиск городов, ориентиров и ближайших мест через Geoapify.
- Расчёт пешего маршрута: расстояние, время ходьбы и линия пути.
- Сохранение маршрутов в PostgreSQL и их повторное открытие.
- Валидация запросов, обработка ошибок и контроль версии при изменении поездки.

Автоматический подбор маршрута по интересам, авторизация и клиентский интерфейс пока не реализованы.

## Запуск
Нужны Docker Desktop и ключи API Groq и Geoapify.
Создайте `.env` в корне проекта:
```dotenv
POSTGRES_DB=travel_planner
POSTGRES_USER=travel
POSTGRES_PASSWORD=your_password

GROQ_API_KEY=your_groq_key
GEOAPIFY_API_KEY=your_geoapify_key
```
Запустите из корня проекта:
```bash
docker  compose  up  -d  --build
```

Swagger: http://localhost:8080/swagger-ui/index.html

Логи сервера:
```bash
docker  compose  logs  -f  server
```

Остановка:
```bash
docker  compose  down
```

## Проверка работы
В Swagger создайте поездку через `POST /api/trips`, затем рассчитайте и сохраните маршрут через `POST /api/trips/{tripId}/routes`. Получить его можно через `GET /api/trips/{tripId}/routes/{routeId}`.
Для анализа текста используйте `POST /api/recommendations/context`. Если город не указан, передайте `latitude` и `longitude`.

## Тесты
Требуются Java 21 и запущенный Docker Desktop. Команды выполняются из папки `server`.

Проверки API и настоящего PostgreSQL во временном контейнере, без обращения к внешним API:
```bash
./gradlew test
```

Проверки расчёта и сохранения маршрутов с настоящими Geoapify и PostgreSQL:
```bash
set -a
source ../.env
set +a
./gradlew externalTest
```

Для `externalTest` нужны интернет и действующий `GEOAPIFY_API_KEY` в `.env`.
