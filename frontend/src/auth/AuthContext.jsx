import { createContext, useContext, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../api/authApi.js";
import { removeStoredToken, setStoredToken } from "../api/apiClient.js";

const AuthContext = createContext(null);

const USER_STORAGE_KEY = "springlms_user";

function readStoredJson(key) {
  try {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const navigate = useNavigate();

  const [user, setUser] = useState(() => readStoredJson(USER_STORAGE_KEY));

  async function login(email, password) {
    const loggedInUser = await loginUser(email, password);

    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(loggedInUser));
    setStoredToken(loggedInUser.token);

    setUser(loggedInUser);

    return loggedInUser;
  }

  function logout() {
    localStorage.removeItem(USER_STORAGE_KEY);
    removeStoredToken();
    setUser(null);
    navigate("/login", { replace: true });
  }

  const value = useMemo(
    () => ({
      user,
      isLoggedIn: Boolean(user),
      login,
      logout
    }),
    [user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
