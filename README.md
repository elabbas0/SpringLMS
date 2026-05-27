# SpringLMS

SpringLMS, a university-style student information and learning management system. Handles users, faculties, specializations, student groups, schedules, attendance, course materials, assignments, submissions, and grading.


## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Security | Spring Security, JWT |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven Wrapper |
| Runtime | Docker Compose |

## Requirements

- JDK 21
- Docker Desktop
- Git

Check your jdk ver:

```powershell
java -version
javac -version
```

Both should show jdk 21.

## Quick Start With Docker

From the project root:

```powershell
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5432`
- SpringLMS backend on `http://localhost:8080`
- Addentionally the frontend can be run on `http://localhost:3000`, though it's minimal.

The database is stored in a Docker volume named `springlms-postgres-data`.
To stop:

```powershell
docker compose down
```

To reset all database data:

```powershell
docker compose down -v
docker compose up --build
```

## Run Backend Locally

If PostgreSQL is already running, start the backend directly:

```powershell
cd springlms
.\mvnw.cmd spring-boot:run
```

Default local database config:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/springlms
SPRING_DATASOURCE_USERNAME=springlms
SPRING_DATASOURCE_PASSWORD=springlms
```

You can override these environment variables before running the app.

## Environment Variables

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/springlms` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `springlms` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `springlms` | Database password |
| `APP_JWT_SECRET` | `change-this-dev-secret-before-production` | JWT signing secret |
| `APP_JWT_EXPIRATION_MS` | `43200000` | JWT lifetime in milliseconds |

## Database Notes

The project uses PostgreSQL only.

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

That is useful for development. Can be removed for prod.

## Seeded Demo Data

On startup, demo data is created if it does not already exist.

Teacher accounts:

| Email | Password |
|---|---|
| `teacher.demo1@springlms.local` | `Demo1234` |
| `teacher.demo2@springlms.local` | `Demo1234` |
| `teacher.demo3@springlms.local` | `Demo1234` |
| `teacher.demo4@springlms.local` | `Demo1234` |
| `teacher.demo5@springlms.local` | `Demo1234` |

Student accounts follow this pattern:

| Example Email | Password |
|---|---|
| `student.6525a1.01@springlms.local` | `Demo1234` |
| `student.6525a2.01@springlms.local` | `Demo1234` |
| `student.6525a3.01@springlms.local` | `Demo1234` |
| `student.6525a4.01@springlms.local` | `Demo1234` |

There are 20 students per seeded group.

Admin account:

- If no admin exists, the backend prints a generated admin email and password in the terminal on startup.
- Save that generated credential after the first run.

Seeded academic structure:

- Faculty: `ITT`
- Specialization: `Computer Science`
- Groups: `6525a1`, `6525a2`, `6525a3`, `6525a4`
- Demo course: `WEB101`
- Demo section: `WP-6525A4`
- Attendance data: 2 weeks

## Authentication

Login:

```http
POST /api/auth/login
```

Example body:

```json
{
  "email": "teacher.demo1@springlms.local",
  "password": "Demo1234"
}
```

Use the returned JWT token on protected endpoints:

```http
Authorization: Bearer <token>
```

## Error Format

Errors return a structured response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required.",
  "path": "/api/teacher/materials/assignments",
  "timestamp": "2026-05-28T12:00:00"
}
```

Main status types:

| Status | Meaning |
|---|---|
| `400` | Invalid request |
| `401` | Not authenticated or invalid login |
| `403` | Authenticated, but not allowed |
| `404` | Resource not found |
| `500` | Unexpected server error |

## Main Workflow

1. Admin logs in.
2. Admin creates faculties.
3. Admin creates teachers.
4. Admin creates specializations.
5. Admin creates student groups and assigns multiple teachers with subjects, credits, and lecture/seminar/lab weekly counts.
6. Admin generates schedules.
7. Teachers view schedules and manage attendance.
8. Students view schedules and attendance.
9. Admin creates courses and course sections.
10. Teachers add course materials and assignments.
11. Students submit assignment work.
12. Teachers grade submissions.

## Important API Endpoints

### Auth

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/login` | Login and receive JWT |
| `POST` | `/api/auth/register/student` | Register student account |

### Admin Users

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/admin/dashboard` | Admin dashboard stats |
| `GET` | `/api/admin/users/search` | Search/filter users |
| `PATCH` | `/api/admin/users/{userId}/state` | Change user state |
| `POST` | `/api/admin/users/teachers` | Create teacher |
| `GET` | `/api/admin/faculties` | List faculties |
| `POST` | `/api/admin/faculties` | Create faculty |

### Admin Academic

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/admin/academic/specializations` | List specializations |
| `POST` | `/api/admin/academic/specializations` | Create specialization |
| `GET` | `/api/admin/academic/student-groups` | List student groups |
| `POST` | `/api/admin/academic/student-groups` | Create student group |
| `PATCH` | `/api/admin/academic/student-groups/{groupId}/approval` | Approve or reject group |
| `GET` | `/api/admin/academic/config` | Get global schedule/attendance config |
| `PUT` | `/api/admin/academic/config` | Update global schedule/attendance config |
| `GET` | `/api/admin/academic/student-groups/{groupId}/schedule` | View group schedule |
| `POST` | `/api/admin/academic/student-groups/{groupId}/schedule/generate` | Generate group schedule |

### Admin Courses

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/admin/courses` | List courses |
| `POST` | `/api/admin/courses` | Create course |
| `GET` | `/api/admin/courses/sections` | List sections |
| `POST` | `/api/admin/courses/sections` | Create section, attach groups, enroll students |

### Teacher Academic

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/teacher/academic/specializations` | List assigned specializations |
| `GET` | `/api/teacher/academic/student-groups` | List assigned student groups |
| `GET` | `/api/teacher/academic/students` | List assignable students |
| `POST` | `/api/teacher/academic/student-groups/{groupId}/students` | Add student to group |
| `GET` | `/api/teacher/academic/schedule` | View teacher schedule |
| `PATCH` | `/api/teacher/academic/schedule/{scheduleEntryId}` | Move teacher lecture time |

### Teacher Attendance

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/teacher/attendance/sessions/open` | Open attendance session |
| `GET` | `/api/teacher/attendance/sessions/{sessionId}` | View session records |
| `PUT` | `/api/teacher/attendance/sessions/{sessionId}` | Save or submit attendance |

Attendance becomes locked 3 hours after submission.

### Teacher Materials, Assignments, Grading

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/teacher/materials/sections` | List teacher sections |
| `GET` | `/api/teacher/materials/attachments` | List teacher attachments |
| `POST` | `/api/teacher/materials/attachments` | Add URL material |
| `POST` | `/api/teacher/materials/attachments/upload` | Upload real file material |
| `GET` | `/api/teacher/materials/attachments/{attachmentId}/download` | Download file material |
| `GET` | `/api/teacher/materials/assignments` | List teacher assignments |
| `POST` | `/api/teacher/materials/assignments` | Create assignment |
| `GET` | `/api/teacher/materials/submissions` | List submissions for teacher sections |
| `PUT` | `/api/teacher/materials/submissions/{submissionId}/grade` | Grade submission |

### Student Academic

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/student/academic/schedule` | View student schedule |

### Student Attendance

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/student/attendance/summary` | Attendance totals and final exam risk |
| `GET` | `/api/student/attendance/entries` | Attendance history by session |

### Student Materials and Assignments

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/student/materials` | View enrolled course materials |
| `GET` | `/api/student/materials/assignments` | View available assignments |
| `GET` | `/api/student/materials/submissions` | View own submissions |
| `POST` | `/api/student/materials/assignments/{assignmentId}/submissions` | Submit assignment |
| `GET` | `/api/student/materials/attachments/{attachmentId}/download` | Download file material |

## Request Examples

### Create Faculty

```http
POST /api/admin/faculties
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "name": "Information Technologies and Telecommunications",
  "code": "ITT"
}
```

### Create Teacher

```json
{
  "email": "teacher.new@springlms.local",
  "password": "Demo1234",
  "finCode": "ABC1234",
  "serialNumber": "AZE1234567",
  "firstName": "Aysel",
  "lastName": "Aliyeva",
  "dateOfBirth": "1990-05-10",
  "phoneNumber": "0501234567",
  "address": "Baku",
  "facultyId": 1,
  "hoursTaught": 0
}
```

### Create Student Group

```json
{
  "name": "6525a1",
  "specializationId": 1,
  "studyYear": 1,
  "teacherAssignments": [
    {
      "teacherUserId": 2,
      "subjectName": "Riyazi analiz",
      "creditValue": 7,
      "lectureSessionsPerWeek": 1,
      "seminarSessionsPerWeek": 1,
      "labSessionsPerWeek": 0
    },
    {
      "teacherUserId": 3,
      "subjectName": "Proqramlasdirma",
      "creditValue": 5,
      "lectureSessionsPerWeek": 1,
      "seminarSessionsPerWeek": 0,
      "labSessionsPerWeek": 1
    }
  ]
}
```

### Open Attendance Session

```json
{
  "scheduleEntryId": 1,
  "sessionDate": "2026-05-28"
}
```

### Submit Attendance

```json
{
  "submitted": true,
  "records": [
    {
      "studentUserId": 10,
      "status": "PRESENT"
    },
    {
      "studentUserId": 11,
      "status": "LATE"
    }
  ]
}
```

Allowed attendance statuses:

- `PRESENT`
- `LATE`
- `ABSENT`
- `EXCUSED`

### Create Assignment

```json
{
  "sectionId": 1,
  "title": "Lab 1",
  "description": "Build the first page and submit a short report.",
  "dueDate": "2026-06-15",
  "maxScore": 100
}
```

### Submit Assignment

This endpoint uses `multipart/form-data`.

Fields:

| Field | Type | Required |
|---|---|---|
| `textContent` | text | no |
| `linkUrl` | text | no |
| `file` | file | no |

At least one of `textContent`, `linkUrl`, or `file` is required.

Example:

```powershell
curl.exe -X POST "http://localhost:8080/api/student/materials/assignments/1/submissions" `
  -H "Authorization: Bearer <student-token>" `
  -F "textContent=My solution is attached." `
  -F "linkUrl=https://example.com/my-work" `
  -F "file=@C:\Users\arkan\Desktop\solution.docx"
```

### Grade Submission

```json
{
  "score": 95,
  "teacherFeedback": "Good structure and correct result."
}
```

### Upload File Material

This endpoint uses `multipart/form-data`.

Fields:

| Field | Type | Required |
|---|---|---|
| `sectionId` | number | yes |
| `title` | text | yes |
| `attachmentType` | text | no |
| `description` | text | no |
| `file` | file | yes |

Allowed attachment types:

- `IMAGE`
- `MODEL`
- `DOCUMENT`
- `LINK`

Example:

```powershell
curl.exe -X POST "http://localhost:8080/api/teacher/materials/attachments/upload" `
  -H "Authorization: Bearer <teacher-token>" `
  -F "sectionId=1" `
  -F "title=Lecture notes" `
  -F "attachmentType=DOCUMENT" `
  -F "description=Week 1 material" `
  -F "file=@C:\Users\arkan\Desktop\notes.pdf"
```

### Download File Material

```powershell
curl.exe -L "http://localhost:8080/api/student/materials/attachments/1/download" `
  -H "Authorization: Bearer <student-token>" `
  -o material.bin
```
## Common Commands

Compile backend:

```powershell
cd springlms
.\mvnw.cmd -q -DskipTests compile
```

Package backend:

```powershell
cd springlms
.\mvnw.cmd -q -DskipTests package
```

View Docker logs:

```powershell
docker compose logs -f backend
```

Reset PostgreSQL volume:

```powershell
docker compose down -v
docker compose up --build
```

## Notes For Development

- Admin credentials are generated only when no admin exists.
- Demo teacher and student credentials are stable.
- Most protected endpoints require `Authorization: Bearer <token>`.
- Teachers can only manage their assigned sections, groups, schedules, materials, attendance, assignments, and submissions.
- Students can only see enrolled materials, their own schedule, their own attendance, and their own submissions.
- File data is currently stored in PostgreSQL as binary data for simplicity.
