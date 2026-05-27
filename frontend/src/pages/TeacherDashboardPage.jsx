import { useEffect, useMemo, useState } from "react";
import Select from "react-select";
import {
  assignStudentToTeacherGroup,
  listTeacherAssignableStudents,
  listTeacherStudentGroups
} from "../api/teacherApi.js";
import { darkSelectStyles } from "../components/selectStyles.js";

export default function TeacherDashboardPage() {
  const [studentGroups, setStudentGroups] = useState([]);
  const [students, setStudents] = useState([]);
  const [studentSelectionByGroup, setStudentSelectionByGroup] = useState({});
  const [pageLoading, setPageLoading] = useState(true);
  const [assigningGroupId, setAssigningGroupId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

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
      const [studentGroupResult, studentResult] = await Promise.all([
        listTeacherStudentGroups(),
        listTeacherAssignableStudents()
      ]);

      setStudentGroups(studentGroupResult || []);
      setStudents(studentResult || []);
    } catch (err) {
      setError(err.message || "Failed to load teacher data.");
    } finally {
      setPageLoading(false);
    }
  }

  function updateGroupStudentSelection(groupId, studentUserId) {
    setStudentSelectionByGroup((current) => ({
      ...current,
      [groupId]: studentUserId
    }));
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
      const [refreshedStudents, refreshedGroups] = await Promise.all([
        listTeacherAssignableStudents(),
        listTeacherStudentGroups()
      ]);
      setStudents(refreshedStudents || []);
      setStudentGroups(refreshedGroups || []);
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

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">My Groups</span>
          <h2>Assigned student groups</h2>
        </div>

        {studentGroups.length === 0 && <div className="infoBox">No student groups assigned yet.</div>}

        <div className="groupCardList">
          {studentGroups.map((group) => (
            <article className="groupCard" key={group.id}>
              <div className="groupCardHeader">
                <div>
                  <h3>{group.name}</h3>
                  <span className="groupMeta">{group.specializationName} · Year {group.studyYear}</span>
                </div>
                <span className="stateBadge state-active">Assigned</span>
              </div>

              <div className="groupMembers">
                <h4>Subjects</h4>
                <div className="memberChips">
                  {group.teacherAssignments?.map((assignment) => (
                    <span className="memberChip" key={assignment.id}>
                      {assignment.subjectName} ({assignment.creditValue}) - {assignment.teacherName}
                    </span>
                  ))}
                </div>
              </div>

              <div className="groupAssignRow">
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
