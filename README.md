# 💰 Expense Tracker API

A REST API built with **Spring Boot 3** where users register, log in with **JWT**, and manage their own expenses: create, update, delete, filter by category and date, and get a monthly summary by category.

**Tech:** Java 17 · Spring Boot · Spring Security (JWT) · Spring Data JPA · H2 / MySQL · Bean Validation · Swagger (OpenAPI) · JUnit 5 + Mockito

## Features
- Register / login with BCrypt-hashed passwords and stateless JWT auth
- Full CRUD on expenses; users can only access **their own** data
- Filtering by category and date range, with pagination and sorting
- Monthly summary: total spent and totals per category
- Request validation and a global exception handler with consistent JSON errors
- Interactive API docs with Swagger UI
- Unit tests (service layer, Mockito) and an integration test (MockMvc)

## Run it
Requires Java 17+ and Maven.

```bash
mvn spring-boot:run          # uses in-memory H2, zero setup
mvn test                     # run the tests
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:expensedb`, user `sa`)

**MySQL:** create a database `expensedb`, then run with
`DB_URL=jdbc:mysql://localhost:3306/expensedb DB_USER=root DB_PASSWORD=... JWT_SECRET=<32+ chars> mvn spring-boot:run -Dspring-boot.run.profiles=prod`

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account, returns JWT |
| POST | `/api/auth/login` | Log in, returns JWT |
| POST | `/api/expenses` | Create expense |
| GET | `/api/expenses?category=FOOD&from=2026-10-01&to=2026-10-31&page=0&size=10&sort=amount,desc` | List (filter, paginate, sort) |
| GET | `/api/expenses/{id}` | Get one |
| PUT | `/api/expenses/{id}` | Update |
| DELETE | `/api/expenses/{id}` | Delete |
| GET | `/api/expenses/summary?month=2026-10` | Totals per category for a month |

All `/api/expenses` endpoints need `Authorization: Bearer <token>`.

```bash
# register, then use the returned token
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","password":"password123"}'

curl -X POST localhost:8080/api/expenses -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Lunch","amount":150.50,"category":"FOOD","expenseDate":"2026-10-12"}'
```

Categories: `FOOD, TRANSPORT, RENT, UTILITIES, ENTERTAINMENT, HEALTH, SHOPPING, OTHER`

## Design decisions
- **Layered architecture:** controller → service → repository. Controllers stay thin; business rules live in services.
- **DTOs (Java records)** instead of exposing entities, so the API shape is independent of the DB schema.
- **Ownership checks:** every lookup is `findByIdAndUserId`. Someone else's expense returns 404, so ids can't be probed.
- **`BigDecimal`** for money, never `double`.
- **Stateless JWT:** a filter validates the token on each request; no server sessions, so CSRF protection is disabled.
- **`open-in-view=false`** and explicit `@Transactional` boundaries.
- **Composite index** on `(user_id, expense_date)` for the most common query.

## Future improvements
Refresh tokens, roles (admin), recurring expenses, budgets with alerts, CSV export, Docker, Flyway migrations.
