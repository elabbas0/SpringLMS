import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { createFaculty, listFaculties } from "../api/adminApi.js";

const initialForm = {
  name: "",
  code: ""
};

export default function CreateFacultyPage() {
  const [form, setForm] = useState(initialForm);
  const [faculties, setFaculties] = useState([]);
  const [loading, setLoading] = useState(false);
  const [listLoading, setListLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadFaculties();
  }, []);

  async function loadFaculties() {
    setListLoading(true);

    try {
      const result = await listFaculties();
      setFaculties(result || []);
    } catch (err) {
      setError(err.message || "Failed to load faculties.");
    } finally {
      setListLoading(false);
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
      const createdFaculty = await createFaculty(form);
      setFaculties((current) => [...current, createdFaculty].sort((a, b) => a.name.localeCompare(b.name)));
      setSuccess(`Faculty created: ${createdFaculty.name} (${createdFaculty.code})`);
      setForm(initialForm);
    } catch (err) {
      setError(err.message || "Failed to create faculty.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <div className="pageTitleRow">
        <div>
          <span className="eyebrow">Admin</span>
          <h1>Create Faculty</h1>
          <p>Create faculties like ITT before assigning teachers and groups.</p>
        </div>

        <Link className="secondaryButton smallButton" to="/admin/teachers/create">
          Create Teacher
        </Link>
      </div>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}

      <form className="formGrid" onSubmit={handleSubmit}>
        <div className="twoColumn">
          <label>
            Faculty Name
            <input
              value={form.name}
              onChange={(event) => updateField("name", event.target.value)}
              placeholder="Information Technologies and Telecommunications"
              required
            />
          </label>

          <label>
            Faculty Code
            <input
              value={form.code}
              onChange={(event) => updateField("code", event.target.value)}
              placeholder="ITT"
              required
            />
          </label>
        </div>

        <div className="buttonRow">
          <button type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Faculty"}
          </button>
        </div>
      </form>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Available Faculties</span>
          <h2>Current faculty list</h2>
          <p>These options will appear in the teacher creation dropdown.</p>
        </div>

        {listLoading ? (
          <div className="infoBox">Loading faculties...</div>
        ) : faculties.length === 0 ? (
          <div className="infoBox">No faculties created yet.</div>
        ) : (
          <div className="tableWrap">
            <table className="adminTable">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Code</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {faculties.map((faculty) => (
                  <tr key={faculty.id}>
                    <td>{faculty.id}</td>
                    <td>{faculty.name}</td>
                    <td>{faculty.code}</td>
                    <td>{faculty.archived ? "Archived" : "Active"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
