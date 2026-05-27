import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";

function getRedirectPath(user) {
  if (user.role === "ADMIN") return "/admin/dashboard";
  if (user.role === "TEACHER") return "/teacher/dashboard";

  if (user.role === "STUDENT") {
    return user.state === "PENDING_APPROVAL" ? "/student/pending" : "/student/dashboard";
  }

  return "/";
}

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({
    email: "",
    password: ""
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function updateField(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const user = await login(form.email, form.password);
      navigate(getRedirectPath(user), { replace: true });
    } catch (err) {
      setError(err.message || "Invalid email or password.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="authLayout fadeIn">
      <aside className="authIntroPanel">
        <span className="eyebrow">SpringLMS</span>
        <h1>Login</h1>
        <p>Azerbaijani university workflow for admins, teachers, and students.</p>
      </aside>

      <section className="authCard">
        <form onSubmit={handleSubmit} className="formGrid">
          <label>
            Email
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={updateField}
              required
              placeholder="teacher.demo1@springlms.local"
            />
          </label>

          <label>
            Password
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={updateField}
              required
              placeholder="Demo1234"
            />
          </label>

          {error && <div className="errorBox">{error}</div>}

          <button disabled={loading} className="primaryButton">
            {loading ? "Logging in..." : "Login"}
          </button>

          <p className="mutedText authFootnote">
            No account? <Link to="/register/student">Submit registration</Link>
          </p>
        </form>
      </section>
    </div>
  );
}
