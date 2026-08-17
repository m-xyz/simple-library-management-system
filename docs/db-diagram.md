# Database Diagram

The diagram below shows the main database entities and relationships used by the application. It's written in Mermaid ER syntax; many readers and tools (GitHub, VS Code extensions) can render it directly.

```mermaid
erDiagram
    AUTHORS {
        INT id PK "author_id"
        STRING name
    }

    BOOKS {
        STRING isbn PK
        STRING title
        INT stock
        BOOLEAN decommissioned
        INT author_id FK
    }

    TRANSACTIONS {
        INT transaction_id PK
        INT user_id FK
        DATE request_date
        DATE due_date
    }

    BOOK_LOANS {
        INT transaction_id FK
        STRING isbn FK
        DATE return_date
        DECIMAL fine
    }

    WAITING_LIST {
        INT id PK
        STRING isbn FK
        INT user_id FK
        DATETIME request_date
    }

    USERS {
        INT id PK
        STRING username
        STRING email
        STRING password
        STRING role
    }

    AUTHORS ||--o{ BOOKS : "has"
    BOOKS ||--o{ BOOK_LOANS : "loaned_in"
    TRANSACTIONS ||--o{ BOOK_LOANS : "contains"
    USERS ||--o{ TRANSACTIONS : "makes"
    USERS ||--o{ WAITING_LIST : "joins"
    BOOKS ||--o{ WAITING_LIST : "has_waitlist"

```

Notes:
- The `BOOK_LOANS` table uses a composite key (`transaction_id`, `isbn`) in the application model.
- Field names reflect the domain model; consult `src/main/resources/db/migration` for exact schema DDL.
