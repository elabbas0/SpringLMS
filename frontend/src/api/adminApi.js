import { authHeaders, jsonRequest } from "./apiClient.js";

function buildQuery(params) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, value);
    }
  });

  const queryString = searchParams.toString();

  return queryString ? `?${queryString}` : "";
}

export function getAdminDashboard() {
  return jsonRequest("/api/admin/dashboard", {
    method: "GET",
    headers: authHeaders()
  });
}

export function searchUsers({ query, role, state } = {}) {
  const queryString = buildQuery({ query, role, state });

  return jsonRequest(`/api/admin/users/search${queryString}`, {
    method: "GET",
    headers: authHeaders()
  });
}

export function changeUserState(userId, state) {
  return jsonRequest(`/api/admin/users/${userId}/state`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ state })
  });
}

export function createTeacher(payload) {
  return jsonRequest("/api/admin/users/teachers", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function listFaculties() {
  return jsonRequest("/api/admin/faculties", {
    method: "GET",
    headers: authHeaders()
  });
}

export function createFaculty(payload) {
  return jsonRequest("/api/admin/faculties", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function listSpecializations() {
  return jsonRequest("/api/admin/academic/specializations", {
    method: "GET",
    headers: authHeaders()
  });
}

export function createSpecialization(payload) {
  return jsonRequest("/api/admin/academic/specializations", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload)
  });
}

export function listStudentGroups() {
  return jsonRequest("/api/admin/academic/student-groups", {
    method: "GET",
    headers: authHeaders()
  });
}

export function changeStudentGroupApproval(groupId, approved) {
  return jsonRequest(`/api/admin/academic/student-groups/${groupId}/approval`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ approved })
  });
}
