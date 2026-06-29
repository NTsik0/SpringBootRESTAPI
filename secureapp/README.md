# SecureApp

A production-ready Spring Boot REST API by **Nikoloz Tsikaridze**.

Implements a full user-management and task-tracking system with Spring Security, role-based access control, REST CRUD, JPA with entity relationships, externalized configuration, internationalization (i18n), structured logging, application monitoring (Actuator), and a comprehensive automated test suite.

---

## Technologies Used

| Category | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6, BCrypt |
| Persistence | Spring Data JPA, Hibernate, H2 (dev), PostgreSQL (prod) |
| API Docs | SpringDoc OpenAPI 2 / Swagger UI |
| Monitoring | Spring Boot Actuator, Micrometer, Prometheus |
| Validation | Jakarta Bean Validation (JSR-303) |
| Logging | SLF4J, Logback |
| Testing | JUnit 5, Mockito, MockMvc, JaCoCo |
| Build | Maven |
| Utilities | Lombok |

---

## Running the Application

The app has two profiles: `dev` (H2 in-memory) and `prod` (PostgreSQL).

### Command line

```bash
# Development (default)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Packaged JAR
java -jar target/secureapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar target/secureapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

On Windows replace `./mvnw` with `.\mvnw.cmd`.

### IntelliJ IDEA

1. Open **Run → Edit Configurations**
2. Set **Active profiles** to `dev` or `prod`
3. Click Apply → OK and run

---

## User Credentials and Roles

Seed data is created automatically when the `dev` profile is active.

| Username  | Password   | Role       | Access |
|-----------|------------|------------|--------|
| `admin`   | `admin123` | ROLE_ADMIN | Everything, including `/admin/**`, `/api/users`, `/actuator/**` |
| `nikoloz` | `user123`  | ROLE_USER  | Dashboard, profile, own tasks via `/api/tasks` |

> In production, create users via `POST /api/auth/register` or directly in the database.

---

## REST API Endpoints

### Public (no authentication required)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/info` | App metadata (profile-aware) |
| GET | `/api/auth/ping` | Health ping |
| POST | `/api/auth/register` | Register a new user (returns **201 Created**) |

### Tasks (authenticated users — own tasks only)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/tasks` | List my tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| PATCH | `/api/tasks/{id}/toggle` | Toggle completed status |
| DELETE | `/api/tasks/{id}` | Delete a task |

### Users (ADMIN only)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/users/me` | Current user profile |
| GET | `/api/users/{id}` | Get user by ID |
| DELETE | `/api/users/{id}` | Delete a user |

---

## Swagger / OpenAPI

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

All REST endpoints are documented. Use the **Authorize** button (HTTP Basic) to test protected endpoints directly from the browser.

---

## Monitoring Endpoints (Spring Boot Actuator)

| Endpoint | Access | Description |
|---|---|---|
| `GET /actuator/health` | Public | Application health status |
| `GET /actuator/info` | Public | Application metadata |
| `GET /actuator/metrics` | ADMIN only | Micrometer metrics list |
| `GET /actuator/metrics/{name}` | ADMIN only | Specific metric |
| `GET /actuator/prometheus` | ADMIN only | Prometheus scrape endpoint |

### Testing with curl

```bash
# Public health check
curl http://localhost:8080/actuator/health

# Public info
curl http://localhost:8080/actuator/info

# Metrics (requires admin credentials — HTTP Basic is supported)
curl -u admin:admin123 http://localhost:8080/actuator/metrics

# REST API with HTTP Basic auth
curl -u nikoloz:user123 http://localhost:8080/api/tasks
curl -u admin:admin123 http://localhost:8080/api/users
```

> Unauthenticated requests to `/api/**` return **401 Unauthorized**. Requests to the web UI (e.g. `/dashboard`) redirect to `/login`.

---

## Testing

### Run all tests

```bash
./mvnw test
```

### Run with JaCoCo coverage report

```bash
./mvnw verify
```

Coverage report is generated at `target/site/jacoco/index.html`.

### Test types included

| Type | Class | What it tests |
|---|---|---|
| Unit | `UserServiceTest` | `UserService` business logic with Mockito |
| Unit | `TaskServiceTest` | `TaskService` business logic with Mockito |
| Repository slice | `UserRepositoryTest` | `UserRepository` queries with `@DataJpaTest` |
| Repository slice | `TaskRepositoryTest` | `TaskRepository` custom queries with `@DataJpaTest` |
| Controller slice | `AuthApiControllerTest` | Registration endpoint with `@WebMvcTest` |
| Controller slice | `TaskControllerTest` | All task endpoints with `@WebMvcTest` |
| Integration | `TaskIntegrationTest` | Full Spring context, H2, REST + DB assertions |

---

## Custom Configuration Properties

Defined under the `app.settings` prefix in `application.yml`:

| Property | Validation | Purpose |
|---|---|---|
| `app.settings.title` | `@NotBlank` | App name shown in `/api/info` responses |
| `app.settings.pagination-limit` | `@Min(1)` `@Max(200)` | Max records per page |
| `app.settings.external-service-url` | `@NotBlank` | Base URL of external service |
| `app.settings.contact-email` | `@NotBlank` `@Email` | Contact email in metadata |
| `app.settings.debug-mode` | — | Adds diagnostic fields in dev responses |

These are validated at startup — the application refuses to start if any constraint is violated.

---

## Internationalization (i18n)

Supported languages: **English** (`en`) and **Georgian** (`ka`).

The language is resolved from the `Accept-Language` HTTP header via `AcceptHeaderLocaleResolver`.

| Endpoint / Case | What is localized |
|---|---|
| `GET /api/info` | Welcome message |
| `POST /api/auth/register` | Success message |
| DTO validation errors | Field error messages |
| All exception responses | Error message body |

### Testing i18n

```bash
# English (default)
curl http://localhost:8080/api/info -H "Accept-Language: en"

# Georgian
curl http://localhost:8080/api/info -H "Accept-Language: ka"

# Validation errors in Georgian
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Accept-Language: ka" \
  -d '{"username":"","password":"123","displayName":""}'
```

---

## Logging

Logs are written to both the console and a rolling file.

**Log file location:**
```
logs/app.log
```

Archived logs:
```
logs/app-yyyy-MM-dd.N.log.gz
```

### Log levels by profile

| Profile | Root level | `com.nikoloz.secureapp` | Spring Security |
|---------|-----------|------------------------|-----------------|
| `dev` | INFO | DEBUG | INFO |
| `prod` | WARN | INFO | inherited |

Rolling policy: 10 MB max file size, 30 days history, 500 MB total cap.

---

## Profile Configuration

### dev profile

- Uses H2 in-memory database
- Schema recreated on every startup (`ddl-auto: create-drop`)
- H2 console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:secureappdb`)
- Test users and tasks seeded automatically
- Debug mode on (extra fields in `/api/info`)
- Log level: DEBUG for app package

### prod profile

- Connects to PostgreSQL (set env vars before running):

```bash
export DB_HOST=your-db-host
export DB_NAME=secureappdb
export DB_USER=secureapp_user
export DB_PASS=your-secure-password
```

- Schema validation only (`ddl-auto: validate`)
- H2 console disabled
- Debug mode off
- Log level: WARN for root, INFO for app package

---

## Key URLs (dev)

```
http://localhost:8080/                → Home page
http://localhost:8080/login           → Login
http://localhost:8080/register        → Register
http://localhost:8080/dashboard       → User dashboard
http://localhost:8080/admin           → Admin dashboard (ADMIN only)
http://localhost:8080/api/info        → App metadata
http://localhost:8080/swagger-ui.html → Swagger UI
http://localhost:8080/actuator/health → Health check
http://localhost:8080/h2-console      → H2 browser
```
