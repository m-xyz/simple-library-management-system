# A (Simple) Library Management System
A (simple) library management system that allows clients to browse, borrow, and return books. Owners can manage the library inventory, add / update or decommission books, track loan history and currently on loan books. Includes a late return fee fine for clients that return a book late, the fine amount and book loan days can be configure in the ```application.properties``` file, also includes a queue system where clients can get on a waitlist for a particular book they want but it's not currently available.


## Setup

### Prerequisites

- Java 25
- Maven
- MySQL 8.x

#
Define the following environment variables needed for the database connection used by the application:
```bash
export MYSQL_DB_NAME=libmansys
export MYSQL_DB_USER=root
export MYSQL_DB_PW=your_password
```
```
application.properties:

spring.datasource.url=${MYSQL_DB_NAME}
spring.datasource.username=${MYSQL_DB_USER}
spring.datasource.password=${MYSQL_DB_PW}
```

### Database Setup

Create a MySQL database with a name that matches \${MYSQL_DB_NAME} env variable

```sql
CREATE DATABASE libmansys;
```

Flyway is used for DB migration, JPA Hibernate validates DB schemas, Flyway is responsible for applying schema changes.<br>
Migration files are located in:

```
src/main/resources/db/migration/
```

### Running/Building the application
To run the app (after setting up the DB env variables), run the command:

```
./mvnw spring-boot:run
```
or alternatively, build the application first:
```
./mvnw clean package
```
### Run tests

```
./mvnw test
```

## DB Schema

                         ┌─────────────────────┐
                         │       AUTHORS       │
                         ├─────────────────────┤
                         │ PK author_id        │
                         │    name             │
                         └──────────┬──────────┘
                                    │
                                    │ 1
                                    │
                                    │ N
                         ┌──────────▼──────────┐
                         │        BOOKS        │
                         ├─────────────────────┤
                         │ PK isbn             │
                         │    title            │
                         │    stock            │
                         │ FK author_id        │
                         │    decommissioned   │
                         └──────────┬──────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    │ 1                             │ 1
                    │                               │
                    │ N                             │ N
          ┌─────────▼───────────┐          ┌────────▼────────────┐
          │     BOOK_LOANS      │          │    WAITING_LIST     │
          ├─────────────────────┤          ├─────────────────────┤
          │ PK/FK transaction_id│          │ PK id               │
          │ PK/FK isbn          │          │ FK isbn             │
          │       return_date   │          │ FK user_id          │
          │       fine          │          │    request_date     │
          └─────────┬───────────┘          └────────┬────────────┘
                    │                              │
                    │ N                            │ N
                    │                              │
                    │ 1                            │ 1
          ┌─────────▼──────────┐          ┌────────▼────────────┐
          │    TRANSACTIONS    │          │       USERS         │
          ├────────────────────┤          ├─────────────────────┤
          │ PK transaction_id  │          │ PK id               │
          │ FK user_id         │──────────│    username         │
          │    request_date    │    N:1   │    email            │
          │    due_date        │          │    password         │
          └────────────────────┘          │    role             │
                                          └─────────────────────┘

| Property                            | Description                                     |
| ----------------------------------- | ----------------------------------------------- |
| `spring.datasource.url`             | MySQL database connection                       |
| `spring.datasource.username`        | MySQL username                                  |
| `spring.datasource.password`        | MySQL password                                  |
| `spring.jpa.hibernate.ddl-auto`     | Hibernate schema validation mode                |
| `spring.flyway.baseline-on-migrate` | Allows Flyway to baseline the existing database |
| `spring.flyway.baseline-version`    | Version assigned to the existing database       |
| `loan.late.fee`                     | Late return fee per day                         |
| `book.borrow.days`                  | Number of days a book can be borrowed           |


## HTTP Endpoints

The application uses Spring MVC controllers to handle authentication, book management, borrowing, returns, and waiting lists.

Most endpoints return server-rendered HTML pages or redirects. Some endpoints return JSON/data responses and can be used by client-side JavaScript.

### General / Authentication

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/` | Application home page. Redirects authenticated users to their role-specific home page. | Public |
| `GET` | `/home` | Redirects authenticated users to the appropriate client or owner page. | Authenticated |
| `GET` | `/login` | Displays the login page. | Public |
| `GET` | `/signup` | Displays the registration page. | Public |
| `POST` | `/signup` | Registers a new user. | Public |
| `GET` | `/forbidden` | Displays the access-denied page. | Public |

### Client Endpoints

Client endpoints are available to authenticated users with the `CLIENT` role.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/client` | Displays available books and the client's dashboard. |
| `POST` | `/client/borrow` | Borrows one or more books. |
| `GET` | `/client/return-book` | Displays the client's active loans and waiting-list entries. |
| `POST` | `/client/return-book` | Returns one or more books and calculates any applicable late fees. |
| `POST` | `/client/waitlist/{isbn}` | Adds the authenticated client to the waiting list for a book. |

### Owner Endpoints

Owner endpoints are available to authenticated users with the `OWNER` role.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/owner` | Displays the owner dashboard and current book inventory. |
| `GET` | `/owner/add-book` | Displays the add-book form. |
| `POST` | `/owner/add-book` | Adds a new book to the inventory. |
| `GET` | `/owner/loan-history` | Displays the complete loan history. |
| `GET` | `/owner/loans/{isbn}` | Returns loan information for a specific book. |
| `PUT` | `/owner/edit-book/{isbn}` | Updates a book's title, author, and stock. |
| `POST` | `/owner/decomm-book/{isbn}` | Decommissions a book. |
