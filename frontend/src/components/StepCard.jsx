export default function StepCard({ title, subtitle, children }) {
  return (
    <section className="stepCard fadeIn">
      <div className="sectionHeader">
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {children}
    </section>
  );
}
