import { useEffect, useMemo, useState } from "react";
import { listStudentSchedule } from "../api/studentApi.js";

const dayOrder = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"];
const dayLabels = {
  MONDAY: "Monday",
  TUESDAY: "Tuesday",
  WEDNESDAY: "Wednesday",
  THURSDAY: "Thursday",
  FRIDAY: "Friday"
};

function formatSessionType(sessionType) {
  if (!sessionType) return "";
  if (sessionType === "LABORATORY") return "Lab";
  return sessionType[0] + sessionType.slice(1).toLowerCase();
}

export default function StudentSchedulePage() {
  const [schedule, setSchedule] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadSchedule();
  }, []);

  async function loadSchedule() {
    setLoading(true);
    setError("");

    try {
      const result = await listStudentSchedule();
      setSchedule(result || []);
    } catch (err) {
      setError(err.message || "Failed to load schedule.");
    } finally {
      setLoading(false);
    }
  }

  const groupedSchedule = useMemo(() => {
    return dayOrder.map((day) => ({
      day,
      lessons: schedule.filter((entry) => entry.dayOfWeek === day)
    }));
  }, [schedule]);

  const totalLessons = schedule.length;

  return (
    <section className="contentPanel fadeIn studentSchedulePage">
      <div className="sectionHeader">
        <span className="eyebrow">Student</span>
        <h1>Schedule</h1>
      </div>

      <div className="studentScheduleHero">
        <div>
          <span className="studentScheduleLabel">Weekly overview</span>
          <h2>{totalLessons} lessons</h2>
          <p>Classes are grouped by weekday so the week stays readable.</p>
        </div>
        <div className="studentScheduleStat">
          <span>Visible days</span>
          <strong>{groupedSchedule.filter((day) => day.lessons.length > 0).length}</strong>
        </div>
      </div>

      {error && <div className="errorBox">{error}</div>}
      {loading && <div className="infoBox">Loading schedule...</div>}

      {!loading && !error && totalLessons === 0 && <div className="infoBox">No schedule yet.</div>}

      <div className="studentScheduleGrid">
        {groupedSchedule.map(({ day, lessons }) => (
          <article className="dayCard" key={day}>
            <div className="dayCardHeader">
              <div>
                <span className="studentScheduleLabel">{dayLabels[day]}</span>
                <h3>{lessons.length ? `${lessons.length} lesson${lessons.length === 1 ? "" : "s"}` : "Free day"}</h3>
              </div>
            </div>

            <div className="lessonStack">
              {lessons.length === 0 && <div className="mutedText">No classes scheduled.</div>}

              {lessons.map((entry) => (
                <div className="lessonCard" key={entry.id}>
                  <div className="lessonTopRow">
                    <strong>{entry.subjectName}</strong>
                    <span className={`sessionPill session-${entry.sessionType ? entry.sessionType.toLowerCase() : "lecture"}`}>
                      {formatSessionType(entry.sessionType)}
                    </span>
                  </div>

                  <div className="lessonMetaRow">
                    <span>{entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}</span>
                    <span>{entry.teacherName}</span>
                  </div>

                  <div className="lessonDateRow">{entry.nextSessionDate} · {dayLabels[entry.dayOfWeek]}</div>
                  <div className="lessonGroupMeta">{entry.studentGroupName}</div>
                </div>
              ))}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
