import { useEffect, useMemo, useState } from "react";
import Select from "react-select";
import {
  createTeacherAttachment,
  listTeacherAttachments,
  listTeacherSections
} from "../api/teacherApi.js";
import { darkSelectStyles } from "../components/selectStyles.js";

const attachmentOptions = [
  { value: "IMAGE", label: "Image" },
  { value: "MODEL", label: "Model" },
  { value: "DOCUMENT", label: "Document" },
  { value: "LINK", label: "Link" }
];

export default function TeacherMaterialsPage() {
  const [sections, setSections] = useState([]);
  const [attachments, setAttachments] = useState([]);
  const [form, setForm] = useState({
    sectionId: null,
    title: "",
    attachmentType: "DOCUMENT",
    resourceUrl: "",
    description: ""
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError("");

    try {
      const [sectionResult, attachmentResult] = await Promise.all([
        listTeacherSections(),
        listTeacherAttachments()
      ]);
      setSections(sectionResult || []);
      setAttachments(attachmentResult || []);
      setForm((current) => ({
        ...current,
        sectionId: current.sectionId ?? sectionResult?.[0]?.id ?? null
      }));
    } catch (err) {
      setError(err.message || "Failed to load materials.");
    } finally {
      setLoading(false);
    }
  }

  const sectionOptions = useMemo(
    () =>
      sections.map((section) => ({
        value: section.id,
        label: `${section.courseCode} - ${section.sectionCode}`
      })),
    [sections]
  );

  async function handleSave(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setSuccess("");

    try {
      await createTeacherAttachment(form);
      setForm((current) => ({
        ...current,
        title: "",
        resourceUrl: "",
        description: ""
      }));
      await loadData();
      setSuccess("Attachment saved.");
    } catch (err) {
      setError(err.message || "Failed to save attachment.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Teacher</span>
      <h1>Materials</h1>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}
      {loading && <div className="infoBox">Loading...</div>}

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Upload</span>
          <h2>Add attachment</h2>
        </div>

        <form className="courseSectionForm" onSubmit={handleSave}>
          <label>
            Section
            <Select
              className="appSelect"
              styles={darkSelectStyles}
              menuPortalTarget={document.body}
              options={sectionOptions}
              value={sectionOptions.find((option) => option.value === form.sectionId) ?? null}
              onChange={(option) => setForm((current) => ({ ...current, sectionId: option?.value ?? null }))}
              placeholder="Select section..."
            />
          </label>

          <label>
            Title
            <input value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} />
          </label>

          <label>
            Type
            <select value={form.attachmentType} onChange={(event) => setForm((current) => ({ ...current, attachmentType: event.target.value }))}>
              {attachmentOptions.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </label>

          <label className="fullSpan">
            Link or file URL
            <input
              value={form.resourceUrl}
              onChange={(event) => setForm((current) => ({ ...current, resourceUrl: event.target.value }))}
              placeholder="https://..."
            />
          </label>

          <label className="fullSpan">
            Description
            <textarea
              rows="3"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </label>

          <div className="fullSpan buttonRow">
            <button type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save Attachment"}
            </button>
          </div>
        </form>
      </div>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Attachments</span>
          <h2>Current materials</h2>
        </div>

        <div className="groupCardList">
          {attachments.length === 0 && <div className="infoBox">No attachments yet.</div>}

          {attachments.map((attachment) => (
            <article className="groupCard" key={attachment.id}>
              <div className="groupCardHeader">
                <div>
                  <h3>{attachment.title}</h3>
                  <span className="groupMeta">{attachment.courseName} · {attachment.sectionCode}</span>
                </div>
                <span className="stateBadge state-active">{attachment.attachmentType}</span>
              </div>
              <div className="mutedText">{attachment.description || "No description."}</div>
              <div className="materialLinkRow">
                <a href={attachment.resourceUrl} target="_blank" rel="noreferrer">{attachment.resourceUrl}</a>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
