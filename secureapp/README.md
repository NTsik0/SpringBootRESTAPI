# SecureApp

A Spring Boot web application by Nikoloz Tsikaridze. Covers Spring Security with role-based access, externalized configuration, internationalization (i18n), DTO validation, and structured logging.

---

## Running the Application

The app has two profiles: `dev` for local development and `prod` for production.

### From the command line

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Run a packaged JAR:

```bash
java -jar target/secureapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar target/secureapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

On Windows replace `./mvnw` with `.\mvnw.cmd`.

### From IntelliJ IDEA

1. Open `Run → Edit Configurations`
2. Find the **Active profiles** field
3. Type `dev` or `prod`
4. Click Apply → OK and run

---

## Default Test Accounts (dev only)

| Username  | Password   | Role  |
|-----------|------------|-------|
| `admin`   | `admin123` | ADMIN |
| `nikoloz` | `user123`  | USER  |

---

## Production Database Setup

The `prod` profile connects to PostgreSQL. Set these environment variables before running:

```bash
export DB_HOST=your-db-host
export DB_NAME=secureappdb
export DB_USER=secureapp_user
export DB_PASS=your-secure-password
```

---

## Custom Configuration Properties

Defined under the `app.settings` prefix in `application.yml`:

| Property | Type | Validation | Purpose |
|---|---|---|---|
| `app.settings.title` | String | `@NotBlank` | App name shown in API responses |
| `app.settings.pagination-limit` | int | `@Min(1)` `@Max(200)` | Max records per page |
| `app.settings.external-service-url` | String | `@NotBlank` | Base URL of external service |
| `app.settings.contact-email` | String | `@NotBlank` `@Email` | Contact email in API metadata |
| `app.settings.debug-mode` | boolean | — | Adds extra diagnostic fields in dev |

These are validated at startup — the app will refuse to start if any value is invalid.

---

## Internationalization (i18n)

The following endpoints and error responses are fully localized based on the `Accept-Language` request header. Supported languages are **English** (`en`) and **Georgian** (`ka`).

| Endpoint / Case | What is localized |
|---|---|
| `GET /api/info` | Welcome message |
| `POST /api/auth/register` | Success message |
| DTO validation errors | Field error messages |
| All exception responses | Error message body |

### Testing i18n

```bash
# English
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

Logs are written to both the console and a file.

**Log file location:**
```
logs/app.log
```

Archived logs are stored as:
```
logs/app-yyyy-MM-dd.N.log.gz
```

| Profile | Root level | App package | Spring Security |
|---------|-----------|-------------|-----------------|
| `dev` | INFO | DEBUG | DEBUG |
| `prod` | WARN | INFO | inherited |

Rolling policy: 10 MB max file size, 30 days history, 500 MB total cap.

---

## Useful URLs (dev)

```
http://localhost:8080/           → Home
http://localhost:8080/login      → Login
http://localhost:8080/register   → Register
http://localhost:8080/dashboard  → Dashboard
http://localhost:8080/api/info   → API metadata
http://localhost:8080/api/auth/ping → Health check
http://localhost:8080/h2-console → H2 database browser
```