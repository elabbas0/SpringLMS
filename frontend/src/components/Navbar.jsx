import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        SpringLMS
      </Link>

      <nav className="navLinks">
        {!user && (
          <>
            <NavLink to="/login">Login</NavLink>
            <NavLink to="/register/student">Register</NavLink>
          </>
        )}

        {user?.role === "ADMIN" && (
          <>
            <NavLink to="/admin/dashboard">Dashboard</NavLink>
            <NavLink to="/admin/faculties/create">Create Faculty</NavLink>
            <NavLink to="/admin/teachers/create">Create Teacher</NavLink>
          </>
        )}

        {user?.role === "STUDENT" && (
          <NavLink to={user.state === "PENDING_APPROVAL" ? "/student/pending" : "/student/dashboard"}>
            Student
          </NavLink>
        )}

        {user?.role === "TEACHER" && <NavLink to="/teacher/dashboard">Teacher</NavLink>}
      </nav>

      {user && (
        <div className="userPill">
          <span>{user.email}</span>
          <strong>{user.role}</strong>
          <button className="ghostButton" onClick={logout}>Logout</button>
        </div>
      )}
    </header>
  );
}
