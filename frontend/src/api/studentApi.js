import { authHeaders, jsonRequest } from "./apiClient.js";

export function listStudentSchedule() {
  return jsonRequest("/api/student/academic/schedule", {
    method: "GET",
    headers: authHeaders()
  });
}
