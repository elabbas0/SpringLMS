import { Navigate, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar.jsx";
import ProtectedRoute from "./auth/ProtectedRoute.jsx";
import { useAuth } from "./auth/AuthContext.jsx";

import LoginPage from "./pages/LoginPage.jsx";
import StudentRegisterPage from "./pages/StudentRegisterPage.jsx";
import AdminDashboardPage from "./pages/AdminDashboardPage.jsx";
import CreateFacultyPage from "./pages/CreateFacultyPage.jsx";
import CreateTeacherPage from "./pages/CreateTeacherPage.jsx";
import StudentDashboardPage from "./pages/StudentDashboardPage.jsx";
import StudentPendingPage from "./pages/StudentPendingPage.jsx";
import TeacherDashboardPage from "./pages/TeacherDashboardPage.jsx";
import ForbiddenPage from "./pages/ForbiddenPage.jsx";

function HomeRedirect() {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;

  if (user.role === "ADMIN") return <Navigate to="/admin/dashboard" replace />;
  if (user.role === "TEACHER") return <Navigate to="/teacher/dashboard" replace />;

  if (user.role === "STUDENT") {
    return (
      <Navigate
        to={user.state === "PENDING_APPROVAL" ? "/student/pending" : "/student/dashboard"}
        replace
      />
    );
  }

  return <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <>
      <Navbar />

      <main className="pageShell">
        <Routes>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register/student" element={<StudentRegisterPage />} />
          <Route path="/forbidden" element={<ForbiddenPage />} />

          <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="/admin/faculties/create" element={<CreateFacultyPage />} />
            <Route path="/admin/teachers/create" element={<CreateTeacherPage />} />
          </Route>

          <Route element={<ProtectedRoute requiredRole="STUDENT" />}>
            <Route path="/student/dashboard" element={<StudentDashboardPage />} />
            <Route path="/student/pending" element={<StudentPendingPage />} />
          </Route>

          <Route element={<ProtectedRoute requiredRole="TEACHER" />}>
            <Route path="/teacher/dashboard" element={<TeacherDashboardPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}
