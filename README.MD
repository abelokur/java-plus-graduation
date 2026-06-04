# Explore With Me
## Описание проекта
**Explore With Me** — это распределённое приложение-афиша, позволяющее пользователям делиться информацией о различных событиях и находить компанию для участия в них. Система поддерживает создание событий, управление заявками на участие, формирование тематических подборок, а также оставление комментариев.

## Архитектура проекта
Проект построен по микросервисной архитектуре и состоит из нескольких независимых сервисов:

### Основные сервисы (`core`):
Состоит из:
- Бизнес сервисы:
  - **Event Service** (`event-service`)
    - Управление событиями: создание, редактирование, публикация, поиск.
    - Управление категориями: создание, редактирование, публикация, поиск.
    - Управление подборками: создание, редактирование, публикация, поиск.
  
  - **User Service** (`user-service`)
      - Управление пользователями: регистрация, удаление, получение информации.
  
  - **Request Service** (`request-service`)
    - Управление заявками на участие в событиях.
  
  - **Comment Service** (`comment-service`)
    - Управление комментариями к событиям.

- Вспомогательный библиотечный модуль:
  - **Interaction API** (`interaction-api`):
    - Кастомные аннотации
    - Контракты Feign-клиентов для межсервисного взаимодействия
    - Общие DTO (Data Transfer Objects)
    - Исключения (exceptions)
    - Базовый обработчик исключений
    - Перечисления (enum)
    - Утилитарные классы


### Stats Service (`stats`)
Сервис статистики для учёта просмотров событий.


### Infra (`infra`) 
Инфраструктурные компоненты.
- **Discovery Server** (`discovery-server`) 
    - сервис регистрации и обнаружения микросервисов на базе Eureka

- **Config Server** (`config-server`) 
  - централизованный сервис конфигурации

- **Gateway Server** (`gateway-server`) 
  - API Gateway на базе Spring Cloud Gateway

## Основные сущности:

- События (EventFullDto, EventShortDto)
- Пользователи (UserDto, UserShortDto)
- Категории (CategoryDto)
- Подборки событий (CompilationDto)
- Заявки на участие (ParticipationRequestDto)
- Комментарии (CommentDto, StateCommentDto)

Структура проекта (Maven модули)
```text
explore-with-me/
├── core/                          # Основные сервисы
│   ├── event-service/             # Сервис событий
│   ├── user-service/              # Сервис пользователей
│   ├── request-service/           # Сервис заявок
│   ├── comment-service/           # Сервис комментариев
│   └── interaction-api/           # Общий API для взаимодействия
├── stats/                         # Сервис статистики
│   ├── stats-service/             # Сервис статистики
│   ├── stats-dto/                 # Общие DTO для статистики
│   └── stats-client/              # Клиент статистики
├── infra/                         # Инфраструктура
│   ├── config-server/             # Сервис удаленной конфигурации
│   ├── discovery-server/          # Discovery Server
│   └── gateway-server/            # Единая точка входа в сервисы
└── pom.xml                        # Родительский POM
```

## Особенности реализации:
- Docker-compose для запуска PostgreSQL (Все БД запускаются в одном контейнере)
- Maven для сборки проекта.
- Java 21 для разработки.
- Spring Boot для создания микросервисов.
- Spring Cloud Gateway для API Gateway.
- Spring Cloud Eureka для регистрации и обнаружения микросервисов.
- Spring Cloud Config Server для централизованного управления конфигурацией.
- Spring Boot Actuator для мониторинга и логирования.
- Spring Data JPA для работы с БД.
- QueryDSL для создания динамических запросов к БД.
- MapStruct для маппинга объектов.
- Lombok для упрощения кода.
- Spring Retry для повторных попыток вызовов.
- OpenFeign для вызова методов других сервисов.
- Resilience4j для устойчивости к сбоям в межсервисном взаимодействии.
- Caffeine Cache для кеширования.

## Конфигурация
Проект использует Spring Cloud Config Server для централизованного управления конфигурацией.
Настройки указываются в `application.yml` каждого сервиса.
Конфигурации хранятся локально в сервисе `config-server`: `classpath:config/{profile}/{application}`

Адрес сервиса `discovery-server` - `http://${eureka.instance.hostname:localhost}:${eureka.instance.port:8761}/eureka/`
Все сервисы запускаются на случайном порту, кроме сервиса `discovery-server (8761)` и `gateway-server (8080)`.
Каждый сервис регистрируется в сервисе `discovery-server`.

Базы данных хранятся в PostgreSQL и запускаются в общем контейнере с доступом по порту 5432 - `jdbc:postgresql://localhost:5432/`
Для подключения используется пользователь `postgres` и пароль `postgres`.

## Open-Api
- Open-Api спецификация основной сервис - [ewm-main-service-spec.json](ewm-main-service-spec.json)
- Open-Api спецификация статистики - [ewm-stats-service-spec.json](ewm-stats-service-spec.json)
- Дополнительно реализован сервис по работе с комментариями.

## Технологический стек

- Java 21

- Maven 3.9.9

- Docker 28.5.1

- Docker Compose 2.40.3

- Spring Boot 3.5.5

- Spring Cloud (Eureka, Config Server, OpenFeign)

- PostgreSQL 42.7.7

- QueryDSL 5.1.0

- MapStruct 1.6.3

- Lombok 1.18.30

- SpringDoc OpenAPI 2.8.13

- Resilience4j + Spring Retry (отказоустойчивость)

- Caffeine Cache (кэширование)

## Требования
- Java 21+
- Maven 3.8.8+
- Docker 20.10+
- Docker Compose 2.20+

## Запуск

1. Сборка проекта
```bash
mvn clean install -DskipTests
```

2. Запуск баз данных

```bash
docker-compose up -d
```

3. Запуск сервисов:

- В первую очередь запускаются сервисы (в любой последовательности)
  - Discovery Server 
  - Config Server
- Затем запускаются остальные сервисы (в любой последовательности):
  - Gateway Server
  - User Service 
  - Event Service 
  - Request Service 
  - Comment Service 
  - Stats Server

## Примечание

При добавлении нового сервиса, нужно настроить регистрацию его в `discovery-server` и получение им конфигурации из `config-server`.
Пример конфигурации сервиса: `application.yml` в корне каждого сервиса.
При необходимости работы с новой базой данных, нужно добавить ее БД в файл инициализации БД [init-databases.sql](init-databases.sql)

**_Проект разработан в рамках учебного курса YandexPracticum(Java-разработчик (Расширенный))._**
