import { useState } from "react";
import { Link } from "react-router-dom";
import { registerStudent } from "../api/authApi.js";
import AppDateField from "../components/AppDateField.jsx";
import StepCard from "../components/StepCard.jsx";

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
  entranceScore: "",
  studyYear: "",
  admissionYear: "",
  fundingType: "STATE_FUNDED",
  emergencyContactName: "",
  emergencyContactPhone: "",
  emergencyContactRelation: ""
};

export default function StudentRegisterPage() {
  const [form, setForm] = useState(initialForm);
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [createdUser, setCreatedUser] = useState(null);
  const [error, setError] = useState("");

  function updateField(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value
    }));
  }

  function nextStep(event) {
    event.preventDefault();
    setError("");
    setStep(2);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");

    try {
      const payload = {
        ...form,
        entranceScore: form.entranceScore === "" ? null : Number(form.entranceScore),
        studyYear: form.studyYear === "" ? null : Number(form.studyYear),
        admissionYear: form.admissionYear === "" ? null : Number(form.admissionYear)
      };

      const result = await registerStudent(payload);
      setCreatedUser(result);
      setMessage("Registration submitted. Waiting for approval.");
      setForm(initialForm);
      setStep(1);
    } catch (err) {
      setError(err.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="centeredPage">
      <div className="stepWrap">
        <div className="stepIndicator">
          <span className={step === 1 ? "activeStep" : ""}>1. Required</span>
          <span className={step === 2 ? "activeStep" : ""}>2. Details</span>
        </div>

        {step === 1 && (
          <StepCard
            title="Student Registration"
          >
            <form onSubmit={nextStep} className="formGrid">
              <label>
                Email
                <input name="email" type="email" value={form.email} onChange={updateField} required />
              </label>

              <label>
                Password
                <input name="password" type="password" value={form.password} onChange={updateField} required />
              </label>

              <div className="twoColumn">
                <label>
                  FIN
                  <input
                    name="finCode"
                    value={form.finCode}
                    onChange={updateField}
                    placeholder="AB12CD3"
                    maxLength="7"
                    required
                  />
                </label>

                <label>
                  Serial Number
                  <input
                    name="serialNumber"
                    value={form.serialNumber}
                    onChange={updateField}
                    placeholder="AZE1234567"
                    required
                  />
                </label>
              </div>

              <div className="twoColumn">
                <label>
                  First Name
                  <input name="firstName" value={form.firstName} onChange={updateField} required />
                </label>

                <label>
                  Last Name
                  <input name="lastName" value={form.lastName} onChange={updateField} required />
                </label>
              </div>

              <label>
                Date of Birth
                <AppDateField
                  value={form.dateOfBirth}
                  onChange={(value) => updateField({ target: { name: "dateOfBirth", value } })}
                  placeholder="Pick date of birth"
                />
              </label>

              <button className="primaryButton">Next</button>
            </form>
          </StepCard>
        )}

        {step === 2 && (
          <StepCard
            title="Student Registration"
          >
            <form onSubmit={handleSubmit} className="formGrid">
              <label>
                Phone Number
                <input
                  name="phoneNumber"
                  value={form.phoneNumber}
                  onChange={updateField}
                  placeholder="+123456789"
                  required
                />
              </label>

              <label>
                Address
                <textarea
                  name="address"
                  value={form.address}
                  onChange={updateField}
                  rows="3"
                  required
                />
              </label>

              <div className="twoColumn">
                <label>
                  Entrance Score
                  <input
                    name="entranceScore"
                    type="number"
                    step="0.1"
                    min="0"
                    value={form.entranceScore}
                    onChange={updateField}
                    required
                  />
                </label>

                <label>
                  Study Year
                  <input
                    name="studyYear"
                    type="number"
                    min="1"
                    value={form.studyYear}
                    onChange={updateField}
                    required
                  />
                </label>
              </div>

              <div className="twoColumn">
                <label>
                  Admission Year
                  <input
                    name="admissionYear"
                    type="number"
                    min="1900"
                    value={form.admissionYear}
                    onChange={updateField}
                    required
                  />
                </label>

                <label>
                  Funding Type
                  <select name="fundingType" value={form.fundingType} onChange={updateField} required>
                    <option value="STATE_FUNDED">State Funded</option>
                    <option value="PERSONAL_CREDIT">Personal Credit</option>
                  </select>
                </label>
              </div>

              <label>
                Emergency Contact Name
                <input name="emergencyContactName" value={form.emergencyContactName} onChange={updateField} />
              </label>

              <div className="twoColumn">
                <label>
                  Emergency Phone
                  <input name="emergencyContactPhone" value={form.emergencyContactPhone} onChange={updateField} />
                </label>

                <label>
                  Relation
                  <input name="emergencyContactRelation" value={form.emergencyContactRelation} onChange={updateField} />
                </label>
              </div>

              {error && <div className="errorBox">{error}</div>}
              {message && <div className="successBox">{message}</div>}

              {createdUser && (
                <div className="resultBox">
                  <strong>{createdUser.email}</strong>
                  <span>{createdUser.role} · {createdUser.state}</span>
                </div>
              )}

              <div className="buttonRow">
                <button type="button" className="secondaryButton" onClick={() => setStep(1)}>
                  Back
                </button>

                <button disabled={loading} className="primaryButton">
                  {loading ? "Submitting..." : "Submit Registration"}
                </button>
              </div>

              <p className="mutedText">
                Already registered? <Link to="/login">Go to login</Link>
              </p>
            </form>
          </StepCard>
        )}
      </div>
    </div>
  );
}
