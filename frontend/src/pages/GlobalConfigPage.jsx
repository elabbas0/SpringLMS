import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getGlobalConfig, updateGlobalConfig } from "../api/adminApi.js";

const initialForm = {
  absenceValue: "1",
  lateValue: "0.5",
  minimumAssignedGroupsForAutoSchedule: "2",
  lectureLengthMinutes: "80",
  year1StartTime: "09:00",
  year1EndTime: "13:20",
  year2StartTime: "09:00",
  year2EndTime: "15:00",
  year3StartTime: "09:00",
  year3EndTime: "16:30",
  year4StartTime: "09:00",
  year4EndTime: "18:00"
};

export default function GlobalConfigPage() {
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadConfig();
  }, []);

  async function loadConfig() {
    setPageLoading(true);
    setError("");

    try {
      const config = await getGlobalConfig();
      if (config) {
        setForm({
          absenceValue: String(config.absenceValue ?? "1"),
          lateValue: String(config.lateValue ?? "0.5"),
          minimumAssignedGroupsForAutoSchedule: String(config.minimumAssignedGroupsForAutoSchedule ?? "2"),
          lectureLengthMinutes: String(config.lectureLengthMinutes ?? "80"),
          year1StartTime: (config.year1StartTime || "09:00").slice(0, 5),
          year1EndTime: (config.year1EndTime || "13:20").slice(0, 5),
          year2StartTime: (config.year2StartTime || "09:00").slice(0, 5),
          year2EndTime: (config.year2EndTime || "15:00").slice(0, 5),
          year3StartTime: (config.year3StartTime || "09:00").slice(0, 5),
          year3EndTime: (config.year3EndTime || "16:30").slice(0, 5),
          year4StartTime: (config.year4StartTime || "09:00").slice(0, 5),
          year4EndTime: (config.year4EndTime || "18:00").slice(0, 5)
        });
      }
    } catch (err) {
      setError(err.message || "Failed to load config.");
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

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const payload = {
        ...form,
        absenceValue: Number(form.absenceValue),
        lateValue: Number(form.lateValue),
        minimumAssignedGroupsForAutoSchedule: Number(form.minimumAssignedGroupsForAutoSchedule),
        lectureLengthMinutes: Number(form.lectureLengthMinutes)
      };

      const saved = await updateGlobalConfig(payload);
      setForm({
        absenceValue: String(saved.absenceValue),
        lateValue: String(saved.lateValue),
        minimumAssignedGroupsForAutoSchedule: String(saved.minimumAssignedGroupsForAutoSchedule),
        lectureLengthMinutes: String(saved.lectureLengthMinutes),
        year1StartTime: saved.year1StartTime.slice(0, 5),
        year1EndTime: saved.year1EndTime.slice(0, 5),
        year2StartTime: saved.year2StartTime.slice(0, 5),
        year2EndTime: saved.year2EndTime.slice(0, 5),
        year3StartTime: saved.year3StartTime.slice(0, 5),
        year3EndTime: saved.year3EndTime.slice(0, 5),
        year4StartTime: saved.year4StartTime.slice(0, 5),
        year4EndTime: saved.year4EndTime.slice(0, 5)
      });
      setSuccess("Global config updated.");
    } catch (err) {
      setError(err.message || "Failed to save config.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <div className="pageTitleRow">
        <div>
          <span className="eyebrow">Admin</span>
          <h1>Global Config</h1>
        </div>

        <div className="buttonRow compactRow">
          <Link className="secondaryButton smallButton" to="/admin/faculties/groups">
            Student Groups
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
        <div className="twoColumn">
          <label>
            Absence Value
            <input
              type="number"
              step="0.1"
              min="0"
              value={form.absenceValue}
              onChange={(event) => updateField("absenceValue", event.target.value)}
            />
          </label>

          <label>
            Late Value
            <input
              type="number"
              step="0.1"
              min="0"
              value={form.lateValue}
              onChange={(event) => updateField("lateValue", event.target.value)}
            />
          </label>
        </div>

        <div className="twoColumn">
          <label>
            Minimum Assigned Groups For Auto Schedule
            <input
              type="number"
              min="1"
              value={form.minimumAssignedGroupsForAutoSchedule}
              onChange={(event) => updateField("minimumAssignedGroupsForAutoSchedule", event.target.value)}
            />
          </label>

          <label>
            Lecture Length (minutes)
            <input
              type="number"
              min="30"
              value={form.lectureLengthMinutes}
              onChange={(event) => updateField("lectureLengthMinutes", event.target.value)}
            />
          </label>
        </div>

        <div className="adminSection">
          <div className="sectionHeader">
            <span className="eyebrow">Study Year Windows</span>
            <h2>Allowed lecture hours</h2>
          </div>

          {[1, 2, 3, 4].map((year) => (
            <div className="twoColumn" key={year}>
              <label>
                Year {year} Start
                <input
                  type="time"
                  value={form[`year${year}StartTime`]}
                  onChange={(event) => updateField(`year${year}StartTime`, event.target.value)}
                />
              </label>

              <label>
                Year {year} End
                <input
                  type="time"
                  value={form[`year${year}EndTime`]}
                  onChange={(event) => updateField(`year${year}EndTime`, event.target.value)}
                />
              </label>
            </div>
          ))}
        </div>

        <div className="buttonRow">
          <button type="submit" disabled={loading}>
            {loading ? "Saving..." : "Save Config"}
          </button>
        </div>
      </form>
    </section>
  );
}
