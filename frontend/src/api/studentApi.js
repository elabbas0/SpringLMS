import { authHeaders, jsonRequest } from "./apiClient.js";

export function listStudentSchedule() {
  return jsonRequest("/api/student/academic/schedule", {
    method: "GET",
    headers: authHeaders()
  });
}

export function getStudentAttendanceSummary() {
  return jsonRequest("/api/student/attendance/summary", {
    method: "GET",
    headers: authHeaders()
  });
}

export function listStudentAttendanceEntries() {
  return jsonRequest("/api/student/attendance/entries", {
    method: "GET",
    headers: authHeaders()
  });
}

export function listStudentMaterials() {
  return jsonRequest("/api/student/materials", {
    method: "GET",
    headers: authHeaders()
  });
}
