import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import IncidentTable from '../components/IncidentTable';
import DecisionModal from '../components/DecisionModal';
import { approveIncident, listIncidents, rejectIncident, requestCorrections } from '../api/incidents';
import { hazardBySlug, STATUSES } from '../hazards/config';
import { useAuth } from '../context/AuthContext';

export default function QueuePage() {
  const { hazard: slug } = useParams();
  const hazard = hazardBySlug(slug);
  const { session } = useAuth();
  const canDecide = session.role === 'PROVINCIAL_SUPERVISOR';

  const [statusFilter, setStatusFilter] = useState(canDecide ? 'PENDING' : '');
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [modal, setModal] = useState(null); // { type: 'reject' | 'corrections', incident }

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setIncidents(await listIncidents(hazard.servicePath, statusFilter || undefined));
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [hazard.servicePath, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleApprove(id) {
    await approveIncident(hazard.servicePath, id);
    load();
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-900">
          {hazard.label} {canDecide ? 'approval queue' : 'incidents'}
        </h1>
        <select className="input w-48" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.replace('_', ' ')}
            </option>
          ))}
        </select>
      </div>

      {error && <p className="text-sm text-rose-600">{error}</p>}
      {loading ? (
        <p className="text-sm text-slate-500">Loading...</p>
      ) : (
        <IncidentTable
          hazard={hazard}
          incidents={incidents}
          emptyMessage="No incidents match this filter."
          renderActions={
            canDecide
              ? (incident) =>
                  incident.status === 'PENDING' ? (
                    <div className="flex gap-2">
                      <button className="btn-primary" onClick={() => handleApprove(incident.id)}>
                        Approve
                      </button>
                      <button
                        className="btn-secondary"
                        onClick={() => setModal({ type: 'corrections', incident })}
                      >
                        Request corrections
                      </button>
                      <button className="btn-danger" onClick={() => setModal({ type: 'reject', incident })}>
                        Reject
                      </button>
                    </div>
                  ) : null
              : undefined
          }
        />
      )}

      {modal && modal.type === 'reject' && (
        <DecisionModal
          title={`Reject incident #${modal.incident.id}`}
          confirmLabel="Reject"
          onClose={() => setModal(null)}
          onConfirm={async (notes) => {
            await rejectIncident(hazard.servicePath, modal.incident.id, notes);
            load();
          }}
        />
      )}
      {modal && modal.type === 'corrections' && (
        <DecisionModal
          title={`Request corrections for incident #${modal.incident.id}`}
          confirmLabel="Request corrections"
          onClose={() => setModal(null)}
          onConfirm={async (notes) => {
            await requestCorrections(hazard.servicePath, modal.incident.id, notes);
            load();
          }}
        />
      )}
    </div>
  );
}
