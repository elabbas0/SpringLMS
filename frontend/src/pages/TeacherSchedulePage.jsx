import { useEffect, useState } from "react";
import { listTeacherSchedule, updateTeacherScheduleEntry } from "../api/teacherApi.js";

const dayOptions = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"];

export default function TeacherSchedulePage() {
  const [schedule, setSchedule] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [draftById, setDraftById] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadSchedule();
  }, []);

  async function loadSchedule() {
    setLoading(true);
    setError("");

    try {
      const result = await listTeacherSchedule();
      setSchedule(result || []);
    } catch (err) {
      setError(err.message || "Failed to load schedule.");
    } finally {
      setLoading(false);
    }
  }

  function beginEdit(entry) {
    setEditingId(entry.id);
    setDraftById((current) => ({
      ...current,
      [entry.id]: {
        dayOfWeek: entry.dayOfWeek,
        startTime: entry.startTime.slice(0, 5)
      }
    }));
  }

  function updateDraft(entryId, field, value) {
    setDraftById((current) => ({
      ...current,
      [entryId]: {
        ...current[entryId],
        [field]: value
      }
    }));
  }

  async function handleSave(entryId) {
    setError("");
    setSuccess("");

    try {
      const draft = draftById[entryId];
      const updated = await updateTeacherScheduleEntry(entryId, draft);
      setSchedule((current) => current.map((entry) => (entry.id === updated.id ? updated : entry)));
      setEditingId(null);
      setSuccess("Lecture time updated.");
    } catch (err) {
      setError(err.message || "Failed to update lecture time.");
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Teacher</span>
      <h1>Schedule</h1>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}
      {loading && <div className="infoBox">Loading...</div>}

      <div className="tableWrap">
        <table className="adminTable">
          <thead>
            <tr>
              <th>Group</th>
              <th>Subject</th>
              <th>Type</th>
              <th>Day</th>
              <th>Start</th>
              <th>End</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {!loading && schedule.length === 0 && (
              <tr>
                <td colSpan="7">No schedule yet.</td>
              </tr>
            )}

            {schedule.map((entry) => {
              const draft = draftById[entry.id] || {
                dayOfWeek: entry.dayOfWeek,
                startTime: entry.startTime.slice(0, 5)
              };

              return (
                <tr key={entry.id}>
                  <td>{entry.studentGroupName}</td>
                  <td>{entry.subjectName}</td>
                  <td>{entry.sessionType}</td>
                  <td>
                    {editingId === entry.id ? (
                      <select
                        value={draft.dayOfWeek}
                        onChange={(event) => updateDraft(entry.id, "dayOfWeek", event.target.value)}
                      >
                        {dayOptions.map((day) => (
                          <option value={day} key={day}>
                            {day}
                          </option>
                        ))}
                      </select>
                    ) : (
                      entry.dayOfWeek
                    )}
                  </td>
                  <td>
                    {editingId === entry.id ? (
                      <input
                        type="time"
                        value={draft.startTime}
                        onChange={(event) => updateDraft(entry.id, "startTime", event.target.value)}
                      />
                    ) : (
                      entry.startTime.slice(0, 5)
                    )}
                  </td>
                  <td>{entry.endTime.slice(0, 5)}</td>
                  <td>
                    {editingId === entry.id ? (
                      <div className="inlineActionRow">
                        <button type="button" className="secondaryButton" onClick={() => handleSave(entry.id)}>
                          Save
                        </button>
                        <button type="button" className="ghostButton" onClick={() => setEditingId(null)}>
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <button type="button" className="secondaryButton" onClick={() => beginEdit(entry)}>
                        Change Time
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}
