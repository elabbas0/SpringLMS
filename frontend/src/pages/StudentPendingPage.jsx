export default function StudentPendingPage() {
  return (
    <section className="contentPanel fadeIn">
      <span className="eyebrow">Pending Approval</span>
      <h1>Your registration is waiting for approval</h1>
      <p>
        Your student account exists, but it is not active yet. Once an admin approval endpoint exists,
        this page can refresh the user state or show approval progress.
      </p>
    </section>
  );
}
