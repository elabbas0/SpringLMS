import { useEffect, useState } from "react";
import { listStudentMaterials } from "../api/studentApi.js";

function formatType(value) {
  if (!value) return "";
  if (value === "LABORATORY") return "Lab";
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export default function StudentMaterialsPage() {
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadMaterials();
  }, []);

  async function loadMaterials() {
    setLoading(true);
    setError("");

    try {
      const result = await listStudentMaterials();
      setSections(result || []);
    } catch (err) {
      setError(err.message || "Failed to load materials.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Student</span>
      <h1>Materials</h1>

      {error && <div className="errorBox">{error}</div>}
      {loading && <div className="infoBox">Loading...</div>}
      {!loading && !error && sections.length === 0 && <div className="infoBox">No materials yet.</div>}

      <div className="groupCardList">
        {sections.map((section) => (
          <article className="groupCard" key={section.sectionId}>
            <div className="groupCardHeader">
              <div>
                <h3>{section.courseName}</h3>
                <span className="groupMeta">{section.courseCode} · {section.sectionCode}</span>
              </div>
            </div>

            <div className="groupMembers">
              <h4>Attachments</h4>
              {section.attachments?.length ? (
                <div className="materialList">
                  {section.attachments.map((attachment) => (
                    <div className="materialItem" key={attachment.id}>
                      <div className="materialItemTop">
                        <strong>{attachment.title}</strong>
                        <span className="stateBadge state-active">{attachment.attachmentType}</span>
                      </div>
                      <div className="mutedText">{attachment.description || "No description."}</div>
                      <div className="lessonMetaRow">
                        <span>By {attachment.teacherName}</span>
                        <span>{formatType(attachment.attachmentType)}</span>
                      </div>
                      <a href={attachment.resourceUrl} target="_blank" rel="noreferrer">{attachment.resourceUrl}</a>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="mutedText">No attachments yet.</div>
              )}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
