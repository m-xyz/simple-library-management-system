# A (Simple) Library Management System

application.properties:
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

Example:
export DATABASE_URL=jdbc:postgresql://localhost:8080/myapp
export DATABASE_USERNAME=myapp
export DATABASE_PASSWORD=myapp

./mvnw spring-boot:run


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
          ┌─────────▼──────────┐          ┌────────▼────────────┐
          │     BOOK_LOANS     │          │    WAITING_LIST     │
          ├────────────────────┤          ├─────────────────────┤
          │ PK/FK transaction_id│         │ PK id               │
          │ PK/FK isbn         │          │ FK isbn             │
          │    return_date     │          │ FK user_id          │
          │    fine            │          │    request_date     │
          └─────────┬──────────┘          └────────┬────────────┘
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