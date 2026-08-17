# A (Simple) Library Management System

A lightweight Spring Boot application for managing library inventory, loans and waiting lists. It provides:

- Book management (add, edit, decommission)
- Borrowing and returning with late-fee calculation
- Per-book waiting lists and automatic handover when copies are returned
- Server-rendered HTML pages for owners and clients

### Functionalities by role

- Owner
	- Manage books: add new books, edit book details (title, author, stock), decommission/reactivate books
	- View and export loan history (`/owner/loan-history`)
	- Inspect active loans per ISBN (`/owner/loans/{isbn}`)

- Client
	- Browse available books and view personal dashboard (`/client`)
	- Borrow books (`POST /client/borrow`) with duplicate-loan protection
	- Return books and receive fine calculation if late (`/client/return-book`)
	- Join per-book waiting lists (`POST /client/waitlist/{isbn}`) and see position in queue

See [src/main/resources/application.properties](src/main/resources/application.properties) for configurable properties (loan days, late fee, datasource).

## Prerequisites
- Java 17 or newer (project compiled with a recent JDK)
- Maven 3.8+ or the bundled `mvnw`
- MySQL 8.x (or adjust datasource for an alternative)

## Configuration

Set database connection environment variables (example):

```bash
export MYSQL_DB_NAME=libmansys
export MYSQL_DB_USER=root
export MYSQL_DB_PW=your_password
```

Then ensure `application.properties` references these variables for `spring.datasource.*`.

Database

- Flyway migrations are applied at startup. Migration scripts live in `src/main/resources/db/migration/`.
- Create the database before running the app, for example:

```sql
CREATE DATABASE libmansys;
```

## Build & run

```bash
# Run with the bundled wrapper
./mvnw spring-boot:run

# Or build a package
./mvnw clean package
java -jar target/vestas_proj-0.0.1-SNAPSHOT.jar
```

### Tests

Run the full test suite with:

```bash
./mvnw test
```

You can run a single class or method with Maven's `-Dtest` option, for example:

```bash
./mvnw -Dtest=BookServiceTest test
```

**Unit tests use JUnit 5 and Mockito and do not require a running database.**

## Database schema

![Database diagram](docs/db-diagram.svg)

## API

The application exposes server-rendered pages and a small number of JSON endpoints. Key routes include:

- Authentication: `/login`, `/signup`, `/forbidden`
- Client: `/client`, `/client/borrow`, `/client/return-book`, `/client/waitlist/{isbn}`
- Owner: `/owner`, `/owner/add-book`, `/owner/loan-history`, `/owner/loans/{isbn}`


### Authentication
- `GET /login` — Login page (Public)
- `GET /signup` — Registration page (Public)
- `POST /signup` — Register a new user (Public)

### Client (ROLE_CLIENT)
- `GET /client` — Client dashboard: browse available books and view personal loans (ROLE_CLIENT)
- `POST /client/borrow` — Borrow one or more books. Form param: `isbns` (set of ISBN strings). Returns redirect with flash messages (ROLE_CLIENT)
- `GET /client/return-book` — Page to view and select active loans for return (ROLE_CLIENT)
- `POST /client/return-book` — Return selected loans. Form param: `loanIds` (list of `transactionId:isbn` values). Returns redirect and success/error messages (ROLE_CLIENT)
- `POST /client/waitlist/{isbn}` — Join waiting list for a book. Returns `200 OK` with a success message or `400 Bad Request` with error text (ROLE_CLIENT, JSON/text response)

### Owner (ROLE_OWNER)
- `GET /owner` — Owner dashboard and inventory (ROLE_OWNER)
- `GET /owner/add-book` — Display add-book form (ROLE_OWNER)
- `POST /owner/add-book` — Add a new book. Form fields: `isbn`, `title`, `authorName`, `stock`. Returns redirect with flash message (ROLE_OWNER)
- `GET /owner/loan-history` — View complete loan history (ROLE_OWNER)
- `GET /owner/loans/{isbn}` — Returns JSON map with current loans for an ISBN (ROLE_OWNER, JSON)
- `PUT /owner/edit-book/{isbn}` — Update book details. Expects JSON body with `title`, `author`, `stock`. Returns `200 OK` on success (ROLE_OWNER)
- `POST /owner/decomm-book/{isbn}` — Toggle a book's decommissioned state. Returns `200 OK` (ROLE_OWNER)

⚠️ **Authentication and authorization are enforced via Spring Security; ensure test users have the appropriate `CLIENT` or `OWNER` role when exercising endpoints.**

#### Examples
- Borrow (form): `POST /client/borrow` with form field `isbns=ISBN1,ISBN2`
- Return (form): `POST /client/return-book` with `loanIds=123:ISBN1` (multiple values allowed)
- Join waitlist (AJAX): `POST /client/waitlist/9781234567890` → `200 OK` + message body

| Method | Endpoint | Required role | Description |
|---|---:|---|---|
| GET | `/login` | Public | Login page |
| GET | `/signup` | Public | Registration page |
| POST | `/signup` | Public | Create a new user |
| GET | `/client` | ROLE_CLIENT | Client dashboard, browse books |
| POST | `/client/borrow` | ROLE_CLIENT | Borrow one or more books (form) |
| GET | `/client/return-book` | ROLE_CLIENT | View active loans for return |
| POST | `/client/return-book` | ROLE_CLIENT | Return selected loans (form) |
| POST | `/client/waitlist/{isbn}` | ROLE_CLIENT | Join waiting list for a book (AJAX/text) |
| GET | `/owner` | ROLE_OWNER | Owner dashboard and inventory |
| GET | `/owner/add-book` | ROLE_OWNER | Add-book form |
| POST | `/owner/add-book` | ROLE_OWNER | Add a new book (form) |
| GET | `/owner/loan-history` | ROLE_OWNER | Full loan history (UI) |
| GET | `/owner/loans/{isbn}` | ROLE_OWNER | JSON: loans for ISBN |
| PUT | `/owner/edit-book/{isbn}` | ROLE_OWNER | Update a book (JSON) |
| POST | `/owner/decomm-book/{isbn}` | ROLE_OWNER | Toggle decommission state |

⚠️ **The application uses cookie-based authentication and CSRF protection. The examples below assume you have an authenticated session cookie (`JSESSIONID`) and a valid CSRF token value. Replace `<JSESSIONID>` and `<CSRF_TOKEN>`.**

- Borrow books (form):

```bash
curl -i -X POST 'http://localhost:8080/client/borrow' \
	-H 'Cookie: JSESSIONID=<JSESSIONID>' \
	-H 'X-CSRF-TOKEN: <CSRF_TOKEN>' \
	-H 'Content-Type: application/x-www-form-urlencoded' \
	--data 'isbns=9781234567890&isbns=9789876543210'
```

- Return books (form):

```bash
curl -i -X POST 'http://localhost:8080/client/return-book' \
	-H 'Cookie: JSESSIONID=<JSESSIONID>' \
	-H 'X-CSRF-TOKEN: <CSRF_TOKEN>' \
	-H 'Content-Type: application/x-www-form-urlencoded' \
	--data 'loanIds=123:9781234567890&loanIds=124:9789876543210'
```

- Join waiting list (AJAX/text response):

```bash
curl -i -X POST 'http://localhost:8080/client/waitlist/9781234567890' \
	-H 'Cookie: JSESSIONID=<JSESSIONID>' \
	-H 'X-CSRF-TOKEN: <CSRF_TOKEN>'
```

- Owner: add a book (form):

```bash
curl -i -X POST 'http://localhost:8080/owner/add-book' \
	-H 'Cookie: JSESSIONID=<JSESSIONID>' \
	-H 'X-CSRF-TOKEN: <CSRF_TOKEN>' \
	-H 'Content-Type: application/x-www-form-urlencoded' \
	--data 'isbn=9780000000001&title=My+Book&authorName=Jane+Doe&stock=3'
```





