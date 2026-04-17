# Rate Limiter API

A per-user, per-endpoint sliding window rate limiter with API key authentication, built with Spring Boot, Java 17, and PostgreSQL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Project Structure

```
com.nithish.ratelimiter
├── apikey/
│   ├── ApiKeyGenerator.java       # Generates secure URL-safe Base64 keys (32 random bytes)
│   └── ApiKeyStore.java           # Creates, validates, and revokes API keys via DB
├── config/
│   └── RateLimitConfig.java       # Reads ratelimiter.limit and ratelimiter.window-minutes
├── controller/
│   ├── AdminController.java       # GET /admin/stats — live usage stats
│   ├── ApiKeyController.java      # POST /apikey/generate, DELETE /apikey/revoke
│   └── RateLimiterController.java # GET /api/test — sample protected endpoint
├── dto/
│   ├── ApiKeyResponse.java        # { success, apiKey }
│   ├── ApiRequestDTO.java         # { email, endpoint, ip }
│   ├── ApiResponse.java           # { allowed, message, remaining, limit, resetTime }
│   └── RateLimitResult.java       # Internal result from the rate limiter service
├── entity/
│   └── ApiKey.java                # JPA entity: key (PK), username, expiry
├── exceptionhandler/
│   └── GlobalExceptionHandler.java # Handles validation errors and generic exceptions
├── filter/
│   └── RateLimiterFilter.java     # OncePerRequestFilter — intercepts all protected routes
├── repository/
│   └── ApiKeyRepository.java      # JpaRepository<ApiKey, String>
└── service/
    ├── RateLimiterService.java         # Interface: allow(ApiRequestDTO)
    └── implementation/
        └── RateLimiterServiceImpl.java # Sliding window logic with ConcurrentHashMap
```

---

## Configuration

All settings live in `src/main/resources/application.properties`:

```properties
# Rate limit settings
ratelimiter.limit=5
ratelimiter.window-minutes=1
cleanup.interval=60000

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/rate_limiter_db
spring.datasource.username=postgres
spring.datasource.password=123
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

---

## API Endpoints

### API Key Management — `/apikey` (no authentication required)

#### Generate an API Key
```
POST /apikey/generate?email={email}
```
**Response:**
```json
{
  "success": true,
  "apiKey": "abc123..."
}
```
Keys expire after **30 minutes** and are persisted to the `api_key` table in PostgreSQL.

---

#### Revoke an API Key
```
DELETE /apikey/revoke?key={key}
```
**Response:** `"API key revoked"` (plain text)

---

### Protected Routes — `/api` (requires `X-API-KEY` header)

#### Test Endpoint
```
GET /api/test
Headers: X-API-KEY: <your-key>
```

**Success (200):**
```json
{
  "allowed": true,
  "message": "Request successful"
}
```

**Rate limited (429):**
```json
{
  "allowed": false,
  "message": "Rate limit exceeded",
  "remaining": 0,
  "limit": 5,
  "resetTime": 42
}
```

**Unauthorized (401):** Missing or invalid API key.

**Response Headers (all protected routes):**

| Header | Description |
|---|---|
| `X-Rate-Limit-Limit` | Max requests allowed per window |
| `X-Rate-Limit-Remaining` | Requests remaining in the current window |
| `X-Rate-Limit-Reset` | Seconds until the window resets |

---

### Admin — `/admin` (no authentication, filter-bypassed)

#### Get Stats
```
GET /admin/stats
```
**Response:**
```json
{
  "activeUsers": 3,
  "totalRequests": 12
}
```
`activeUsers` is the number of distinct `email:endpoint:ip` keys currently tracked in memory. `totalRequests` is the total number of timestamps across all windows.

---

## How Rate Limiting Works

The service uses a **sliding window algorithm** backed by a `ConcurrentHashMap<String, List<Long>>`.

**Composite key:** `email:endpoint:ip`

**On every request:**
1. Prune timestamps older than `windowMinutes` from the list.
2. If `count >= limit` → return `allowed: false` with a `429`.
3. Otherwise, append the current timestamp and return `allowed: true`.

**Reset time** is derived from the oldest timestamp still in the window:
```
resetTime = (windowMillis - (now - oldest)) / 1000
```

> **Note:** The in-memory map is not persisted. Rate limit state resets on server restart.

---

## Request Lifecycle

```
HTTP Request
    │
    ▼
RateLimiterFilter (OncePerRequestFilter)
    │
    ├── Skip? → /apikey/**, /admin/**, /swagger-ui/**, /v3/api-docs/**
    │
    ├── Missing X-API-KEY → 401 Unauthorized
    │
    ├── ApiKeyStore.isValid(key)
    │       ├── Not found → 401
    │       └── Expired → delete from DB → 401
    │
    ├── Build ApiRequestDTO { email, endpoint, ip }
    │
    ├── RateLimiterServiceImpl.allow(dto)
    │       ├── Over limit → 429 + JSON ApiResponse
    │       └── Allowed → set X-Rate-Limit-* headers
    │
    └── filterChain.doFilter() → Controller
```

---

## Running the Application

**Prerequisites:**
- Java 17+
- Maven 3.8+
- PostgreSQL running on `localhost:5432` with a database named `rate_limiter_db`

**Steps:**

```bash
# 1. Clone the repo
git clone <repo-url>
cd rate-limiter

# 2. Create the database
psql -U postgres -c "CREATE DATABASE rate_limiter_db;"

# 3. Run the app (Hibernate will auto-create tables)
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.  
Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

---

## Example Usage

```bash
# 1. Generate an API key
curl -X POST "http://localhost:8080/apikey/generate?email=user@example.com"

# 2. Use the key to call a protected endpoint
curl -H "X-API-KEY: <your-key>" http://localhost:8080/api/test

# 3. Check admin stats
curl http://localhost:8080/admin/stats
```

---

## Known Limitations & Improvements

| Area | Current State | Suggested Improvement |
|---|---|---|
| Rate limit state | In-memory (lost on restart) | Use Redis for persistent, distributed state |
| Admin security | No authentication | Add Spring Security or a shared secret |
| Key expiry cleanup | `cleanup.interval` is defined but not yet wired to `@Scheduled` | Implement a scheduled task to prune expired keys |
| API key storage | Plaintext key as DB primary key | Store a hashed key for security |

---

## Author

**Nithish** — `com.nithish.ratelimiter`
