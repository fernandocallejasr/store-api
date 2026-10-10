# Stickerstore

A Spring Boot REST API for a stickers e-commerce store.
Built with Spring Boot 4.1.0, Java 25, JPA + Flyway, runs on H2 (dev) or Postgres (prod).

> **`JWT_SECRET` is required.** It's the key the app uses to sign and verify login tokens. 
> It must be at least 32 bytes, and the app refuses to start without it. Generate one with `openssl rand -base64 48`. 
> Changing the secret invalidates every token issued with the old one.

## Quick start with random JWT SECRET (in-memory H2)
```
JWT_SECRET="$(openssl rand -base64 48)" ./mvnw spring-boot:run
```

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

`.env.example` leaves `JWT_SECRET` empty on purpose. Set it in `.env` (for example with `openssl rand -base64 48`) before starting, or the app won't start.

### Option B — inline
```
docker compose up -d
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://localhost:5432/stickerstore \
DB_USERNAME=stickerstore \
DB_PASSWORD=stickerstore \
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:4173 \
JWT_SECRET="$(openssl rand -base64 48)" \
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

## Authentication

The product endpoints are public; for anything else, log in to get a JWT (valid for 1 hour) and send it as `Authorization: Bearer <token>`.

| Method | Path                           | Auth         | Purpose                                                         |
|--------|--------------------------------|--------------|-----------------------------------------------------------------|
| `GET`  | `/api/v1/products`             | Public       | List all products                                               |
| `GET`  | `/api/v1/products/{productId}` | Public       | Get one product by ID                                           |
| `POST` | `/api/v1/auth/register`        | Public       | Create an account (`name`, `email`, `mobileNumber`, `password`) |
| `POST` | `/api/v1/auth/login`           | Public       | Log in with `username` (the email) and `password`               |
| `GET`  | `/api/v1/profile`              | Bearer token | Get your profile (`userId`, `name`, `email`, `mobileNumber`)    |
| `GET`  | `/actuator/metrics`            | Bearer token | List the available metrics (dev profile only)                   |

Register returns `201`. A request body that fails validation returns `400`, with the messages for each field under `errors`, 
and an email or mobile number that's already registered returns `409`. 
Login returns the token (`jwtToken`) and the `user`. A wrong email and a wrong password get the same `401`.
The profile is read from the database on every call, so changes to the account show up right away, even with a token issued before them.
If the account no longer exists, the profile returns `404`.

## Observability

A local Prometheus + Grafana stack lives in `compose.yaml` under the `observability` profile, so `docker compose up` keeps starting only Postgres. Spring Boot Actuator + Micrometer expose the metrics.

### Start the stack
```
docker compose --profile observability up -d
JWT_SECRET="$(openssl rand -base64 48)" ./mvnw spring-boot:run    # in another terminal
```

| Service          | URL                                                | Notes                                            |
|------------------|----------------------------------------------------|--------------------------------------------------|
| Liveness probe   | http://localhost:8080/actuator/health/liveness     | JVM-only check                                   |
| Readiness probe  | http://localhost:8080/actuator/health/readiness    | includes the database                            |
| Prometheus       | http://localhost:9090                              | scrapes the app every 15s                        |
| Grafana          | http://localhost:3000                              | login `admin` / `admin`; one dashboard auto-provisioned |

Actuator's `health` (including the probes), `info` and `prometheus` endpoints are only served with the dev profile, so Prometheus can only scrape the app while it runs with dev. `/actuator/metrics` also needs a Bearer token (see [Authentication](#authentication)). The prod profile keeps actuator fully closed.

The `ProductService` is instrumented with two custom Micrometer meters: `products.list` (timer) and `products.notfound` (counter), visible at `/actuator/prometheus`.

### Wipe observability state
Both Prometheus and Grafana use `tmpfs`, so a container restart wipes them. To fully tear down without touching Postgres:
```
docker compose rm -fsv prometheus grafana
```

## Tests
```
./mvnw clean test
```
