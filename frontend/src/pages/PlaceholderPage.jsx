export default function PlaceholderPage({ eyebrow, title }) {
  return (
    <section className="contentPanel fadeIn">
      {eyebrow && <span className="eyebrow">{eyebrow}</span>}
      <h1>{title}</h1>
    </section>
  );
}
