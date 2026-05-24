import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Select from "react-select";
import { createTeacher, listFaculties } from "../api/adminApi.js";
import AppDateField from "../components/AppDateField.jsx";
import { darkSelectStyles } from "../components/selectStyles.js";

const initialForm = {
  email: "",
  password: "",
  finCode: "",
  serialNumber: "",
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  phoneNumber: "",
  address: "",
  facultyId: null,
  hoursTaught: 0
};

export default function CreateTeacherPage() {
  const [form, setForm] = useState(initialForm);
  const [faculties, setFaculties] = useState([]);
  const [loading, setLoading] = useState(false);
  const [facultiesLoading, setFacultiesLoading] = useState(true);
  const [createdUser, setCreatedUser] = useState(null);
  const [error, setError] = useState("");

  const facultyOptions = useMemo(
    () =>
      faculties.map((faculty) => ({
        value: faculty.id,
        label: `${faculty.code} - ${faculty.name}`
      })),
    [faculties]
  );

  useEffect(() => {
    loadFaculties();
  }, []);

  async function loadFaculties() {
    setFacultiesLoading(true);

    try {
      const result = await listFaculties();
      setFaculties(result || []);
    } catch (err) {
      setError(err.message || "Failed to load faculties.");
    } finally {
      setFacultiesLoading(false);
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
    setCreatedUser(null);

    try {
      if (!form.facultyId) {
        throw new Error("Please select a faculty.");
      }

      const payload = {
        ...form,
        facultyId: form.facultyId,
        hoursTaught: Number(form.hoursTaught || 0)
      };

      const result = await createTeacher(payload);
      setCreatedUser(result);
      setForm(initialForm);
    } catch (err) {
      setError(err.message || "Failed to create teacher.");
    } finally {
      setLoading(false);
    }
  }

  return (
      <section className="contentPanel fadeIn">
        <div className="pageTitleRow">
          <div>
            <span className="eyebrow">Admin</span>
            <h1>Create Teacher</h1>
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

        {createdUser && (
            <div className="successBox">
              Teacher created: {createdUser.email} ({createdUser.state})
            </div>
        )}

        {facultiesLoading && <div className="infoBox">Loading faculties...</div>}

        <form className="formGrid" onSubmit={handleSubmit}>
          <div className="twoColumn">
            <label>
              Email
              <input
                  type="email"
                  value={form.email}
                  onChange={(event) => updateField("email", event.target.value)}
                  required
              />
            </label>

            <label>
              Password
              <input
                  type="password"
                  value={form.password}
                  onChange={(event) => updateField("password", event.target.value)}
                  required
              />
            </label>

            <label>
              FIN
              <input
                  value={form.finCode}
                  onChange={(event) => updateField("finCode", event.target.value.toUpperCase())}
                  maxLength="7"
                  placeholder="AB12CD3"
                  required
              />
            </label>

            <label>
              Serial Number
              <input
                  value={form.serialNumber}
                  onChange={(event) => updateField("serialNumber", event.target.value.toUpperCase())}
                  placeholder="AZE1234567"
                  required
              />
            </label>

            <label>
              First Name
              <input
                  value={form.firstName}
                  onChange={(event) => updateField("firstName", event.target.value)}
                  required
              />
            </label>

            <label>
              Last Name
              <input
                  value={form.lastName}
                  onChange={(event) => updateField("lastName", event.target.value)}
                  required
              />
            </label>

            <label>
              Date of Birth
              <AppDateField
                  value={form.dateOfBirth}
                  onChange={(value) => updateField("dateOfBirth", value)}
                  placeholder="Pick date of birth"
              />
            </label>

            <label>
              Phone Number
              <input
                  value={form.phoneNumber}
                  onChange={(event) => updateField("phoneNumber", event.target.value)}
                  required
              />
            </label>

            <label>
              Hours Taught
              <input
                  type="number"
                  min="0"
                  value={form.hoursTaught}
                  onChange={(event) => updateField("hoursTaught", event.target.value)}
              />
            </label>
          </div>

          <label>
            Faculty
            <Select
                className="appSelect"
                classNamePrefix="appSelect"
                styles={darkSelectStyles}
                menuPortalTarget={document.body}
                options={facultyOptions}
                value={facultyOptions.find((option) => option.value === form.facultyId) ?? null}
                onChange={(option) => updateField("facultyId", option?.value ?? null)}
                placeholder="Search and select faculty..."
                isLoading={facultiesLoading}
                isDisabled={facultiesLoading}
                noOptionsMessage={() => "No faculties found"}
            />
          </label>

          <label>
            Address
            <textarea
                value={form.address}
                onChange={(event) => updateField("address", event.target.value)}
                required
            />
          </label>

          <div className="buttonRow">
            <button type="submit" disabled={loading}>
              {loading ? "Creating..." : "Create Teacher"}
            </button>
          </div>
        </form>
      </section>
  );
}
