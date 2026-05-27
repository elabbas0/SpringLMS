import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Select from "react-select";
import {
  createSpecialization,
  listFaculties,
  listSpecializations,
  searchUsers
} from "../api/adminApi.js";
import { darkSelectStyles } from "../components/selectStyles.js";

const initialForm = {
  name: "",
  facultyId: null,
  teacherUserId: null,
  semesterCreditTarget: 30
};

export default function CreateSpecializationPage() {
  const [form, setForm] = useState(initialForm);
  const [faculties, setFaculties] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [specializations, setSpecializations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const facultyOptions = useMemo(
    () => faculties.map((faculty) => ({ value: faculty.id, label: `${faculty.code} - ${faculty.name}` })),
    [faculties]
  );

  const teacherOptions = useMemo(
    () => teachers.map((teacher) => ({ value: teacher.id, label: teacher.email })),
    [teachers]
  );

  useEffect(() => {
    loadPageData();
  }, []);

  async function loadPageData() {
    setPageLoading(true);
    setError("");

    try {
      const [facultiesResult, teachersResult, specializationsResult] = await Promise.all([
        listFaculties(),
        searchUsers({ role: "TEACHER", state: "ACTIVE" }),
        listSpecializations()
      ]);

      setFaculties(facultiesResult || []);
      setTeachers(teachersResult || []);
      setSpecializations(specializationsResult || []);
    } catch (err) {
      setError(err.message || "Failed to load specialization page.");
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
      if (!form.facultyId) {
        throw new Error("Please select a faculty.");
      }

      if (!form.teacherUserId) {
        throw new Error("Please select a teacher.");
      }

      const created = await createSpecialization(form);
      setSpecializations((current) => [...current, created].sort((a, b) => a.name.localeCompare(b.name)));
      setSuccess(`Specialization created: ${created.name}`);
      setForm(initialForm);
    } catch (err) {
      setError(err.message || "Failed to create specialization.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <div className="pageTitleRow">
        <div>
          <span className="eyebrow">Admin</span>
          <h1>Create Specialization</h1>
        </div>

        <div className="buttonRow compactRow">
          <Link className="secondaryButton smallButton" to="/admin/faculties/create">
            Create Faculty
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
            Specialization Name
            <input
              value={form.name}
              onChange={(event) => updateField("name", event.target.value)}
              placeholder="Computer Science"
              required
            />
          </label>

          <label>
            Faculty
            <Select
              className="appSelect"
              styles={darkSelectStyles}
              menuPortalTarget={document.body}
              options={facultyOptions}
              value={facultyOptions.find((option) => option.value === form.facultyId) ?? null}
              onChange={(option) => updateField("facultyId", option?.value ?? null)}
              placeholder="Select faculty..."
              noOptionsMessage={() => "No faculties"}
            />
          </label>
        </div>

        <label>
          Semester Credit Target
          <input
            type="number"
            min="1"
            max="60"
            value={form.semesterCreditTarget}
            onChange={(event) => updateField("semesterCreditTarget", Number(event.target.value))}
            required
          />
        </label>

        <label>
          Assigned Teacher
          <Select
            className="appSelect"
            styles={darkSelectStyles}
            menuPortalTarget={document.body}
            options={teacherOptions}
            value={teacherOptions.find((option) => option.value === form.teacherUserId) ?? null}
            onChange={(option) => updateField("teacherUserId", option?.value ?? null)}
            placeholder="Select teacher..."
            noOptionsMessage={() => "No active teachers"}
          />
        </label>

        <div className="buttonRow">
          <button type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Specialization"}
          </button>
        </div>
      </form>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Specializations</span>
          <h2>Current list</h2>
        </div>

        <div className="tableWrap">
          <table className="adminTable">
            <thead>
              <tr>
                <th>Name</th>
                <th>Faculty</th>
                <th>Credits</th>
                <th>Assigned Teacher</th>
              </tr>
            </thead>
            <tbody>
              {specializations.length === 0 && (
                <tr>
                  <td colSpan="4">No specializations yet.</td>
                </tr>
              )}

              {specializations.map((specialization) => (
                <tr key={specialization.id}>
                  <td>{specialization.name}</td>
                  <td>{specialization.facultyName}</td>
                  <td>{specialization.semesterCreditTarget}</td>
                  <td>{specialization.teacherEmail || "Unassigned"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
