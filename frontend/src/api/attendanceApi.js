import { authHeaders, jsonRequest } from "./apiClient.js";

export function openTeacherAttendanceSession(payload) {
  return jsonRequest("/api/teacher/attendance/sessions/open", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function getTeacherAttendanceSession(sessionId) {
  return jsonRequest(`/api/teacher/attendance/sessions/${sessionId}`, {
    method: "GET",
    headers: authHeaders()
  });
}

export function saveTeacherAttendanceSession(sessionId, payload) {
  return jsonRequest(`/api/teacher/attendance/sessions/${sessionId}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function getStudentAttendanceSummary() {
  return jsonRequest("/api/student/attendance/summary", {
    method: "GET",
    headers: authHeaders()
  });
}
