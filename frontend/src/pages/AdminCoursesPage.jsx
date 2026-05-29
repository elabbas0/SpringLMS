import { useEffect, useMemo, useState } from "react";
import Select from "react-select";
import {
  createCourse,
  createCourseSection,
  listCourses,
  listCourseSections,
  listStudentGroups,
  searchUsers
} from "../api/adminApi.js";
import { darkSelectStyles } from "../components/selectStyles.js";

const assignmentTypeOptions = [
  { value: "LECTURE", label: "Lecture" },
  { value: "SEMINAR", label: "Seminar" },
  { value: "LABORATORY", label: "Laboratory" }
];

const emptyAssignment = () => ({
  teacherUserId: null,
  assignmentType: "LECTURE"
});

export default function AdminCoursesPage() {
  const [courses, setCourses] = useState([]);
  const [sections, setSections] = useState([]);
  const [groups, setGroups] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [courseForm, setCourseForm] = useState({
    code: "",
    name: "",
    description: "",
    credits: 5
  });
  const [sectionForm, setSectionForm] = useState({
    courseId: null,
    sectionCode: "",
    groupIds: [],
    teacherAssignments: [emptyAssignment()]
  });
  const [loading, setLoading] = useState(true);
  const [submittingCourse, setSubmittingCourse] = useState(false);
  const [submittingSection, setSubmittingSection] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    loadPage();
  }, []);

  async function loadPage() {
    setLoading(true);
    setError("");

    try {
      const [courseResult, sectionResult, groupResult, teacherResult] = await Promise.all([
        listCourses(),
        listCourseSections(),
        listStudentGroups(),
        searchUsers({ role: "TEACHER" })
      ]);

      setCourses(courseResult || []);
      setSections(sectionResult || []);
      setGroups(groupResult || []);
      setTeachers(teacherResult || []);

      const firstCourse = courseResult?.[0]?.id ?? null;
      setSectionForm((current) => ({
        ...current,
        courseId: current.courseId ?? firstCourse
      }));
    } catch (err) {
      setError(err.message || "Failed to load course management data.");
    } finally {
      setLoading(false);
    }
  }

  const courseOptions = useMemo(
    () => courses.map((course) => ({ value: course.id, label: `${course.code} - ${course.name}` })),
    [courses]
  );

  const groupOptions = useMemo(
    () => groups.map((group) => ({ value: group.id, label: `${group.name} - ${group.specializationName}` })),
    [groups]
  );

  const teacherOptions = useMemo(
    () =>
      teachers.map((teacher) => ({
        value: teacher.id,
        label: `${teacher.firstName} ${teacher.lastName} - ${teacher.email}`
      })),
    [teachers]
  );

  async function handleCreateCourse(event) {
    event.preventDefault();
    setSubmittingCourse(true);
    setError("");
    setSuccess("");

    try {
      await createCourse(courseForm);
      setCourseForm({ code: "", name: "", description: "", credits: 5 });
      await loadPage();
      setSuccess("Course created.");
    } catch (err) {
      setError(err.message || "Failed to create course.");
    } finally {
      setSubmittingCourse(false);
    }
  }

  async function handleCreateSection(event) {
    event.preventDefault();
    setSubmittingSection(true);
    setError("");
    setSuccess("");

    try {
      await createCourseSection({
        courseId: sectionForm.courseId,
        sectionCode: sectionForm.sectionCode,
        groupIds: sectionForm.groupIds,
        teacherAssignments: sectionForm.teacherAssignments.filter((assignment) => assignment.teacherUserId && assignment.assignmentType)
      });

      setSectionForm((current) => ({
        ...current,
        sectionCode: "",
        groupIds: [],
        teacherAssignments: [emptyAssignment()]
      }));
      await loadPage();
      setSuccess("Course section created.");
    } catch (err) {
      setError(err.message || "Failed to create course section.");
    } finally {
      setSubmittingSection(false);
    }
  }

  function updateAssignment(index, field, value) {
    setSectionForm((current) => ({
      ...current,
      teacherAssignments: current.teacherAssignments.map((assignment, currentIndex) =>
        currentIndex === index ? { ...assignment, [field]: value } : assignment
      )
    }));
  }

  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Admin</span>
      <h1>Courses</h1>

      {error && <div className="errorBox">{error}</div>}
      {success && <div className="successBox">{success}</div>}
      {loading && <div className="infoBox">Loading...</div>}

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Catalog</span>
          <h2>Create course</h2>
        </div>

        <form className="courseSectionForm" onSubmit={handleCreateCourse}>
          <label>
            Code
            <input value={courseForm.code} onChange={(event) => setCourseForm((current) => ({ ...current, code: event.target.value }))} />
          </label>
          <label>
            Name
            <input value={courseForm.name} onChange={(event) => setCourseForm((current) => ({ ...current, name: event.target.value }))} />
          </label>
          <label>
            Credits
            <input
              type="number"
              min="1"
              value={courseForm.credits}
              onChange={(event) => setCourseForm((current) => ({ ...current, credits: Number(event.target.value) }))}
            />
          </label>
          <label className="fullSpan">
            Description
            <textarea
              rows="3"
              value={courseForm.description}
              onChange={(event) => setCourseForm((current) => ({ ...current, description: event.target.value }))}
            />
          </label>
          <div className="fullSpan buttonRow">
            <button type="submit" disabled={submittingCourse}>
              {submittingCourse ? "Creating..." : "Create Course"}
            </button>
          </div>
        </form>
      </div>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Setup</span>
          <h2>Create section and assign teachers</h2>
        </div>

        <form className="courseSectionForm" onSubmit={handleCreateSection}>
          <label>
            Course
            <Select
              className="appSelect"
              styles={darkSelectStyles}
              menuPortalTarget={document.body}
              options={courseOptions}
              value={courseOptions.find((option) => option.value === sectionForm.courseId) ?? null}
              onChange={(option) => setSectionForm((current) => ({ ...current, courseId: option?.value ?? null }))}
              placeholder="Select course..."
            />
          </label>

          <label>
            Section code
            <input
              value={sectionForm.sectionCode}
              onChange={(event) => setSectionForm((current) => ({ ...current, sectionCode: event.target.value }))}
              placeholder="e.g. WP-6525A4"
            />
          </label>

          <label className="fullSpan">
            Groups
            <Select
              isMulti
              className="appSelect"
              styles={darkSelectStyles}
              menuPortalTarget={document.body}
              options={groupOptions}
              value={groupOptions.filter((option) => sectionForm.groupIds.includes(option.value))}
              onChange={(selected) =>
                setSectionForm((current) => ({
                  ...current,
                  groupIds: (selected || []).map((item) => item.value)
                }))
              }
              placeholder="Select groups..."
            />
          </label>

          <div className="fullSpan">
            <div className="sectionHeader compactHeader">
              <span className="eyebrow">Assignments</span>
              <button type="button" className="secondaryButton" onClick={() => setSectionForm((current) => ({
                ...current,
                teacherAssignments: [...current.teacherAssignments, emptyAssignment()]
              }))}>
                Add Assignment
              </button>
            </div>

            <div className="assignmentList">
              {sectionForm.teacherAssignments.map((assignment, index) => (
                <div className="assignmentRow" key={index}>
                  <Select
                    className="appSelect"
                    styles={darkSelectStyles}
                    menuPortalTarget={document.body}
                    options={teacherOptions}
                    value={teacherOptions.find((option) => option.value === assignment.teacherUserId) ?? null}
                    onChange={(option) => updateAssignment(index, "teacherUserId", option?.value ?? null)}
                    placeholder="Teacher..."
                  />
                  <select
                    value={assignment.assignmentType}
                    onChange={(event) => updateAssignment(index, "assignmentType", event.target.value)}
                  >
                    {assignmentTypeOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="ghostButton"
                    onClick={() =>
                      setSectionForm((current) => ({
                        ...current,
                        teacherAssignments: current.teacherAssignments.filter((_, currentIndex) => currentIndex !== index)
                      }))
                    }
                    disabled={sectionForm.teacherAssignments.length === 1}
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="fullSpan buttonRow">
            <button type="submit" disabled={submittingSection}>
              {submittingSection ? "Creating..." : "Create Section"}
            </button>
          </div>
        </form>
      </div>

      <div className="adminSection">
        <div className="sectionHeader">
          <span className="eyebrow">Sections</span>
          <h2>Current course sections</h2>
        </div>

        <div className="tableWrap">
          <table className="adminTable">
            <thead>
              <tr>
                <th>Course</th>
                <th>Section</th>
                <th>Term</th>
                <th>Groups</th>
                <th>Teachers</th>
                <th>Enrollments</th>
              </tr>
            </thead>
            <tbody>
              {sections.length === 0 && (
                <tr>
                  <td colSpan="6">No sections yet.</td>
                </tr>
              )}

              {sections.map((section) => (
                <tr key={section.id}>
                  <td>{section.courseCode} - {section.courseName}</td>
                  <td>{section.sectionCode}</td>
                  <td>{section.termName}</td>
                  <td>{section.groups?.map((group) => group.groupName).join(", ") || "-"}</td>
                  <td>{section.teacherAssignments?.map((item) => `${item.teacherName} (${item.assignmentType})`).join(", ") || "-"}</td>
                  <td>{section.enrollmentCount ?? 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
