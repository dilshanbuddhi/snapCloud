# AGENTS.md (SnapCloud)

This repo contains the backend for a SaaS Image Hosting Platform (Cloudinary/ImgBB-like) built with **Java 17 + Spring Boot 3**.

## Quick Start

### Prereqs
- Java: **17+**
- Maven: **3.9+**
- DB: PostgreSQL (default) or MySQL (supported by swapping driver)

### Build / Run
- Build (no tests): `mvn -DskipTests package`
- Run locally: `mvn spring-boot:run`

### Test
- Run all tests: `mvn test`
- Run a single test class: `mvn -Dtest=SnapCloudApplicationTests test`
- Run a single test method (Surefire): `mvn -Dtest=SnapCloudApplicationTests#contextLoads test`

### Lint / Format
This project currently does **not** include a formatter/linter plugin (like Spotless/Checkstyle) yet.
If you add one, prefer:
- Spotless (Google Java Format) for formatting
- Checkstyle/PMD for lint rules

### Common Maven tasks
- Dependency tree: `mvn -DskipTests dependency:tree`
- Clean build: `mvn clean package`

## Architecture Notes

### Auth model
- Dashboard: JWT authentication (Spring Security)
- API upload endpoints: API Key authentication
- API keys should be stored **hashed** (BCrypt) and compared using BCrypt match.

### Storage model
- Images are stored in AWS S3 (private bucket).
- S3 object key convention:
  - `users/{userId}/{imageUuid}.{ext}`
- Backend enforces quotas based on per-user totals, not separate S3 accounts.

### Subscription model
- FREE plan with limited storage.
- Upgrade via Stripe checkout.
- Stripe webhooks update subscription state; on plan upgrades we revoke old keys and issue new keys.

## Code Style Guidelines

### General Java/Spring conventions
- Java: target **Java 17** language features.
- Keep classes small and single-responsibility.
- Prefer constructor injection over field injection.
- Avoid `@Autowired` on fields.

### Packages
- Base package: `com.snapcloud.api`
- Typical layout (suggested):
  - `com.snapcloud.api.domain` (JPA entities)
  - `com.snapcloud.api.repository` (Spring Data repositories)
  - `com.snapcloud.api.service` (business logic)
  - `com.snapcloud.api.web` (controllers + DTOs)
  - `com.snapcloud.api.security` (JWT + API key filters)
  - `com.snapcloud.api.config` (Spring configuration)

### Imports
- Use explicit imports (no wildcard `*`).
- Order imports: java.* / jakarta.* / org.* / com.*.
- Prefer `java.time.Instant/LocalDate/OffsetDateTime` over `java.util.Date`.

### Formatting
- 2-space indentation is used in current files.
- Keep line length reasonable (~120).
- Use `@Column(...)` and other JPA annotations on fields (field access).

### Naming
- Classes: `PascalCase`
- Methods/fields/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Database tables: plural snake case (e.g. `users`, `api_keys`).

### Types / IDs
- Entity primary keys: `Long` (IDENTITY)
- Public image identifier: `UUID` stored as `public_id` and exposed in URLs.
- Monetary amounts: `BigDecimal`.
- Storage sizes: `long` in **bytes** (`storageLimitBytes`, `sizeBytes`).

### Entities and relationships
- Default fetch type:
  - `@ManyToOne` should be `LAZY` (explicitly set)
  - `@OneToMany` should be `LAZY` (default) + avoid eager loading
- Keep bidirectional relationships minimal; avoid recursion in JSON.
- Don’t expose entities directly from REST controllers; use DTOs.

### Error handling
- Use a global exception handler via `@ControllerAdvice`.
- Map domain errors to appropriate HTTP status:
  - 400 validation errors
  - 401 unauthenticated
  - 403 insufficient quota / forbidden
  - 404 not found
  - 409 conflicts (duplicate email, etc.)
- Never leak secrets (API keys, JWTs, Stripe secret, S3 credentials) into logs.

### Validation
- Use `jakarta.validation` annotations on DTOs (`@NotNull`, `@Email`, `@Size`).
- Validate file upload:
  - allowlist MIME types
  - size limits
  - reject unknown/unsafe extensions

### Security
- API key:
  - store only `keyHash` (BCrypt)
  - transmit only once at creation (never return stored hash)
  - support revocation (`active=false`, `revokedAt`)
- JWT:
  - short-lived access tokens
  - refresh tokens (if implemented) must be stored securely
- Rate limiting: plan-based throttling per API key (if implemented).

### Stripe webhook safety
- Verify Stripe signature header.
- Treat webhook events as source of truth for subscriptions.
- Make webhook handlers idempotent (based on event id / payment intent id).

### AWS S3 safety
- Use server-side encryption (SSE-S3 or SSE-KMS).
- Bucket must be private; generate signed URLs (if needed).

## Repo-specific Notes

### Existing domain model (implemented)
- Entities are in `src/main/java/com/snapcloud/api/domain`:
  - `User`, `Plan`, `Subscription`, `ApiKey`, `Image`, `Usage`, `Payment`
- Enums: `src/main/java/com/snapcloud/api/domain/enums`

### Configuration
- Default config is in `src/main/resources/application.yml`.
- `spring.jpa.hibernate.ddl-auto=update` is for development only; switch to migrations (Flyway/Liquibase) for production.

## Cursor / Copilot Rules
- No `.cursor/rules/`, `.cursorrules`, or `.github/copilot-instructions.md` files were found in this repo.
