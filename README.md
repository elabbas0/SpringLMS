# SpringLMS

SpringLMS is a university-style learning management system with a Spring Boot backend and a React frontend. It handles authentication, academic structure, schedules, attendance, courses, course materials, assignments, submissions, and grading.

## What It Does

SpringLMS provides role-based workflows for:

- **Admins**: manage faculties, teachers, specializations, student groups, global scheduling settings, and course sections
- **Teachers**: manage assigned groups, schedules, attendance sessions, materials, assignments, and grading
- **Students**: view schedules, attendance summaries, course materials, assignments, and submissions

The backend uses JWT authentication and PostgreSQL persistence. The frontend talks to the backend through `http://localhost:8080`.

## Why It's Useful

- Gives a single place to manage academic structure and teaching workflows
- Separates admin, teacher, and student permissions clearly
- Supports file attachments and downloadable course materials
- Covers attendance and assignment submission flows end to end
- Includes tests for the REST controllers and the admin seeder

## Project Layout

- `springlms/` - Spring Boot backend
- `frontend/` - Vite + React frontend
- `docker-compose.yml` - local PostgreSQL + backend stack

## Getting Started

### Prerequisites

- Java 21
- Docker Desktop
- Node.js and npm if you want to run the frontend locally

### Run With Docker

From the repository root:

```powershell
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5432`
- Backend on `http://localhost:8080`

Stop everything with:

```powershell
docker compose down
```

Reset the database volume with:

```powershell
docker compose down -v
docker compose up --build
```

### Run Backend Locally

From `springlms/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Default local database settings:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/springlms
SPRING_DATASOURCE_USERNAME=springlms
SPRING_DATASOURCE_PASSWORD=springlms
```

### Run Frontend Locally

From `frontend/`:

```bash
npm install
npm run dev
```

The frontend expects the backend at `http://localhost:8080`.

## Configuration

The backend reads these environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/springlms` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `springlms` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `springlms` | Database password |
| `APP_JWT_SECRET` | `change-this-dev-secret-before-production` | JWT signing secret |
| `APP_JWT_EXPIRATION_MS` | `43200000` | JWT lifetime in milliseconds |
| `APP_STORAGE_PATH` | `./uploads` | Local file storage path |

In Docker, uploads are stored in the `springlms-uploads` volume.

## Authentication

Login returns a JWT token. Use it for protected requests:

```http
Authorization: Bearer <token>
```

Example login request:

```json
{
  "email": "admin@example.com",
  "password": "your-password"
}
```

## API Docs

Swagger UI is available after the backend starts:

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The UI is grouped by role:

- `Auth`
- `Admin`
- `Teacher`
- `Student`

## Demo Data

The current codebase does **not** seed demo teachers, students, or course content.

Only the initial admin account is created automatically if no admin exists yet. The generated username, email, and password are printed to the terminal on first startup.

## Help and Documentation

- Backend source: [`springlms/src/main/java/com/springlms/backend/`](springlms/src/main/java/com/springlms/backend/)
- Backend tests: [`springlms/src/test/java/com/springlms/backend/`](springlms/src/test/java/com/springlms/backend/)
- Frontend source: [`frontend/src/`](frontend/src/)
- Frontend notes: [`frontend/README.md`](frontend/README.md)
- Docker setup: [`docker-compose.yml`](docker-compose.yml)

If you need a quick map of features, start with the controller classes in [`springlms/src/main/java/com/springlms/backend/controller/`](springlms/src/main/java/com/springlms/backend/controller/) and the matching pages in [`frontend/src/pages/`](frontend/src/pages/).

## Contributing

This repository is maintained by the project owner and contributors. If you change behavior, keep the corresponding backend tests updated and make sure the frontend still matches the API contract.

Please keep contributions focused and aligned with the existing structure in `springlms/` and `frontend/`.

## Verification

Useful backend checks:

```powershell
cd springlms
.\mvnw.cmd test
```

Useful frontend checks:

```bash
cd frontend
npm run build
```
