# Dynamic Clinic Hub

## 1. Authentication Microservice (`authentication-ms`)

### Overview & Responsibility
In accordance with the microservices architecture requirement, the `authentication-ms` acts as the centralized **Identity Provider (IdP) and Session Manager** for the system. Its single, well-defined responsibility is to securely authenticate users, manage their specific roles, and issue cryptographic proofs of identity (JSON Web Tokens) that other microservices in the ecosystem can trust.

This service directly fulfills the **Optional Enhancement (Bonus)** for Authentication by protecting APIs with JWTs and establishing Role-Based Access Control (RBAC).

### Key Features
* **Role-Based Access Control (RBAC):** Supports three distinct user roles (`PATIENT`, `DOCTOR`, `ADMINISTRATOR`), satisfying the project constraints for multiple user types with distinct needs.
* **Stateless JWT Authentication:** Issues short-lived Access Tokens (JWT) for immediate authorization and long-lived Refresh Tokens (stored securely in an `HttpOnly`, `SameSite=Strict` cookie) to maintain login state while mitigating XSS attacks.
* **Secure Persistence:** Uses **PostgreSQL** with Spring Data JPA. Passwords are never stored in plain text; they are mathematically hashed using `BCrypt`.
* **Database Versioning:** Utilizes **Flyway** migrations (`V1__create_auth_tables.sql`, etc.) to ensure reliable schema creation and prevent schema drift.
* **Observability & Custom Metrics:** Exposes a `/actuator/prometheus` endpoint. It tracks custom business metrics such as `auth_users_created_total` (tagged by role) and `auth_login_attempts_total` (tagged by success/failure) to monitor system health and detect brute-force attacks.

### REST API Endpoints
The API is fully documented via Swagger/OpenAPI (`/swagger-ui/index.html`) and enforces strict payload validation.

* `POST /api/auth/register` - Registers a new user with a specific role.
* `POST /api/auth/login` - Authenticates credentials and returns a JWT & Refresh Token cookie.
* `POST /api/auth/refresh` - Consumes the HttpOnly cookie to issue a new Access Token.
* `POST /api/auth/logout` - Revokes the user's Refresh Token in the database and clears the browser cookie.

### Docker & Configuration
The service is fully containerized and adheres to the twelve-factor app methodology by externalizing all configuration. There are no hardcoded secrets in the `Dockerfile`.

When running via `docker compose up` or Kubernetes, the following environment variables must be injected:
* `SPRING_PROFILES_ACTIVE` (e.g., `stand`)
* `DB_URL` (e.g., `jdbc:postgresql://postgres:5432/miro_auth_db`)
* `DB_USERNAME`
* `DB_PASSWORD`
* `APP_JWT_SECRET` (Base64 encoded secret for signing JWTs)