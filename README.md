# mysql-redis-caching-project

An education project for mastering MySQL, Redis, Hibernate and Docker.
The goal is to optimize database performance by caching frequently requested data into a Redis key-value memory storage.

## *1. Стек технологий*

1. Java 21 - версия языка.
2. Maven - управление зависимостями.
3. Hibernate 7 - ORM для работы с базой данных.
4. MySQL Connector/J  - драйвер для подключения к базе данных.
5. P6spy - инструмент для логирования и анализа SQL-запросов.
6. Docker - для контейнеризации (Redis и MySQL).
7. Lombok - для генерации boilerplate-кода.
8. Redis - для кэширования данных (in-memory хранилище).
9. Lettuce - Redis клиент.
10. Jackson - сериализация объектов в JSON.
11. JUnit 5 (Jupiter API) - интеграционные тесты.
12. SLF4J + Logback - система логирования.

## *2. Функционал*
Приложение представляет собой бенчмарк (консольный тестовый стенд) для сравнения производительности реляционной базы данных MySQL (через Hibernate ORM) и in-memory кэша Redis.
### Работа с реляционной БД
* Маппинг сущностей `City`, `Country` и `CountryLanguage` с настроенными связями.
* Выгрузка данных из базы с поддержкой пагинации.

### Кэширование
* Сериализация связанных объектов в JSON с помощью Jackson.
* Хранение и извлечение данных по ключам через Lettuce.

### Модуль сравнительного тестирования
* Интеграционные тесты на базе JUnit 5 для замеров скорости чтения.
* Прямое сравнение производительности запросов из MySQL против кэша Redis.

### Отладка и мониторинг
* Перехват SQL-запросов с помощью P6Spy.
* Логирование работы приложения через SLF4J + Logback.

## *3. Инструкция по запуску*

Для запуска проекта потребуется:
```text
1. JDK 21 или выше
2. Maven 3.x
3. Docker
```

### 3.1. Скачивание проекта
Откройте терминал в нужной папке и клонируйте репозиторий:
```bash
git clone git@github.com:MalyarchukNA/mysql-redis-caching-project.git
cd mysql-redis-caching-project
```

### 3.2. Запуск инфраструктуры в Docker
Запустите контейнер с MySQL, выполнив следующую команду в терминале:
```bash
docker run --name mysql -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root --restart unless-stopped -v mysql:/var/lib/mysql mysql:8
```
Запустите Redis-сервер как докер контейнер:
```bash
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

### 3.3 Развертывание дампа базы данных
#### 3.3.1 Подключитесь к MySQL через любую удобную программу (например, MySQL Workbench или DBeaver):

| Параметр | Значение  |
|----------|-----------|
| Host     | localhost |
| Port     | 3306      |
| User     | root      |
| Password | root      |

#### 3.3.2 Скачайте файл дампа [world.sql](db/dump-hibernate-final.sql) из репозитория.
#### 3.3.3 Импортируйте файл дампа (.sql) в СУБД:
#### 3.3.4 База данных (world) и таблицы создадутся автоматически.

### 3.4 Сборка проекта
Соберите проект, выполнив в корневой папке проекта команду:
```bash
mvn clean package
```

### 3.5 Запуск бенчмарка
Запустите интеграционные тесты, которые выполняют замеры производительности и выводят результаты в консоль:
```bash
mvn test
```
После завершения тестов вы сможете изучить подробные логи выполнения в консоли или открыть файл логов ./logs/debug.log, где фиксируются все операции с базой данных и кэшем.
