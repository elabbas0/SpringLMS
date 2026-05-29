import { useEffect, useMemo, useState } from "react";
import {
  listTeacherSchedule,
  listTeacherStudentGroups
} from "../api/teacherApi.js";
import {
  openTeacherAttendanceSession,
  saveTeacherAttendanceSession
} from "../api/attendanceApi.js";

const dayLabels = {
  MONDAY: "Monday",
  TUESDAY: "Tuesday",
  WEDNESDAY: "Wednesday",
  THURSDAY: "Thursday",
  FRIDAY: "Friday",
  SATURDAY: "Saturday",
  SUNDAY: "Sunday"
};

function formatSessionType(sessionType) {
  if (!sessionType) return "";
  if (sessionType === "LABORATORY") return "Lab";
  return sessionType[0] + sessionType.slice(1).toLowerCase();
}

function todayAsInput() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

export default function TeacherAttendancePage() {
  const [groups, setGroups] = useState([]);
  const [scheduleEntries, setScheduleEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedEntry, setSelectedEntry] = useState(null);
  const [sessionDate, setSessionDate] = useState(todayAsInput());
  const [sessionLoading, setSessionLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [attendanceSession, setAttendanceSession] = useState(null);

  useEffect(() => {
    loadTeacherAttendanceData();
  }, []);

  const scheduleByGroup = useMemo(() => {
    return groups.map((group) => ({
      ...group,
      scheduleEntries: scheduleEntries.filter((entry) => entry.studentGroupId === group.id)
    }));
  }, [groups, scheduleEntries]);

  async function loadTeacherAttendanceData() {
    setLoading(true);
    setError("");

    try {
      const [groupResult, scheduleResult] = await Promise.all([
        listTeacherStudentGroups(),
        listTeacherSchedule()
      ]);

      setGroups(groupResult || []);
      setScheduleEntries(scheduleResult || []);
    } catch (err) {
      setError(err.message || "Failed to load attendance data.");
    } finally {
      setLoading(false);
    }
  }

  async function openAttendanceModal(entry) {
    setSelectedEntry(entry);
    setSessionDate(todayAsInput());
    setModalOpen(true);
    setError("");
    setSuccess("");
    await loadAttendanceSession(entry.id, todayAsInput());
  }

  async function loadAttendanceSession(scheduleEntryId, dateValue) {
    setSessionLoading(true);
    setError("");

    try {
      const session = await openTeacherAttendanceSession({
        scheduleEntryId,
        sessionDate: dateValue
      });

      setAttendanceSession(session);
    } catch (err) {
      setAttendanceSession(null);
      setError(err.message || "Failed to open attendance session.");
    } finally {
      setSessionLoading(false);
    }
  }

  function updateStudentStatus(studentUserId, status) {
    setAttendanceSession((current) => {
      if (!current) {
        return current;
      }

      return {
        ...current,
        records: current.records.map((record) =>
          record.studentUserId === studentUserId ? { ...record, status } : record
        )
      };
    });
  }

  function setStudentPresent(studentUserId, isPresent) {
    setAttendanceSession((current) => {
      if (!current) {
        return current;
      }

      return {
        ...current,
        records: current.records.map((record) => {
          if (record.studentUserId !== studentUserId) {
            return record;
          }

          return {
            ...record,
            status: isPresent ? (record.status === "LATE" ? "LATE" : "PRESENT") : "ABSENT"
          };
        })
      };
    });
  }

  function setStudentLate(studentUserId, isLate) {
    setAttendanceSession((current) => {
      if (!current) {
        return current;
      }

      return {
        ...current,
        records: current.records.map((record) => {
          if (record.studentUserId !== studentUserId) {
            return record;
          }

          if (record.status === "ABSENT" || record.status === "EXCUSED") {
            return record;
          }

          return {
            ...record,
            status: isLate ? "LATE" : "PRESENT"
          };
        })
      };
    });
  }

  async function handleSave(submitted) {
    if (!attendanceSession) {
      return;
    }

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      const saved = await saveTeacherAttendanceSession(attendanceSession.id, {
        submitted,
        records: attendanceSession.records.map((record) => ({
          studentUserId: record.studentUserId,
          status: record.status
        }))
      });

      setAttendanceSession(saved);
      setSuccess(submitted ? "Attendance submitted." : "Attendance saved.");
    } catch (err) {
      setError(err.message || "Failed to save attendance.");
    } finally {
      setSaving(false);
    }
  }

  function closeModal() {
    setModalOpen(false);
    setSelectedEntry(null);
    setAttendanceSession(null);
    setSessionDate(todayAsInput());
  }

  return (
    <section className="contentPanel fadeIn attendancePage">
      <div className="sectionHeader">
        <span className="eyebrow">Teacher</span>
        <h1>Attendance</h1>
      </div>

      <div className="attendanceIntro">
        <div>
          <span className="studentScheduleLabel">Lecture tab</span>
          <h2>Open a lecture, then mark attendance</h2>
        </div>
        <div className="attendanceNote">
          <span>Lock rule</span>
          <strong>3 hours after submit</strong>
        </div>
      </div>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}
      {loading && <div className="infoBox">Loading attendance groups...</div>}

      <div className="attendanceGroupList">
        {scheduleByGroup.map((group) => (
          <article className="attendanceGroupCard" key={group.id}>
            <div className="groupCardHeader">
              <div>
                <h3>{group.name}</h3>
                <span className="groupMeta">{group.specializationName} · Year {group.studyYear}</span>
              </div>
              <span className="stateBadge state-active">{group.scheduleEntries.length} lessons</span>
            </div>

            <div className="groupMembers">
              <h4>Lessons</h4>
              {group.scheduleEntries.length === 0 ? (
                <div className="mutedText">No scheduled lessons yet.</div>
              ) : (
                <div className="attendanceLessonList">
                  {group.scheduleEntries.map((entry) => (
                    <div className="attendanceLessonRow" key={entry.id}>
                      <div className="attendanceLessonInfo">
                        <strong>{entry.subjectName}</strong>
                        <span className={`sessionPill session-${entry.sessionType ? entry.sessionType.toLowerCase() : "lecture"}`}>
                          {formatSessionType(entry.sessionType)}
                        </span>
                        <div className="mutedText">
                          {entry.nextSessionDate} · {dayLabels[entry.dayOfWeek] || entry.dayOfWeek} · {entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}
                        </div>
                      </div>

                      <button
                        type="button"
                        className="secondaryButton smallButton"
                        onClick={() => openAttendanceModal(entry)}
                      >
                        Set attendance
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </article>
        ))}
      </div>

      {modalOpen && selectedEntry && (
        <div className="attendanceModalOverlay" onClick={closeModal} role="presentation">
          <div className="attendanceModal" onClick={(event) => event.stopPropagation()}>
            <div className="attendanceModalHeader">
              <div>
                <span className="eyebrow">Attendance session</span>
                <h2>{selectedEntry.subjectName}</h2>
                <div className="mutedText">
                  {selectedEntry.studentGroupName} · {selectedEntry.nextSessionDate} · {dayLabels[selectedEntry.dayOfWeek] || selectedEntry.dayOfWeek} · {selectedEntry.startTime.slice(0, 5)} - {selectedEntry.endTime.slice(0, 5)}
                </div>
              </div>
              <button type="button" className="ghostButton" onClick={closeModal}>
                Close
              </button>
            </div>

            <div className="twoColumn attendanceModalMeta">
              <label>
                Session date
                <input
                  type="date"
                  value={sessionDate}
                  onChange={(event) => setSessionDate(event.target.value)}
                />
              </label>

              <div className="attendanceStatusBox">
                <span>Status</span>
                <strong>{attendanceSession?.locked ? "Locked" : attendanceSession?.submitted ? "Submitted" : "Draft"}</strong>
                <small>{attendanceSession?.submittedAt ? `Submitted at ${attendanceSession.submittedAt.slice(0, 16).replace("T", " ")}` : "Not submitted yet"}</small>
              </div>
            </div>

            <div className="buttonRow compactRow attendanceModalActions">
              <button
                type="button"
                className="secondaryButton smallButton"
                disabled={sessionLoading}
                onClick={() => loadAttendanceSession(selectedEntry.id, sessionDate)}
              >
                {sessionLoading ? "Loading..." : "Load session"}
              </button>
            </div>

            {sessionLoading && <div className="infoBox">Loading session...</div>}

            {attendanceSession && (
              <>
                <div className="attendanceLegend">
                  <span className="sessionPill attendanceLegendItem status-present">Present</span>
                  <span className="sessionPill attendanceLegendItem status-late">Late</span>
                  <span className="sessionPill attendanceLegendItem status-absent">Absent</span>
                </div>

                <div className="attendanceStudentList">
                  {attendanceSession.records.map((record) => (
                    <div className="attendanceStudentRow" key={record.studentUserId}>
                      <div>
                        <strong>{record.firstName} {record.lastName}</strong>
                        <div className="mutedText">{record.email}</div>
                      </div>

                      <div className="attendanceToggleGroup">
                        <label className="attendanceSwitch">
                          <input
                            type="checkbox"
                            checked={record.status !== "ABSENT"}
                            disabled={attendanceSession.locked}
                            onChange={(event) => setStudentPresent(record.studentUserId, event.target.checked)}
                          />
                          <span className="attendanceSwitchTrack">
                            <span className="attendanceSwitchLabel attendanceSwitchLabelLeft">Absent</span>
                            <span className="attendanceSwitchThumb" />
                            <span className="attendanceSwitchLabel attendanceSwitchLabelRight">Present</span>
                          </span>
                        </label>

                        <label className="attendanceLateToggle">
                          <input
                            type="checkbox"
                            checked={record.status === "LATE"}
                            disabled={attendanceSession.locked || record.status === "ABSENT"}
                            onChange={(event) => setStudentLate(record.studentUserId, event.target.checked)}
                          />
                          <span>Late</span>
                        </label>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="buttonRow attendanceModalFooter">
                  <button
                    type="button"
                    className="secondaryButton"
                    disabled={saving || attendanceSession.locked}
                    onClick={() => handleSave(false)}
                  >
                    {saving ? "Saving..." : "Save draft"}
                  </button>

                  <button
                    type="button"
                    className="primaryButton"
                    disabled={saving || attendanceSession.locked}
                    onClick={() => handleSave(true)}
                  >
                    {saving ? "Submitting..." : "Submit attendance"}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </section>
  );
}
