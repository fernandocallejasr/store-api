# Stickerstore

A Spring Boot REST API for a stickers e-commerce store.
Built with Spring Boot 4.0.6, Java 25, JPA + Flyway, runs on H2 (dev) or Postgres (prod).

## Quick start (in-memory H2)
```
./mvnw spring-boot:run
```

H2 console at http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:testdb`, user `sa`, no password).
Schema and seed data are managed by Flyway (`src/main/resources/db/migration/`).

## Run against Postgres (`prod` profile)

A `compose.yaml` is provided at the project root. To start a local Postgres and run the app against it:

### Option A — load from .env
```
docker compose up -d
cp .env.example .env       # edit if you change defaults
set -a; source .env; set +a
./mvnw spring-boot:run
```

### Option B — inline
```
docker compose up -d
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://localhost:5432/stickerstore \
DB_USERNAME=stickerstore \
DB_PASSWORD=stickerstore \
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:4173 \
./mvnw spring-boot:run
```

Flyway applies migrations on first startup and skips already-applied ones on subsequent boots.
`application-prod.properties` reads all values from environment variables — no secrets committed.

## Verify it's running

Once the app is up (either mode), hit the products endpoint:
```
curl http://localhost:8080/api/v1/products
```
You should get back the 29 seeded stickers as JSON.

## Tests
```
./mvnw clean test
```