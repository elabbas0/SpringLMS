import { useEffect, useMemo, useState } from "react";
import Select from "react-select";
import { Link } from "react-router-dom";
import {
  createStudentGroup,
  generateStudentGroupSchedule,
  listSpecializations,
  listStudentGroups,
  listStudentGroupSchedule,
  searchUsers
} from "../api/adminApi.js";
import { darkSelectStyles } from "../components/selectStyles.js";

const initialAssignment = {
  teacherUserId: null,
  subjectName: "",
  creditValue: 6
};

const initialForm = {
  name: "",
  specializationId: null,
  studyYear: 1,
  teacherAssignments: [{ ...initialAssignment }]
};

export default function ManageStudentGroupsPage() {
  const [form, setForm] = useState(initialForm);
  const [specializations, setSpecializations] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [scheduleByGroupId, setScheduleByGroupId] = useState({});
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [generatingGroupId, setGeneratingGroupId] = useState(null);
  const [scheduleLoadingGroupId, setScheduleLoadingGroupId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const specializationOptions = useMemo(
    () =>
      specializations.map((specialization) => ({
        value: specialization.id,
        label: `${specialization.name} - ${specialization.facultyName}`
      })),
    [specializations]
  );

  const teacherOptions = useMemo(
    () =>
      teachers.map((teacher) => ({
        value: teacher.id,
        label: [teacher.firstName, teacher.lastName].filter(Boolean).join(" ") || teacher.email
      })),
    [teachers]
  );

  useEffect(() => {
    loadPageData();
  }, []);

  async function loadPageData() {
    setPageLoading(true);
    setError("");

    try {
      const [specializationsResult, teachersResult, groupsResult] = await Promise.all([
        listSpecializations(),
        searchUsers({ role: "TEACHER", state: "ACTIVE" }),
        listStudentGroups()
      ]);

      setSpecializations(specializationsResult || []);
      setTeachers(teachersResult || []);
      setStudentGroups(groupsResult || []);
    } catch (err) {
      setError(err.message || "Failed to load student groups page.");
    } finally {
      setPageLoading(false);
    }
  }

  function updateField(field, value) {
    setForm((current) => ({
      ...current,
      [field]: value
    }));
  }

  function updateAssignment(index, field, value) {
    setForm((current) => ({
      ...current,
      teacherAssignments: current.teacherAssignments.map((assignment, assignmentIndex) =>
        assignmentIndex === index
          ? {
              ...assignment,
              [field]: value
            }
          : assignment
      )
    }));
  }

  function addAssignmentRow() {
    setForm((current) => ({
      ...current,
      teacherAssignments: [...current.teacherAssignments, { ...initialAssignment }]
    }));
  }

  function removeAssignmentRow(index) {
    setForm((current) => ({
      ...current,
      teacherAssignments: current.teacherAssignments.filter((_, assignmentIndex) => assignmentIndex !== index)
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const payload = {
        ...form,
        studyYear: Number(form.studyYear),
        teacherAssignments: form.teacherAssignments.filter(
          (assignment) => assignment.teacherUserId && assignment.subjectName.trim() !== ""
        )
      };

      const created = await createStudentGroup(payload);
      setStudentGroups((current) => [created, ...current]);
      setSuccess(`Student group created: ${created.name}`);
      setForm(initialForm);
    } catch (err) {
      setError(err.message || "Failed to create student group.");
    } finally {
      setLoading(false);
    }
  }

  async function handleGenerateSchedule(groupId) {
    setGeneratingGroupId(groupId);
    setError("");
    setSuccess("");

    try {
      const schedule = await generateStudentGroupSchedule(groupId);
      setScheduleByGroupId((current) => ({
        ...current,
        [groupId]: schedule || []
      }));
      setSuccess("Schedule generated.");
    } catch (err) {
      setError(err.message || "Failed to generate schedule.");
    } finally {
      setGeneratingGroupId(null);
    }
  }

  async function handleLoadSchedule(groupId) {
    setScheduleLoadingGroupId(groupId);
    setError("");

    try {
      const schedule = await listStudentGroupSchedule(groupId);
      setScheduleByGroupId((current) => ({
        ...current,
        [groupId]: schedule || []
      }));
    } catch (err) {
      setError(err.message || "Failed to load schedule.");
    } finally {
      setScheduleLoadingGroupId(null);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <div className="pageTitleRow">
        <div>
          <span className="eyebrow">Admin</span>
          <h1>Student Groups</h1>
        </div>

        <div className="buttonRow compactRow">
          <Link className="secondaryButton smallButton" to="/admin/config">
            Global Config
          </Link>
          <Link className="secondaryButton smallButton" to="/admin/dashboard">
            Back to Dashboard
          </Link>
        </div>
      </div>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}
      {pageLoading && <div className="infoBox">Loading...</div>}

      <form className="formGrid" onSubmit={handleSubmit}>
        <div className="threeColumn">
          <label>
            Group Name
            <input
              value={form.name}
              onChange={(event) => updateField("name", event.target.value)}
              placeholder="6525a1"
              required
            />
          </label>

          <label>
            Specialization
            <Select
              className="appSelect"
              styles={darkSelectStyles}
              menuPortalTarget={document.body}
              options={specializationOptions}
              value={specializationOptions.find((option) => option.value === form.specializationId) ?? null}
              onChange={(option) => updateField("specializationId", option?.value ?? null)}
              placeholder="Select specialization..."
              noOptionsMessage={() => "No specializations"}
            />
          </label>

          <label>
            Study Year
            <select value={form.studyYear} onChange={(event) => updateField("studyYear", event.target.value)}>
              <option value="1">1st year</option>
              <option value="2">2nd year</option>
              <option value="3">3rd year</option>
              <option value="4">4th year</option>
            </select>
          </label>
        </div>

        <div className="adminSection">
          <div className="sectionHeader">
            <span className="eyebrow">Teacher Assignments</span>
            <h2>Subjects and teachers</h2>
          </div>

          <div className="assignmentList">
            {form.teacherAssignments.map((assignment, index) => (
              <div className="assignmentRow" key={`${index}-${assignment.teacherUserId ?? "new"}`}>
                <label>
                  Subject
                  <input
                    value={assignment.subjectName}
                    onChange={(event) => updateAssignment(index, "subjectName", event.target.value)}
                    placeholder="Riyazi analiz"
                    required
                  />
                </label>

                <label>
                  Credits
                  <input
                    type="number"
                    min="1"
                    max="15"
                    value={assignment.creditValue}
                    onChange={(event) => updateAssignment(index, "creditValue", Number(event.target.value))}
                    required
                  />
                </label>

                <label>
                  Teacher
                  <Select
                    className="appSelect"
                    styles={darkSelectStyles}
                    menuPortalTarget={document.body}
                    options={teacherOptions}
                    value={teacherOptions.find((option) => option.value === assignment.teacherUserId) ?? null}
                    onChange={(option) => updateAssignment(index, "teacherUserId", option?.value ?? null)}
                    placeholder="Select teacher..."
                    noOptionsMessage={() => "No active teachers"}
                  />
                </label>

                <button
                  type="button"
                  className="ghostButton"
                  onClick={() => removeAssignmentRow(index)}
                  disabled={form.teacherAssignments.length === 1}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>

          <div className="buttonRow compactRow">
            <button type="button" className="secondaryButton smallButton" onClick={addAssignmentRow}>
              Add Teacher
            </button>
          </div>
        </div>

        <div className="buttonRow">
          <button type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Student Group"}
          </button>
        </div>
      </form>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Current Groups</span>
          <h2>Manage schedules</h2>
        </div>

        <div className="groupCardList">
          {studentGroups.length === 0 && <div className="infoBox">No student groups yet.</div>}

          {studentGroups.map((group) => (
            <article className="groupCard" key={group.id}>
              <div className="groupCardHeader">
                <div>
                  <h3>{group.name}</h3>
                  <span className="groupMeta">
                    {group.specializationName} · Year {group.studyYear}
                  </span>
                </div>
                <div className="buttonRow compactRow">
                  <button
                    type="button"
                    className="secondaryButton smallButton"
                    disabled={generatingGroupId === group.id}
                    onClick={() => handleGenerateSchedule(group.id)}
                  >
                    {generatingGroupId === group.id ? "Generating..." : "Generate Schedule"}
                  </button>
                  <button
                    type="button"
                    className="ghostButton smallButton"
                    disabled={scheduleLoadingGroupId === group.id}
                    onClick={() => handleLoadSchedule(group.id)}
                  >
                    View Schedule
                  </button>
                </div>
              </div>

              <div className="groupMembers">
                <h4>Teachers</h4>
                <div className="memberChips">
                  {group.teacherAssignments?.map((assignment) => (
                    <span className="memberChip" key={assignment.id}>
                      {assignment.subjectName} ({assignment.creditValue}) - {assignment.teacherName}
                    </span>
                  ))}
                </div>
              </div>

              {!!scheduleByGroupId[group.id]?.length && (
                <div className="groupMembers scheduleBlock">
                  <h4>Schedule</h4>
                  <div className="tableWrap">
                    <table className="adminTable">
                      <thead>
                        <tr>
                          <th>Subject</th>
                          <th>Teacher</th>
                          <th>Day</th>
                          <th>Time</th>
                        </tr>
                      </thead>
                      <tbody>
                        {scheduleByGroupId[group.id].map((entry) => (
                          <tr key={entry.id}>
                            <td>{entry.subjectName}</td>
                            <td>{entry.teacherName}</td>
                            <td>{entry.dayOfWeek}</td>
                            <td>{entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
