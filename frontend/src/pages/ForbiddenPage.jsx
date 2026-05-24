import { Link } from "react-router-dom";

export default function ForbiddenPage() {
  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">403</span>
      <h1>Forbidden</h1>
      <Link className="primaryButton smallButton" to="/">
        Go Home
      </Link>
    </section>
  );
}
