# Auto Service

Консольное приложение для управления автосервисом: клиенты, машины,
заказ-наряды и позиции работ.

## Запуск

### Через Docker (рекомендуется)
```bash
docker-compose up --build
```

Команда собирает приложение, поднимает PostgreSQL 16, накатывает миграции и открывает консольное меню.
- Выход из меню: выберите exit в главном меню.
- Остановка контейнеров: docker-compose down (данные сохраняются в томе).
- Полный сброс базы: docker-compose down -v (удаляет все данные).

### Локально (без Docker)
1. Поднимите только базу данных:
```bash
docker-compose up -d db
```
2. Создайте файл .env в корне проекта (пример есть в .env.example):
```bash
DB_URL=jdbc:postgresql://localhost:5433/autoService
DB_USER=autoService
DB_PASSWORD=autoService
DB_MAX_POOL_SIZE=10
```
3. Запустите приложение:
```bash
./gradlew run        # Linux / macOS
gradlew.bat run      # Windows
```

## Тесты

```bash
./gradlew test
```

- Юнит-тесты (моки) проверяют бизнес-логику без базы.
- Интеграционные тесты (Testcontainers) проверяют JDBC-репозитории, транзакции и каскадные удаления на реальном PostgreSQL (требуется запущенный Docker).

## Стек

- Java 21, Gradle (wrapper), JUnit 5, Mockito
- In-memory хранение (`Map`), без БД и внешних зависимостей
- PostgreSQL 16 (через Docker)
- Liquibase (миграции схемы БД)
- HikariCP (пул соединений) / PGSimpleDataSource
- Testcontainers (интеграционные тесты)

##   База данных
Миграции находятся в `src/main/resources/db/changelog`.

Схема включает 4 таблицы с настроенными внешними ключами (`ON DELETE CASCADE`):

- `clients`
- `cars` (зависит от clients)
- `work_orders` (зависит от cars)
- `work_order_items`  (зависит от work_orders)

## Архитектура

```
ui → facade → service → repository → domain
```
```
vsu.cs
├── domain/          сущности, Identifiable, OrderStatus
├── repository/      интерфейсы Repository<T, ID> + специфичные
│   └── impl/
│       ├── inMemoryImpl/ InMemory* реализации (Map + AtomicLong)
│       └── jdbcImpl/     Jdbc* реализации (SQL, транзакции)
├── service/         бизнес-логика: валидация, бизнес-правила
├── facade/          ClientFacade, CarFacade, WorkOrderFacade
├── dto/
│   ├── request/     CreateClientRequest, AddItemRequest, ...
│   ├── response/    ClientResponse, WorkOrderResponse, ...
│   └── DtoMapper    domain ↔ response
├── db/
│   ├── datasource/  DataSourceFactory, Hikari и Simple реализации
│   ├── migration/   DatabaseMigrator (Liquibase)
│   └── transaction/ JdbcUtils (управление транзакциями), SqlFunction/Action
├── config/          DatabaseConfig, EnvironmentConfigReader (.env)
├── ui/              ConsoleApp, Menu, *Controller, ConsoleIO
├── exception/       EntityNotFound, Validation, InvalidState, RepositoryException
├── AppFactory       composition root (собирает граф для InMemory и JDBC)
└── Main             точка входа
```
Сборка зависимостей — в `AppFactory` (единственный composition root).