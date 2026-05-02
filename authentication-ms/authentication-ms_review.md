# Authentication Microservice (authentication-ms)

## a. What does this service do?
This microservice acts as the **Identity Provider (IdP) and Session Manager** for the broader microservice ecosystem. Its single responsibility is to securely identify users, manage their roles, and issue cryptographic proofs of identity (JSON Web Tokens) that other microservices can trust.

Technically, it utilizes a stateless authentication architecture. Instead of storing user sessions in server memory, it issues a short-lived **Access Token** (JWT) for immediate authorization and a long-lived **Refresh Token** (stored securely in an HttpOnly cookie) to maintain the user's login state without compromising security.

## b. Supported Functionality
* **Role-Based Access Control (RBAC):** Supports distinct user roles (`PATIENT`, `DOCTOR`, `ADMINISTRATOR`).
* **Dual-Role Support:** Employs composite database unique constraints, allowing a single human (same name and email) to operate entirely separate accounts if they hold different roles (e.g., a Doctor who is also a Patient).
* **Secure Credential Storage:** Passwords are mathematically hashed using BCrypt before database persistence; plain text passwords are never stored.
* **XSS & CSRF Protection:** Mitigates Cross-Site Scripting (XSS) by burying the Refresh Token in an `HttpOnly`, `SameSite=Strict` cookie that frontend JavaScript cannot read.
* **Schema Versioning:** Uses Flyway migrations to ensure the PostgreSQL database schema is built and updated reliably across all deployment environments.
* **Centralized Exception Handling:** Intercepts runtime and validation errors, translating them into standard, readable JSON error responses.

## c. API Endpoints

The service exposes the following public endpoints under the `/api/auth` prefix:

* **`POST /register`**
    * **Purpose:** Creates a new user account.
    * **Payload:** Requires `name`, `email`, `password` (with strict regex validation), and `role`.
    * **Response:** Saves the user, returns an Access Token in the JSON body, and attaches a Refresh Token via a `Set-Cookie` header.
* **`POST /login`**
    * **Purpose:** Authenticates an existing user.
    * **Payload:** Requires `name`, `password`, and `role` (to resolve dual-role ambiguities).
    * **Response:** Verifies credentials, returns an Access Token, and sets the secure Refresh Token cookie.
* **`POST /refresh`**
    * **Purpose:** Mints a fresh Access Token when the old one expires.
    * **Payload:** Automatically reads the `refreshToken` from the HTTP cookie.
    * **Response:** Validates the token against the database and returns a new Access Token.
* **`POST /logout`**
    * **Purpose:** Terminates the user's session.
    * **Payload:** Automatically reads the `refreshToken` from the HTTP cookie.
    * **Response:** Deletes the refresh token from the PostgreSQL database (revoking future access) and commands the browser to instantly delete the cookie.

## d. How to run this service independently

To run this microservice, you need a running instance of **PostgreSQL**.

### Preconfigurations (Environment Variables)
If running via the `stand` (production) profile, the application requires the following environment variables:
* `DB_URL` (e.g., `jdbc:postgresql://postgres:5432/miro_auth_db`)
* `DB_USERNAME`
* `DB_PASSWORD`
* `JWT_SECRET` (A strong, base64-encoded secret key for signing tokens)

### Running Locally (Development)
The `dev` profile defaults to `localhost:5432` and uses hardcoded development credentials.

1. Start a local PostgreSQL database (e.g., via Docker):
   ```bash
   docker run --name auth_postgres -e POSTGRES_USER=erik.g -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=miro_auth_db -p 5432:5432 -d postgres:15-alpine
   ```

2. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Running via Docker (Production)
1. Build the Docker image:
   ```bash
   docker build -t authentication-ms .
   ```

2. Run the container, passing in the required environment variables:
   ```bash
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=stand \
     -e DB_URL=jdbc:postgresql://<your-db-host>:5432/miro_auth_db \
     -e DB_USERNAME=<user> \
     -e DB_PASSWORD=<password> \
     -e JWT_SECRET=<your-very-long-secret-key> \
     authentication-ms
   ```

## e. Summary
The `authentication-ms` provides a robust, stateless security perimeter for the application ecosystem. By offloading session management to the client via JWTs while maintaining strict revocation control via database-backed refresh tokens, it strikes an optimal balance between microservice scalability and strict security protocols.