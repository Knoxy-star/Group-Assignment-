import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import IncidentForm from '../components/IncidentForm';
import IncidentTable from '../components/IncidentTable';
import { deleteIncident, listIncidents, updateIncident } from '../api/incidents';
import { hazardBySlug } from '../hazards/config';

export default function MySubmissionsPage() {
  const { hazard: slug } = useParams();
  const hazard = hazardBySlug(slug);
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editing, setEditing] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setIncidents(await listIncidents(hazard.servicePath));
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [hazard.servicePath]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleDelete(id) {
    if (!confirm('Delete this submission? This cannot be undone.')) return;
    await deleteIncident(hazard.servicePath, id);
    load();
  }

  async function handleUpdate(payload) {
    await updateIncident(hazard.servicePath, editing.id, payload);
    setEditing(null);
    load();
  }

  return (
    <div className="space-y-6">
      <h1 className="text-lg font-semibold text-slate-900">My {hazard.label.toLowerCase()} submissions</h1>

      {editing && (
        <div className="card max-w-3xl">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-semibold text-slate-900">Edit submission #{editing.id}</h2>
            <button className="btn-secondary" onClick={() => setEditing(null)}>
              Cancel
            </button>
          </div>
          {editing.reviewNotes && (
            <p className="mb-3 rounded-md bg-sky-50 px-3 py-2 text-sm text-sky-800">
              Supervisor's notes: {editing.reviewNotes}
            </p>
          )}
          <IncidentForm
            hazard={hazard}
            initialValues={{ ...editing, occurredAt: editing.occurredAt?.slice(0, 16) }}
            onSubmit={handleUpdate}
            submitLabel="Save changes"
          />
        </div>
      )}

      {error && <p className="text-sm text-rose-600">{error}</p>}
      {loading ? (
        <p className="text-sm text-slate-500">Loading...</p>
      ) : (
        <IncidentTable
          hazard={hazard}
          incidents={incidents}
          emptyMessage="You haven't submitted any incidents yet."
          renderActions={(incident) => (
            <div className="flex gap-2">
              {(incident.status === 'PENDING' || incident.status === 'CORRECTIONS_REQUESTED') && (
                <button className="btn-secondary" onClick={() => setEditing(incident)}>
                  Edit
                </button>
              )}
              {incident.status !== 'APPROVED' && (
                <button className="btn-danger" onClick={() => handleDelete(incident.id)}>
                  Delete
                </button>
              )}
            </div>
          )}
        />
      )}
    </div>
  );
}
