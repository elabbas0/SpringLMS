# SpringLMS React Frontend

Vite React frontend for the current Spring Boot LMS API.

## Run

```bash
npm install
npm run dev
```

Backend expected at:

```txt
http://localhost:8080
```

Change it in:

```txt
src/api/apiClient.js
```

## Current implemented routes

- `/login`
- `/register/student`
- `/admin/dashboard`
- `/admin/teachers/create`
- `/student/dashboard`
- `/student/pending`
- `/teacher/dashboard`
- `/forbidden`

## Important auth note

This backend currently has no JWT/session token. Admin endpoints require HTTP Basic auth, so the frontend temporarily stores email/password in localStorage.

This is dev-only and should be replaced later with JWT, secure cookies, or session auth.
