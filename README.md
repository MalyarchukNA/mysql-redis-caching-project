# mysql-redis-caching-project

An education project for mastering MySQL, Redis, Hibernate and Docker.
The goal is to optimize database performance by caching frequently requested data into a Redis key-value memory storage.

## *1. Стек технологий*

1. Java 21 - версия языка.
2. Maven - управление зависимостями.
3. Hibernate 7 - ORM для работы с базой данных.
4. MySQL Connector/J  - драйвер для подключения к базе данных.
5. p6spy - инструмент для логгирования и анализа SQL-запросов.
6. Docker - для контейнеризации (Redis и MySQL).
7. Lombok - для генерации boilerplate-кода.
8. Redis - для кэширования данных (in-memory хранилище).

## *2. Функционал*

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