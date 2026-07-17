# AGENTS.md

## Project Overview

Java 11 Maven multi-module microservice project. Spring Boot 2.7.18 + Spring Cloud 2021.0.8 + Spring Cloud Alibaba 2021.0.5.0.

Three modules:
- **gateway-server** (port 8080) — Spring Cloud Gateway, routes by IP region
- **playlet-internal-server** (port 8081) — Main business service (internal/domestic)
- **playlet-oversea-server** (port 8082) — Overseas business service

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

No tests exist in any module (`src/test/` is empty or absent). No CI/CD pipelines. No Dockerfile.

## Architecture

### Routing (gateway-server)

Gateway routes all traffic via Nacos service discovery:

| Path Pattern | Target Service | StripPrefix |
|---|---|---|
| `/china/admin/**` | playlet-internal-server | 2 |
| `/global/admin/**` | playlet-oversea-server | 2 |
| `/entrance/**` | playlet-internal-server | 1 |

**Note**: The ToC API route is `/entrance/**`, not `/api/**`.

IP-based routing (`playlet.gateway.route-mode`):
- `1` = always domestic (internal-server)
- `2` = always overseas (oversea-server)
- `3` = IP region check via `ip-api.com` (default)

The `IpRegionRouteFilter` (`com.playlet.gateway.filter`) implements this as a `GlobalFilter` with order `-100`. It bypasses region check for `/china/admin/` and `/global/admin/` paths. Internal IPs (127.0.0.1, 192.168.*, 10.*, 172.*) always route domestic.

### Security & Authentication

**JWT login**: POST `/login` with JSON body `{"username": "x", "password": "y", "googleCode": "123456"}`. No separate login controller — handled by `JWTLoginFilter` extending `UsernamePasswordAuthenticationFilter`.

**Token**: Returned in `x-playlet-token` header (format: `Bearer <token>`). Verify via `x-playlet-token` header, NOT `Authorization`. Token expiry: 24h, stored in Redis with key prefix `playletInternalServer`.

**Google Authenticator 2FA**: Always required for login — the `googleCode` field must be present in the login request. The `dscoins.login.googleLimit.enable` flag controls whether the code is actually *verified* (currently `false` = skip verification but field still required).

**Rate limiting**: `GlobalRateLimitFilter` enforces 3 requests/sec per IP+URI. Runs before JWT filters.

**Security config caveat**: `WebSecurityConfig` has `.antMatchers("/**").permitAll()` at the top, but JWT filters still add authentication headers. Swagger, actuator, and static assets are explicitly permitted.

### Databases

- **Internal server**: MySQL `playlet_internal` on localhost:3306
- **Oversea server**: MySQL `playlet_oversea` on localhost:3306

Both use Druid connection pool + MyBatis-Plus + PageHelper. MapperScan: `com.playlet.internal.dao` (internal) / `com.playlet.oversea.dao` (oversea).

**Note**: The oversea-server does NOT have Qiniu file upload config — only the internal-server has it.

### Dependencies (playlet-internal-server)

- **Data**: MySQL 8.0.33 + MyBatis-Plus 3.1.0 + Druid 1.1.8 + PageHelper 1.4.7
- **Cache**: Redis (Jedis via spring-boot-starter-data-redis)
- **Security**: Spring Security + JWT (jjwt 0.11.5 + java-jwt 3.8.3)
- **API Docs**: SpringDoc OpenAPI 1.7.0 (Swagger UI at `/swagger-ui.html`)
- **File Upload**: Qiniu SDK 7.13.0 (max 255MB)
- **Utilities**: Hutool 5.6.0, FastJSON 1.2.30, OkHttp3, Lombok

### Package Structure (playlet-internal-server)

```
com.playlet.internal
├── aop/           # Custom annotations (@ApiIdempotent, @AccessLimit, @SysLogAnnotation, @ExcelAnnotation)
├── aspect/        # AOP aspects (WebLogAspect, LimitSubmitAspect)
├── base/          # Response wrappers (JsonData, ResponseBase, BaseApiService)
├── config/        # Spring configs (Security, Redis, Swagger, I18n, Async, Qiniu)
├── constants/     # App constants (Constants.SIGNING_KEY has hardcoded JWT secret)
├── dao/           # MyBatis mappers (MapperScan on com.playlet.internal.dao)
├── entity/        # JPA/MyBatis entities
├── enums/         # Business enums
├── exception/     # Custom exceptions
├── exceptionHandler/ # Global exception handlers
├── filter/        # JWT filters (JWTLoginFilter + JWTAuthenticationFilter + GlobalRateLimitFilter)
├── handler/       # Security handlers
├── query/         # Query DTOs
├── scheduled/     # Scheduled tasks
├── security/      # UserDetailsService, AuthenticationProvider
├── service/       # Business logic AND REST controllers (see below)
└── utils/         # Utility classes
```

The oversea-server (`com.playlet.oversea`) mirrors this structure with the same package names but different base package.

**Unusual pattern: Service-as-Controller** — Service interfaces (e.g. `SysUserService`) define REST endpoints via `@RequestMapping`. Their implementations (e.g. `SysUserServiceImpl`) are annotated with `@RestController` and `@CrossOrigin`. There is no separate `controller/` package — the service layer directly exposes REST APIs.

**MyBatis uses annotation-based SQL** — Mappers extend `BaseMapper<T>` and use `@Select`/`@Insert`/`@Update`/`@Delete` annotations directly. No XML mapper files exist.

## Gotchas

- **Credentials in config**: `application.yml` contains hardcoded DB credentials and Qiniu keys — treat as reference, not production values.
- **Nacos required**: All services need Nacos at `16.163.6.237:8848` (namespace `9d71b331-4907-4e19-b982-fc0b121330bc`) to start.
- **Redis required**: Both business servers require Redis on localhost:6379.
- **MyBatis mapper scan**: New mappers must be in `com.playlet.internal.dao` (internal) or `com.playlet.oversea.dao` (oversea).
- **JWT signing key**: Hardcoded in `Constants.SIGNING_KEY` — not externalized to config.
- **File upload limit**: Max 255MB (`spring.servlet.multipart.max-file-size`).
- **Eclipse project**: `.project` and `.settings/` indicate Eclipse IDE usage.
- **Lombok required**: Both servers use Lombok — ensure IDE has Lombok plugin installed.
