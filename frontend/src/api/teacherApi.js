import { authHeaders, jsonRequest } from "./apiClient.js";

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

export function assignStudentToTeacherGroup(groupId, studentUserId) {
  return jsonRequest(`/api/teacher/academic/student-groups/${groupId}/students`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ studentUserId })
  });
}

export function listTeacherSchedule() {
  return jsonRequest("/api/teacher/academic/schedule", {
    method: "GET",
    headers: authHeaders()
  });
}

export function updateTeacherScheduleEntry(scheduleEntryId, payload) {
  return jsonRequest(`/api/teacher/academic/schedule/${scheduleEntryId}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function listTeacherSections() {
  return jsonRequest("/api/teacher/materials/sections", {
    method: "GET",
    headers: authHeaders()
  });
}

export function listTeacherAttachments() {
  return jsonRequest("/api/teacher/materials/attachments", {
    method: "GET",
    headers: authHeaders()
  });
}

export function createTeacherAttachment(payload) {
  return jsonRequest("/api/teacher/materials/attachments", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}
