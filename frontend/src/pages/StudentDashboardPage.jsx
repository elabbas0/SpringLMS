import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getStudentAttendanceSummary, listStudentSchedule } from "../api/studentApi.js";

export default function StudentDashboardPage() {
  const [summary, setSummary] = useState(null);
  const [schedule, setSchedule] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    setLoading(true);
    setError("");

    try {
      const [summaryResult, scheduleResult] = await Promise.all([
        getStudentAttendanceSummary(),
        listStudentSchedule()
      ]);

      setSummary(summaryResult);
      setSchedule(scheduleResult || []);
    } catch (err) {
      setError(err.message || "Failed to load dashboard.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Student</span>
      <h1>Dashboard</h1>

      {error && <div className="errorBox">{error}</div>}
      {loading && <div className="infoBox">Loading dashboard...</div>}

      {!loading && summary && (
        <>
          <div className="statGrid">
            <article className="statCard">
              <span>Attendance</span>
              <strong>{Number(summary.attendancePercentage ?? 0).toFixed(1)}%</strong>
            </article>
            <article className="statCard">
              <span>Lectures</span>
              <strong>{summary.recordedSessions ?? 0}</strong>
            </article>
            <article className="statCard">
              <span>Remaining</span>
              <strong>{summary.remainingLectureCount ?? 0}</strong>
            </article>
            <article className="statCard">
              <span>Absence left</span>
              <strong>{Number(summary.remainingAbsenceEquivalentUntilFinalExam ?? 0).toFixed(1)}</strong>
            </article>
          </div>

          <div className="adminSection">
            <div className="sectionHeader">
              <span className="eyebrow">Quick Links</span>
              <h2>Open your pages</h2>
            </div>

            <div className="buttonRow compactRow">
              <Link className="primaryButton smallButton" to="/student/schedule">Schedule</Link>
              <Link className="secondaryButton smallButton" to="/student/materials">Materials</Link>
              <Link className="secondaryButton smallButton" to="/student/attendance">Attendance</Link>
            </div>
          </div>

          <div className="adminSection">
            <div className="sectionHeader">
              <span className="eyebrow">Today</span>
              <h2>Next lessons</h2>
            </div>

            <div className="groupCardList">
              {schedule.slice(0, 3).map((entry) => (
                <article className="groupCard" key={entry.id}>
                  <div className="groupCardHeader">
                    <div>
                      <h3>{entry.subjectName}</h3>
                      <span className="groupMeta">{entry.studentGroupName} · {entry.nextSessionDate}</span>
                    </div>
                    <span className="stateBadge state-active">{entry.sessionType}</span>
                  </div>
                  <div className="lessonMetaRow">
                    <span>{entry.dayOfWeek}</span>
                    <span>{entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}</span>
                  </div>
                </article>
              ))}
              {schedule.length === 0 && <div className="infoBox">No schedule yet.</div>}
            </div>
          </div>
        </>
      )}
    </section>
  );
}
