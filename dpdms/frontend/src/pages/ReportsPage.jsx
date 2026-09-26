import { useEffect, useState } from 'react';
import { downloadBlob, generateReport, reportHistory } from '../api/reports';
import { HAZARDS, SEVERITIES, STATUSES } from '../hazards/config';
import { useAuth } from '../context/AuthContext';

const FORMATS = [
  { value: 'pdf', label: 'PDF' },
  { value: 'docx', label: 'Word (DOCX)' },
  { value: 'xlsx', label: 'Excel (XLSX)' },
  { value: 'csv', label: 'CSV' },
];

export default function ReportsPage() {
  const { session } = useAuth();
  const canChooseStatus = session.role === 'PROVINCIAL_SUPERVISOR' || session.role === 'PROVINCIAL_ADMIN';

  const [filters, setFilters] = useState({
    hazard: '',
    ward: '',
    district: '',
    from: '',
    to: '',
    severity: '',
    status: '',
  });
  const [generating, setGenerating] = useState(null);
  const [message, setMessage] = useState(null);
  const [history, setHistory] = useState(null);

  useEffect(() => {
    if (session.role === 'PROVINCIAL_ADMIN') {
      reportHistory().then(setHistory).catch(() => setHistory([]));
    }
  }, [session.role]);

  function set(key, value) {
    setFilters((f) => ({ ...f, [key]: value }));
  }

  async function handleDownload(format) {
    setGenerating(format);
    setMessage(null);
    try {
      const { blob, filename, warnings, rows } = await generateReport({ ...filters, format });
      downloadBlob(blob, filename);
      setMessage(`Downloaded ${filename} (${rows} row${rows === '1' ? '' : 's'}).${warnings ? ` Warnings: ${warnings}` : ''}`);
    } catch (err) {
      setMessage(err.message);
    } finally {
      setGenerating(null);
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-lg font-semibold text-slate-900">Reports</h1>

      <div className="card max-w-3xl space-y-4">
        <p className="text-sm text-slate-500">
          All filters are optional. Only approved incidents are included unless you can choose another status.
        </p>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <Field label="Hazard">
            <select className="input" value={filters.hazard} onChange={(e) => set('hazard', e.target.value)}>
              <option value="">All hazards</option>
              {HAZARDS.map((h) => (
                <option key={h.hazardEnum} value={h.hazardEnum}>
                  {h.label}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Ward">
            <input className="input" value={filters.ward} onChange={(e) => set('ward', e.target.value)} />
          </Field>
          <Field label="District">
            <input className="input" value={filters.district} onChange={(e) => set('district', e.target.value)} />
          </Field>
          <Field label="From">
            <input type="date" className="input" value={filters.from} onChange={(e) => set('from', e.target.value)} />
          </Field>
          <Field label="To">
            <input type="date" className="input" value={filters.to} onChange={(e) => set('to', e.target.value)} />
          </Field>
          <Field label="Severity">
            <select className="input" value={filters.severity} onChange={(e) => set('severity', e.target.value)}>
              <option value="">Any severity</option>
              {SEVERITIES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </Field>
          {canChooseStatus && (
            <Field label="Status">
              <select className="input" value={filters.status} onChange={(e) => set('status', e.target.value)}>
                <option value="">Approved (default)</option>
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {s.replace('_', ' ')}
                  </option>
                ))}
              </select>
            </Field>
          )}
        </div>

        {message && <p className="text-sm text-slate-600">{message}</p>}

        <div className="flex flex-wrap gap-2">
          {FORMATS.map((f) => (
            <button
              key={f.value}
              className="btn-primary"
              disabled={generating !== null}
              onClick={() => handleDownload(f.value)}
            >
              {generating === f.value ? 'Generating...' : `Download ${f.label}`}
            </button>
          ))}
        </div>
      </div>

      {history && (
        <div className="card">
          <h2 className="mb-3 text-sm font-semibold text-slate-700">Recent report generations</h2>
          {history.length === 0 ? (
            <p className="text-sm text-slate-500">No reports generated yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead>
                  <tr className="text-left text-slate-500">
                    <th className="py-1 pr-4">When</th>
                    <th className="py-1 pr-4">User</th>
                    <th className="py-1 pr-4">Role</th>
                    <th className="py-1 pr-4">Format</th>
                    <th className="py-1 pr-4">Rows</th>
                    <th className="py-1 pr-4">Filters</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {history.map((h) => (
                    <tr key={h.id}>
                      <td className="py-1.5 pr-4">{new Date(h.generatedAt).toLocaleString()}</td>
                      <td className="py-1.5 pr-4">{h.userId}</td>
                      <td className="py-1.5 pr-4">{h.role}</td>
                      <td className="py-1.5 pr-4">{h.format}</td>
                      <td className="py-1.5 pr-4">{h.rowCount}</td>
                      <td className="py-1.5 pr-4 text-xs text-slate-500">{h.filters}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function Field({ label, children }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-slate-700">{label}</span>
      {children}
    </label>
  );
}
