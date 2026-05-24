import { useEffect, useMemo, useState } from "react";
import Select from "react-select";
import {
  assignStudentToTeacherGroup,
  createTeacherStudentGroup,
  listTeacherAssignableStudents,
  listTeacherSpecializations,
  listTeacherStudentGroups
} from "../api/teacherApi.js";
import { darkSelectStyles } from "../components/selectStyles.js";

const initialForm = {
  name: "",
  specializationId: null
};

export default function TeacherDashboardPage() {
  const [form, setForm] = useState(initialForm);
  const [specializations, setSpecializations] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [students, setStudents] = useState([]);
  const [studentSelectionByGroup, setStudentSelectionByGroup] = useState({});
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [assigningGroupId, setAssigningGroupId] = useState(null);
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

  const studentOptions = useMemo(
    () =>
      students.map((student) => ({
        value: student.userId,
        label: `${student.firstName} ${student.lastName} - ${student.email}${student.currentGroupName ? ` (${student.currentGroupName})` : ""}`
      })),
    [students]
  );

  useEffect(() => {
    loadTeacherData();
  }, []);

  async function loadTeacherData() {
    setPageLoading(true);
    setError("");

    try {
      const [specializationResult, studentGroupResult, studentResult] = await Promise.all([
        listTeacherSpecializations(),
        listTeacherStudentGroups(),
        listTeacherAssignableStudents()
      ]);

      setSpecializations(specializationResult || []);
      setStudentGroups(studentGroupResult || []);
      setStudents(studentResult || []);
    } catch (err) {
      setError(err.message || "Failed to load teacher data.");
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

  function updateGroupStudentSelection(groupId, studentUserId) {
    setStudentSelectionByGroup((current) => ({
      ...current,
      [groupId]: studentUserId
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      if (!form.specializationId) {
        throw new Error("Please select a specialization.");
      }

      const created = await createTeacherStudentGroup(form);
      setStudentGroups((current) => [created, ...current]);
      setSuccess(`Student group created: ${created.name}`);
      setForm(initialForm);
    } catch (err) {
      setError(err.message || "Failed to create student group.");
    } finally {
      setLoading(false);
    }
  }

  async function handleAssignStudent(groupId) {
    const studentUserId = studentSelectionByGroup[groupId];
    setAssigningGroupId(groupId);
    setError("");
    setSuccess("");

    try {
      if (!studentUserId) {
        throw new Error("Please select a student.");
      }

      await assignStudentToTeacherGroup(groupId, studentUserId);
      const refreshedStudents = await listTeacherAssignableStudents();
      setStudents(refreshedStudents || []);
      setStudentSelectionByGroup((current) => ({
        ...current,
        [groupId]: null
      }));
      setSuccess("Student assigned to group.");
    } catch (err) {
      setError(err.message || "Failed to assign student.");
    } finally {
      setAssigningGroupId(null);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Teacher</span>
      <h1>Student Groups</h1>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}
      {pageLoading && <div className="infoBox">Loading...</div>}

      <form className="formGrid" onSubmit={handleSubmit}>
        <div className="twoColumn">
          <label>
            Student Group Name
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
              noOptionsMessage={() => "No specialization assigned"}
            />
          </label>
        </div>

        <div className="buttonRow">
          <button type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Student Group"}
          </button>
        </div>
      </form>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">My Groups</span>
          <h2>Submitted student groups</h2>
        </div>

        {studentGroups.length === 0 && <div className="infoBox">No student groups created yet.</div>}

        <div className="groupCardList">
          {studentGroups.map((group) => (
            <article className="groupCard" key={group.id}>
              <div className="groupCardHeader">
                <div>
                  <h3>{group.name}</h3>
                  <span className="groupMeta">{group.specializationName} · {group.facultyName}</span>
                </div>
                <span className={`stateBadge ${group.approved ? "state-active" : "state-pending_approval"}`}>
                  {group.approved ? "Approved" : "Pending"}
                </span>
              </div>

              <div className="groupAssignRow">
                {group.approved ? (
                  <>
                    <Select
                      className="appSelect assignSelect"
                      styles={darkSelectStyles}
                      menuPortalTarget={document.body}
                      options={studentOptions}
                      value={studentOptions.find((option) => option.value === studentSelectionByGroup[group.id]) ?? null}
                      onChange={(option) => updateGroupStudentSelection(group.id, option?.value ?? null)}
                      placeholder="Select student..."
                      noOptionsMessage={() => "No students"}
                    />
                    <button
                      type="button"
                      className="secondaryButton"
                      disabled={assigningGroupId === group.id}
                      onClick={() => handleAssignStudent(group.id)}
                    >
                      Add
                    </button>
                  </>
                ) : (
                  <span className="mutedText">Approve first</span>
                )}
              </div>

              <div className="groupMembers">
                <h4>Students</h4>
                {group.students?.length ? (
                  <div className="memberChips">
                    {group.students.map((student) => (
                      <span className="memberChip" key={student.userId}>
                        {student.firstName} {student.lastName}
                      </span>
                    ))}
                  </div>
                ) : (
                  <div className="mutedText">No students assigned yet.</div>
                )}
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
