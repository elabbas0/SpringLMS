import { useEffect, useMemo, useState } from "react";
import { getStudentAttendanceSummary, listStudentAttendanceEntries } from "../api/studentApi.js";

function formatPercent(value) {
  if (value === null || value === undefined) {
    return "0%";
  }

  return `${Number(value).toFixed(1)}%`;
}

function formatDay(value) {
  const map = {
    MONDAY: "Monday",
    TUESDAY: "Tuesday",
    WEDNESDAY: "Wednesday",
    THURSDAY: "Thursday",
    FRIDAY: "Friday",
    SATURDAY: "Saturday",
    SUNDAY: "Sunday"
  };

  return map[value] || value || "";
}

function formatSessionType(value) {
  if (!value) return "";
  if (value === "LABORATORY") return "Lab";
  return value[0] + value.slice(1).toLowerCase();
}

function statusOrder(status) {
  if (status === "PRESENT") return 0;
  if (status === "LATE") return 1;
  if (status === "EXCUSED") return 2;
  return 3;
}

export default function StudentAttendancePage() {
  const [summary, setSummary] = useState(null);
  const [entries, setEntries] = useState([]);
  const [selectedSubject, setSelectedSubject] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadSummary();
  }, []);

  async function loadSummary() {
    setLoading(true);
    setError("");

    try {
      const [summaryResult, entriesResult] = await Promise.all([
        getStudentAttendanceSummary(),
        listStudentAttendanceEntries()
      ]);

      setSummary(summaryResult);
      setEntries(entriesResult || []);
      setSelectedSubject((current) => current || (entriesResult?.[0]?.subjectName ?? ""));
    } catch (err) {
      setError(err.message || "Failed to load attendance summary.");
    } finally {
      setLoading(false);
    }
  }

  const attendancePercent = summary?.attendancePercentage ?? 0;
  const remainingBeforeRestriction = summary?.remainingAbsenceEquivalentUntilFinalExam ?? 0;

  const subjectCards = useMemo(() => {
    const cards = new Map();

    entries.forEach((entry) => {
      if (!cards.has(entry.subjectName)) {
        cards.set(entry.subjectName, {
          subjectName: entry.subjectName,
          teacherName: entry.teacherName,
          types: new Set(),
          total: 0,
          present: 0,
          late: 0,
          absent: 0,
          lastSessionDate: entry.sessionDate,
          lastDayOfWeek: entry.dayOfWeek
        });
      }

      const card = cards.get(entry.subjectName);
      card.types.add(entry.sessionType);
      card.total += 1;
      card.lastSessionDate = entry.sessionDate;
      card.lastDayOfWeek = entry.dayOfWeek;

      if (entry.status === "PRESENT") card.present += 1;
      else if (entry.status === "LATE") card.late += 1;
      else if (entry.status === "ABSENT") card.absent += 1;
      else card.absent += 1;
    });

    return Array.from(cards.values()).map((card) => ({
      ...card,
      types: Array.from(card.types)
    }));
  }, [entries]);

  const selectedSubjectEntries = useMemo(() => {
    if (!selectedSubject) {
      return entries;
    }

    return entries.filter((entry) => entry.subjectName === selectedSubject);
  }, [entries, selectedSubject]);

  const groupedByType = useMemo(() => {
    const buckets = new Map();

    selectedSubjectEntries.forEach((entry) => {
      if (!buckets.has(entry.sessionType)) {
        buckets.set(entry.sessionType, []);
      }

      buckets.get(entry.sessionType).push(entry);
    });

    return Array.from(buckets.entries())
      .map(([sessionType, items]) => ({
        sessionType,
        items: items.sort((a, b) => a.sessionDate.localeCompare(b.sessionDate))
      }))
      .sort((a, b) => statusOrder(a.sessionType) - statusOrder(b.sessionType));
  }, [selectedSubjectEntries]);

  const selectedSubjectStats = useMemo(() => {
    return selectedSubjectEntries.reduce(
      (acc, entry) => {
        acc.total += 1;
        if (entry.status === "PRESENT") acc.present += 1;
        else if (entry.status === "LATE") acc.late += 1;
        else if (entry.status === "EXCUSED") acc.excused += 1;
        else acc.absent += 1;
        return acc;
      },
      { total: 0, present: 0, late: 0, absent: 0, excused: 0 }
    );
  }, [selectedSubjectEntries]);

  return (
    <section className="contentPanel fadeIn studentAttendancePage">
      <div className="sectionHeader">
        <span className="eyebrow">Student</span>
        <h1>Attendance</h1>
      </div>

      <div className="studentAttendanceHero">
        <div>
          <span className="studentScheduleLabel">Semester progress</span>
          <h2>{formatPercent(attendancePercent)}</h2>
        </div>

        <div className="studentAttendanceStat">
          <span>Final exam threshold</span>
          <strong>{summary?.minimumAttendancePercentForFinalExam ?? 0}%</strong>
          <small>{summary?.semesterWeeks ?? 0} weeks</small>
        </div>
      </div>

      {error && <div className="errorBox">{error}</div>}
      {loading && <div className="infoBox">Loading attendance...</div>}

      {!loading && !error && summary && (
        <div className="attendanceMetrics">
          <article className="metricCard">
            <span>Attendance percentage</span>
            <strong>{formatPercent(attendancePercent)}</strong>
          </article>

          <article className="metricCard">
            <span>Lectures delivered</span>
            <strong>{summary.recordedSessions}</strong>
            <small>of {summary.totalExpectedSessions}</small>
          </article>

          <article className="metricCard">
            <span>Remaining lectures</span>
            <strong>{summary.remainingLectureCount}</strong>
            <small>for the semester</small>
          </article>

          <article className="metricCard">
            <span>Allowed absence left</span>
            <strong>{Number(remainingBeforeRestriction).toFixed(1)}</strong>
            <small>lecture equivalents until final exam restriction</small>
          </article>
        </div>
      )}

      {!loading && !error && (
        <div className="scheduleBlock">
          <div className="sectionHeader">
            <span className="eyebrow">Attendance Log</span>
            <h2>Subjects</h2>
          </div>

          {!selectedSubject ? (
            <div className="lectureCardGrid">
              {subjectCards.length === 0 && <div className="infoBox">No attendance records yet.</div>}

              {subjectCards.map((subject) => (
                <button
                  type="button"
                  key={subject.subjectName}
                  className="lectureCardButton"
                  onClick={() => setSelectedSubject(subject.subjectName)}
                >
                  <div className="lectureCardTop">
                    <div>
                      <span className="studentScheduleLabel">
                        {subject.types.length > 1 ? `${subject.types.length} session types` : formatSessionType(subject.types[0])}
                      </span>
                      <h3>{subject.subjectName}</h3>
                    </div>
                    <span className="sessionPill attendanceLegendItem status-present">
                      {subject.total} session{subject.total === 1 ? "" : "s"}
                    </span>
                  </div>

                  <div className="lectureCardMeta">
                    <span>{subject.teacherName}</span>
                    <span>{subject.lastSessionDate} · {formatDay(subject.lastDayOfWeek)}</span>
                  </div>

                  <div className="lectureCardStats">
                    <span>Present {subject.present}</span>
                    <span>Late {subject.late}</span>
                    <span>Absent {subject.absent}</span>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="attendanceDetailView">
              <button
                type="button"
                className="ghostButton smallButton attendanceBackButton"
                onClick={() => setSelectedSubject("")}
              >
                Back
              </button>

              <div className="attendanceDetailHeader">
                <div>
                  <span className="studentScheduleLabel">Selected subject</span>
                  <h3>{selectedSubject}</h3>
                  <p>
                    {selectedSubjectStats.total} session{selectedSubjectStats.total === 1 ? "" : "s"}
                  </p>
                </div>

                <div className="attendanceLectureStats">
                  <span>Present</span>
                  <strong>{selectedSubjectStats.present}</strong>
                  <small>
                    Late {selectedSubjectStats.late} · Absent {selectedSubjectStats.absent}
                  </small>
                </div>
              </div>

              <div className="attendanceTypeSections">
                {groupedByType.map((group) => (
                  <article className="attendanceTypeCard" key={group.sessionType}>
                    <div className="attendanceTypeHeader">
                      <div>
                        <span className="studentScheduleLabel">{formatSessionType(group.sessionType)}</span>
                        <h4>{group.items.length} session{group.items.length === 1 ? "" : "s"}</h4>
                      </div>
                      <span className="sessionPill attendanceLegendItem status-present">
                        P {group.items.filter((item) => item.status === "PRESENT").length}
                      </span>
                    </div>

                    <div className="tableWrap">
                      <table className="adminTable">
                        <thead>
                          <tr>
                            <th>Date</th>
                            <th>Day</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {group.items.map((entry) => (
                            <tr key={entry.attendanceSessionId}>
                              <td>{entry.sessionDate}</td>
                              <td>{formatDay(entry.dayOfWeek)}</td>
                              <td>
                                <span className={`sessionPill attendanceLegendItem status-${entry.status?.toLowerCase()}`}>
                                  {entry.status}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {!loading && !error && !summary && <div className="infoBox">No attendance data yet.</div>}
    </section>
  );
}
