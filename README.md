# BeOrganized

## Description

A Vue single-page web application with a Spring Boot backend. The project is organized into the frontend and the backend side. Application is run within a Docker containers managed by Docker Compose.

### Functionality 

Main functionalities of the application are time-organization and task management. User can add things to do and plan various events. Added items can be organized into custom categories. Schedule and calendar functionality provide user with a monthly and weekly-overview. To preserve important information, users can create notes. Reminders can be created in order to track and not forget about important dates.

----

## Application Structure

The browser sends a user request which is received by the Nginx web server. Depending on the request type, the server serves static files or redirects a request to the backend. The backend processes request and communicates with the database to save or retrieve data, and sends a response that is forwarded to the client.

----

## Backend


### Architecture

The backend follows a layered Spring architecture:

- **Controllers** expose REST endpoints under `/api/**`
- **Services** contain business logic and transaction boundaries
- **Repositories** access the database through Spring Data JPA
- **DTOs** isolate request and response payloads from persistence models
- **Mappers** convert between entities and DTOs with MapStruct
- **Config** classes centralize security, password encoding, CSRF handling, and mapping setup

The backend also includes:

- session-based authentication and authorization
- CSRF protection with a cookie-based token repository
- validation through Jakarta Bean Validation
- reminder scheduling support
- server-sent events for live reminder updates
- global exception handling for API errors

### Technology Stack

- Java 17
- Maven
- MapStruct
- Spring Boot
- Hibernate / JPA (Spring Data JPA)
- Spring Security
- Spring Web
- JUnit Jupiter with H2 database and testcontainers
- Jakarta Bean Validaton (Spring Boot Validation)

----

## Frontend


### Architecture

The frontend is a Vue single-page application organized by feature:

- **`src/app`** contains app bootstrap and router setup
- **`src/common`** holds shared layout, UI, HTTP, and utility code
- **`src/features/*`** groups feature-specific views, components, composables, API clients, and tests

Navigation is handled by Vue Router with route guards for authenticated areas. Pinia stores manage shared client state, while a centralized Axios instance handles API calls and sends credentials with each request. The app is built with Vite and served behind Nginx in production.

### Technology Stack

- TypeScript
- Vue
- CSS with Bootstrap
- npm
- Node.js
- Vite
- Axios
- Pinia
- Vue Router
- Nginx
- Vitest

----

## Containerization

The project is designed to run with Docker Compose and consists of three containers:

- **frontend**: builds the Vue app with Node.js and serves the production bundle through Nginx on port 80
- **backend**: builds the Spring Boot application into a JAR and runs it on port 8081
- **mysql**: runs MySQL 8.4 with persistent storage in the `mysql_data` volume

The frontend Nginx configuration serves the SPA and proxies `/api/` requests to the backend container. The containers communicate over the Compose network named `app`.

----

## Ports

These are the ports used by the application:

### External app port

- `80` - production frontend served by Nginx in Docker Compose

### Internal container network ports

- `8081` - Spring Boot backend inside the Compose network
- `3306` - MySQL inside the Compose network

### Local development port

- `8080` - Vite development server for the frontend   

----

## Required setup

Before running the application, set the required environment variables:

- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PORT`
- `SERVER_PORT`
- `SESSION_COOKIE`

These are used by Docker Compose, the MySQL container, and Spring Security.

----

## Database 

### Database Management System (DBMS)

Application data is stored in a relational database managed by the MySQL DBMS. MySQL database is run in a Docker container.

### Configuration

The `spring.jpa.hibernate.ddl-auto` during development it was set to 'update', so that the JPA/Hibernate was treated as the authoritative development-time schema source. For the production, the value was changed to `validate` to prevent database schema changes and verify entities' alignment to the database tables.

The `spring.sql.init.mode=never` prevents SQL-init from competing with the entity-driven schema strategy.

### Setup

No manual database creation is required.
The database is initialized automatically by the `mysql` service in `compose.yaml`. When the container starts for the first time with an empty `mysql_data` volume, the official MySQL image creates:

- the database specified by `MYSQL_DATABASE`
- the user specified by `MYSQL_USER`
- the password specified by `MYSQL_PASSWORD`

The Spring Boot backend then connects to this database and creates or updates the tables automatically through JPA/Hibernate.

----
## Run

### Docker Compose

The recommended way to run the full application is:

```bash
docker compose up --build
```

This starts the database, backend, and frontend together.

### Local development

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Backend:

```bash
cd backend
mvn spring-boot:run
```

Start the backend first, then run the Vite dev server. The Vite dev server runs on port `8080` and proxies `/api` requests to the backend on `8081`.

----

## Test

### Frontend tests

```bash
cd frontend
npm run test:unit
```

The frontend test suite includes API layer, composable, utility methods, and component tests.

### Frontend build and type-check

```bash
cd frontend
npm run build
```

### Backend tests

```bash
cd backend
mvn test
```

The backend test suite includes repository, service, controller, and API integration tests.

----

## Security Behavior

- Authentication is session-based
- The session cookie name comes from the `SESSION_COOKIE` environment variable
- Cookies are HTTP-only and marked secure
- CSRF protection is enabled through `SecurityConfig`; the SPA fetches `/api/auth/csrf` on startup to prime the CSRF cookie, and login/logout refresh it
- Sessions time out after 30 minutes
- `POST /api/auth/register`, `POST /api/auth/login`, and `GET /api/auth/status` are public
- All other API routes require authentication
- Logout invalidates the session and clears the session cookie
- Account deletion also invalidates the active session
- The frontend Axios client sends requests with credentials enabled

----

## Recommended Flow

1. Set the required environment variables.
2. Start the application with Docker Compose.
3. Register a user account or log in with an existing account.
4. Create categories to organize work.
5. Add tasks, events, notes, and reminders.
6. Use the dashboard, schedule, and calendar views for planning.
7. Use quick-add controls for fast item creation when available.

----

## Author

Natalia Oskiera

GitHub: @Natalia2758

## License

*Copyright © 2026 BeOrganized Natalia Oskiera. All rights reserved*