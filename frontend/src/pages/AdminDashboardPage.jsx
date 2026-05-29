import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { changeUserState, getAdminDashboard, listStudentGroups, searchUsers } from "../api/adminApi.js";

const dashboardCards = [
  ["totalUsers", "Total Users"],
  ["totalStudents", "Students"],
  ["totalTeachers", "Teachers"],
  ["totalAdmins", "Admins"],
  ["pendingUsers", "Pending"],
  ["activeUsers", "Active"],
  ["inactiveUsers", "Inactive"],
  ["suspendedUsers", "Suspended"],
  ["academicLeaveUsers", "Academic Leave"],
  ["graduatedUsers", "Graduated"],
  ["dismissedUsers", "Dismissed"]
];

const roleOptions = ["ADMIN", "TEACHER", "STUDENT"];

const stateOptions = [
  "PENDING_APPROVAL",
  "ACTIVE",
  "ACADEMIC_LEAVE",
  "GRADUATED",
  "DISMISSED",
  "SUSPENDED",
  "INACTIVE"
];

function formatEnum(value) {
  if (!value) return "";

  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatFundingType(value) {
  if (!value) return "-";
  if (value === "STATE_FUNDED") return "Dovlet sifarisi";
  if (value === "PERSONAL_CREDIT") return "Oz hesabina";
  return formatEnum(value);
}

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [query, setQuery] = useState("");
  const [role, setRole] = useState("");
  const [state, setState] = useState("");
  const [dashboardLoading, setDashboardLoading] = useState(true);
  const [usersLoading, setUsersLoading] = useState(false);
  const [stateUpdatingUserId, setStateUpdatingUserId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const activeFilters = useMemo(
    () => ({
      query,
      role,
      state
    }),
    [query, role, state]
  );

  async function loadDashboard() {
    const result = await getAdminDashboard();
    setStats(result);
  }

  async function loadUsers(filters = activeFilters) {
    setUsersLoading(true);
    setError("");

    try {
      const result = await searchUsers(filters);
      setUsers(result || []);
    } catch (err) {
      setError(err.message || "Failed to load users.");
    } finally {
      setUsersLoading(false);
    }
  }

  useEffect(() => {
    let active = true;

    async function loadInitialData() {
      try {
        const [dashboardResult, usersResult, studentGroupsResult] = await Promise.all([
          getAdminDashboard(),
          searchUsers({}),
          listStudentGroups()
        ]);

        if (!active) return;

        setStats(dashboardResult);
        setUsers(usersResult || []);
        setStudentGroups(studentGroupsResult || []);
      } catch (err) {
        if (active) {
          setError(err.message || "Failed to load admin dashboard.");
        }
      } finally {
        if (active) {
          setDashboardLoading(false);
        }
      }
    }

    loadInitialData();

    return () => {
      active = false;
    };
  }, []);

  async function handleSearchSubmit(event) {
    event.preventDefault();
    setSuccess("");
    await loadUsers(activeFilters);
  }

  async function handleResetFilters() {
    const resetFilters = {
      query: "",
      role: "",
      state: ""
    };

    setQuery("");
    setRole("");
    setState("");
    setSuccess("");
    await loadUsers(resetFilters);
  }

  async function handleStateChange(userId, nextState) {
    setError("");
    setSuccess("");
    setStateUpdatingUserId(userId);

    try {
      const updatedUser = await changeUserState(userId, nextState);

      setUsers((currentUsers) =>
        currentUsers.map((user) => (user.id === updatedUser.id ? updatedUser : user))
      );

      await loadDashboard();

      setSuccess(`User ${updatedUser.email} state changed to ${formatEnum(updatedUser.state)}.`);
    } catch (err) {
      setError(err.message || "Failed to change user state.");
    } finally {
      setStateUpdatingUserId(null);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <div className="pageTitleRow">
        <div>
          <span className="eyebrow">Admin</span>
          <h1>Dashboard</h1>
        </div>

        <div className="buttonRow compactRow">
          <Link className="primaryButton smallButton" to="/admin/faculties/groups">
            Student Groups
          </Link>
          <Link className="secondaryButton smallButton" to="/admin/config">
            Global Config
          </Link>
          <Link className="secondaryButton smallButton" to="/admin/teachers/create">
            Create Teacher
          </Link>
        </div>
      </div>

      {dashboardLoading && <div className="infoBox">Loading dashboard...</div>}
      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}

      {stats && (
        <div className="statGrid adminStatGrid">
          {dashboardCards.map(([key, label]) => (
            <article className="statCard" key={key}>
              <span>{label}</span>
              <strong>{stats[key] ?? 0}</strong>
            </article>
          ))}
        </div>
      )}

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Users</span>
          <h2>Search and manage users</h2>
        </div>

        <form className="filterPanel" onSubmit={handleSearchSubmit}>
          <label>
            Search
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search by email..."
            />
          </label>

          <label>
            Role
            <select value={role} onChange={(event) => setRole(event.target.value)}>
              <option value="">All roles</option>
              {roleOptions.map((option) => (
                <option value={option} key={option}>
                  {formatEnum(option)}
                </option>
              ))}
            </select>
          </label>

          <label>
            State
            <select value={state} onChange={(event) => setState(event.target.value)}>
              <option value="">All states</option>
              {stateOptions.map((option) => (
                <option value={option} key={option}>
                  {formatEnum(option)}
                </option>
              ))}
            </select>
          </label>

          <div className="filterActions">
            <button type="submit" disabled={usersLoading}>
              {usersLoading ? "Searching..." : "Search"}
            </button>
            <button type="button" className="secondaryButton" onClick={handleResetFilters}>
              Reset
            </button>
          </div>
        </form>

        <div className="tableWrap">
          <table className="adminTable">
            <thead>
              <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Name</th>
                <th>Role</th>
                <th>Entrance</th>
                <th>Year</th>
                <th>Admission</th>
                <th>Funding</th>
                <th>Current State</th>
                <th>Change State</th>
              </tr>
            </thead>
            <tbody>
              {usersLoading && (
                <tr>
                  <td colSpan="10">Loading users...</td>
                </tr>
              )}

              {!usersLoading && users.length === 0 && (
                <tr>
                  <td colSpan="10">No users found.</td>
                </tr>
              )}

              {!usersLoading &&
                users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.email}</td>
                    <td>{[user.firstName, user.lastName].filter(Boolean).join(" ") || "-"}</td>
                    <td>
                      <span className={`roleBadge role-${user.role?.toLowerCase()}`}>
                        {formatEnum(user.role)}
                      </span>
                    </td>
                    <td>{user.entranceScore ?? "-"}</td>
                    <td>{user.studyYear ?? "-"}</td>
                    <td>{user.admissionYear ?? "-"}</td>
                    <td>{formatFundingType(user.fundingType)}</td>
                    <td>
                      <span className={`stateBadge state-${user.state?.toLowerCase()}`}>
                        {formatEnum(user.state)}
                      </span>
                    </td>
                    <td>
                      <select
                        value={user.state}
                        disabled={stateUpdatingUserId === user.id}
                        onChange={(event) => handleStateChange(user.id, event.target.value)}
                      >
                        {stateOptions.map((option) => (
                          <option value={option} key={option}>
                            {formatEnum(option)}
                          </option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Student Groups</span>
          <h2>Current groups</h2>
        </div>

        <div className="tableWrap">
          <table className="adminTable">
            <thead>
              <tr>
                <th>Group</th>
                <th>Specialization</th>
                <th>Year</th>
                <th>Teachers</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {studentGroups.length === 0 && (
                <tr>
                  <td colSpan="5">No student groups found.</td>
                </tr>
              )}

              {studentGroups.map((group) => (
                <tr key={group.id}>
                  <td>{group.name}</td>
                  <td>{group.specializationName}</td>
                  <td>{group.studyYear}</td>
                  <td>
                    {group.teacherAssignments?.map(
                      (assignment) =>
                        `${assignment.subjectName} (${assignment.creditValue}) L${assignment.lectureSessionsPerWeek}/S${assignment.seminarSessionsPerWeek}/Lab${assignment.labSessionsPerWeek}`
                    ).join(", ") || "-"}
                  </td>
                  <td>{group.approved ? "Approved" : "Pending"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
