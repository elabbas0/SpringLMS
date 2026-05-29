import { jsonRequest } from "./apiClient.js";

export function registerStudent(payload) {
  return jsonRequest("/api/auth/register/student", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function loginUser(email, password) {
  return jsonRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
}