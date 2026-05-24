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
