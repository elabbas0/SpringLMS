import { NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";

const navByRole = {
  STUDENT: [
    { to: "/student/dashboard", label: "Dashboard" },
    { to: "/student/schedule", label: "Schedule" },
    { to: "/student/tasks", label: "Tasks" },
    { to: "/student/attendance", label: "Attendance" }
  ],
  TEACHER: [
    { to: "/teacher/dashboard", label: "Dashboard" },
    { to: "/teacher/schedule", label: "Schedule" },
    { to: "/teacher/groups", label: "Groups" },
    { to: "/teacher/students", label: "Students" },
    { to: "/teacher/standing", label: "Standing" }
  ],
  ADMIN: [
    { to: "/admin/dashboard", label: "Dashboard" },
    { to: "/admin/faculties", label: "Faculties" },
    { to: "/admin/faculties/groups", label: "Groups", child: true },
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
