import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import IncidentForm from '../components/IncidentForm';
import { createIncident } from '../api/incidents';
import { hazardBySlug } from '../hazards/config';

export default function SubmitPage() {
  const { hazard: slug } = useParams();
  const hazard = hazardBySlug(slug);
  const navigate = useNavigate();
  const [success, setSuccess] = useState(false);

  async function handleSubmit(payload) {
    await createIncident(hazard.servicePath, payload);
    setSuccess(true);
    setTimeout(() => navigate(`/h/${slug}/my-submissions`), 900);
  }

  return (
    <div className="card max-w-3xl">
      <h1 className="mb-4 text-lg font-semibold text-slate-900">Report a {hazard.label.toLowerCase()} incident</h1>
      {success && (
        <div className="mb-4 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          Submitted. It now enters PENDING review by your provincial {hazard.label.toLowerCase()} supervisor.
        </div>
      )}
      <IncidentForm hazard={hazard} onSubmit={handleSubmit} submitLabel="Submit incident" />
    </div>
  );
}
