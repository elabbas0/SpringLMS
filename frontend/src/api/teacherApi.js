import { authHeaders, jsonRequest } from "./apiClient.js";

export function listTeacherSpecializations() {
  return jsonRequest("/api/teacher/academic/specializations", {
    method: "GET",
    headers: authHeaders()
  });
}

export function listTeacherStudentGroups() {
  return jsonRequest("/api/teacher/academic/student-groups", {
    method: "GET",
    headers: authHeaders()
  });
}

export function listTeacherAssignableStudents() {
  return jsonRequest("/api/teacher/academic/students", {
    method: "GET",
    headers: authHeaders()
  });
}

export function createTeacherStudentGroup(payload) {
  return jsonRequest("/api/teacher/academic/student-groups", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function assignStudentToTeacherGroup(groupId, studentUserId) {
  return jsonRequest(`/api/teacher/academic/student-groups/${groupId}/students`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ studentUserId })
  });
}
