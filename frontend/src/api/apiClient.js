export const API_BASE_URL = "http://localhost:8080";

const TOKEN_STORAGE_KEY = "springlms_auth_token";
const USER_STORAGE_KEY = "springlms_user";

export function getStoredToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }
}

export function removeStoredToken() {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

function clearStoredSession() {
  localStorage.removeItem(USER_STORAGE_KEY);
  removeStoredToken();
}

export function authHeaders() {
  const token = getStoredToken();

  if (!token) {
    throw new Error("Missing authentication token. Please log in again.");
  }

  return {
    Authorization: `Bearer ${token}`
  };
}

async function safeParseResponse(response) {
  const text = await response.text();

  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function extractErrorMessage(data, fallback) {
  if (!data) return fallback;
  if (typeof data === "string") return data;
  if (data.message) return data.message;
  if (data.error) return data.error;
  if (data.detail) return data.detail;

  return fallback;
}

export async function jsonRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    }
  });

  const data = await safeParseResponse(response);

  if (!response.ok) {
    if (response.status === 401) {
      clearStoredSession();

      if (typeof window !== "undefined" && window.location.pathname !== "/login") {
        window.location.assign("/login");
      }
    }

    throw new Error(extractErrorMessage(data, `Request failed with status ${response.status}`));
  }

  return data;
}
