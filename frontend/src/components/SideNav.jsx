import { NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";

const navByRole = {
  STUDENT: [
    { to: "/student/dashboard", label: "Dashboard" },
    { to: "/student/schedule", label: "Schedule" },
    { to: "/student/materials", label: "Materials" },
    { to: "/student/tasks", label: "Tasks" },
    { to: "/student/attendance", label: "Attendance" }
  ],
  TEACHER: [
    { to: "/teacher/dashboard", label: "Dashboard" },
    { to: "/teacher/schedule", label: "Schedule" },
    { to: "/teacher/attendance", label: "Attendance" },
    { to: "/teacher/materials", label: "Materials" },
    { to: "/teacher/groups", label: "Groups" },
    { to: "/teacher/students", label: "Students" },
    { to: "/teacher/standing", label: "Standing" }
  ],
  ADMIN: [
    { to: "/admin/dashboard", label: "Dashboard" },
    { to: "/admin/faculties", label: "Faculties" },
    { to: "/admin/faculties/groups", label: "Groups", child: true },
    { to: "/admin/courses", label: "Courses" },
    { to: "/admin/students", label: "Students" },
    { to: "/admin/teachers", label: "Teachers" }
  ]
};

export default function SideNav() {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  const items = navByRole[user.role] || [];

  return (
    <aside className="sideNav">
      <div className="sideNavPanel">
        <span className="sideNavRole">{user.role}</span>
        <strong>{user.email}</strong>
      </div>

      <nav className="sideNavList">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `sideNavLink${isActive ? " active" : ""}${item.child ? " child" : ""}`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
