# Auto Service

Консольное приложение для управления автосервисом: клиенты, машины,
заказ-наряды и позиции работ.

## Запуск

```bash
./gradlew run        # Linux / macOS
gradlew.bat run      # Windows
```

## Тесты

```bash
./gradlew test
```

## Стек

- Java 17, Gradle (wrapper), JUnit 5
- In-memory хранение (`Map`), без БД и внешних зависимостей

## Архитектура

```
ui → facade → service → repository → domain
```
```
vsu.cs
├── domain/          сущности, Identifiable, OrderStatus, константы
├── repository/      интерфейсы Repository<T, ID> + специфичные
│   └── impl/        InMemory* реализации (Map + AtomicLong)
├── service/         бизнес-логика: валидация, каскады, пересчёт total
├── facade/          AutoServiceFacade — тонкий делегат (Request → Response)
├── dto/
│   ├── request/     CreateClientRequest, AddItemRequest, ...
│   ├── response/    ClientResponse, WorkOrderResponse, ...
│   └── DtoMapper    domain ↔ response
├── ui/              ConsoleApp, *Controller, ConsoleIO
├── exception/       EntityNotFound, Validation, InvalidState
├── AppFactory       composition root (собирает всё)
└── Main             точка входа
```

Сборка зависимостей — в `AppFactory` (единственный composition root).