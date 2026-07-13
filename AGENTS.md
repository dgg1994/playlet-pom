# AGENTS.md

## Project Overview

Java 11 Maven multi-module microservice project. Spring Boot 2.7.18 + Spring Cloud 2021.0.8 + Spring Cloud Alibaba 2021.0.5.0.

Three modules:
- **gateway-server** (port 8080) — Spring Cloud Gateway, routes by IP region
- **playlet-internal-server** (port 8085) — Main business service (internal/domestic)
- **playlet-oversea-server** (port 8086) — Overseas business service (minimal)

## Build & Run

```bash
# Build all modules (from root)
mvn clean package -DskipTests

# Build single module
mvn clean package -DskipTests -pl gateway-server
mvn clean package -DskipTests -pl playlet-internal-server
mvn clean package -DskipTests -pl playlet-oversea-server

# Run (from module directory after build)
java -jar target/gateway-server-0.0.1-SNAPSHOT.jar
java -jar target/playlet-internal-server-0.0.1-SNAPSHOT.jar
java -jar target/playlet-oversea-server-0.0.1-SNAPSHOT.jar
```

No tests exist in any module (`src/test/` is empty or absent).

## Architecture

### Routing (gateway-server)

Gateway routes all traffic via Nacos service discovery:

| Path Pattern | Target Service | StripPrefix |
|---|---|---|
| `/china/admin/**` | playlet-internal-server | 2 |
| `/global/admin/**` | playlet-oversea-server | 2 |
| `/api/**` | IP-based routing (see below) | 1 |

IP-based routing (`playlet.gateway.route-mode`):
- `1` = always domestic (internal-server)
- `2` = always overseas (oversea-server)
- `3` = IP region check via `ip-api.com` (default)

The `IpRegionRouteFilter` (`com.playlet.gateway.filter`) implements this as a `GlobalFilter`. It bypasses region check for `/china/admin/` and `/global/admin/` paths. Internal IPs (127.0.0.1, 192.168.*, 10.*, 172.*) always route domestic.

### Key Dependencies (playlet-internal-server)

- **Data**: MySQL + MyBatis-Plus + Druid connection pool + PageHelper
- **Cache**: Redis (Jedis)
- **Security**: Spring Security + JWT (jjwt 0.11.5 + java-jwt 3.8.3)
- **API Docs**: SpringDoc OpenAPI 1.7.0 (Swagger UI at `/swagger-ui.html`)
- **Utilities**: Hutool 5.6.0, FastJSON 1.2.30, OkHttp3

### Package Structure (playlet-internal-server)

```
com.playlet.internal
├── aop/           # Custom annotations (@ApiIdempotent, @AccessLimit, @SysLogAnnotation)
├── aspect/        # AOP aspects (WebLogAspect, LimitSubmitAspect)
├── base/          # Response wrappers (JsonData, ResponseBase, BaseApiService)
├── config/        # Spring configs (Security, Redis, Swagger, I18n, Async)
├── constants/     # App constants
├── controller/    # REST controllers
├── dao/           # MyBatis mappers (MapperScan on com.playlet.internal.dao)
├── entity/        # JPA/MyBatis entities
├── enums/         # Business enums
├── exception/     # Custom exceptions
├── exceptionHandler/ # Global exception handlers
├── filter/        # JWT filters (login + authentication)
├── handler/       # Security handlers
├── query/         # Query DTOs
├── scheduled/     # Scheduled tasks
├── security/      # UserDetailsService, AuthenticationProvider
├── service/       # Business logic
└── utils/         # Utility classes
```

## Gotchas

- **No tests**: No test suite exists. Don't assume test coverage.
- **No CI/CD**: No GitHub Actions, Jenkins, or Dockerfile found.
- **Credentials in config**: `application.yml` contains hardcoded DB credentials — treat as reference, not production values.
- **Nacos required**: All services need Nacos at `16.163.6.237:8848` (namespace `9d71b331-...`) to start.
- **Redis required**: `playlet-internal-server` requires Redis on localhost:6379.
- **MySQL required**: `playlet-internal-server` requires MySQL on localhost:3306, database `mergepay`.
- **Eclipse project**: `.project` and `.settings/` indicate Eclipse IDE usage.
- **MyBatis mapper scan**: `@MapperScan("com.playlet.internal.dao")` — new mappers must be in this package.
- **File upload limit**: Max 255MB (`spring.servlet.multipart.max-file-size`).
- **Google Authenticator 2FA**: Login requires Google Authenticator code. The `googleLimit` flag in `application.yml` controls whether it's enforced (currently disabled: `false`).
- **JWT signing key**: Hardcoded in `Constants.SIGNING_KEY` — not externalized to config. Token expiry is 24h for JWT, stored in Redis.
- **Login endpoint**: `/login` (POST, JSON body with `username`, `password`, `googleCode`). No separate login controller — handled by `JWTLoginFilter` extending `UsernamePasswordAuthenticationFilter`.
