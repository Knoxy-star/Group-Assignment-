import { Fragment, useEffect, useState } from 'react';
import { listAlerts } from '../api/alerts';
import { SeverityBadge } from '../components/Badges';

const DELIVERY_STYLES = {
  SENT: 'text-emerald-700',
  FAILED: 'text-rose-700',
  SUPPRESSED: 'text-slate-500',
};

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expanded, setExpanded] = useState(null);

  useEffect(() => {
    listAlerts()
      .then(setAlerts)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-4">
      <h1 className="text-lg font-semibold text-slate-900">Alert log</h1>
      {error && <p className="text-sm text-rose-600">{error}</p>}
      {loading ? (
        <p className="text-sm text-slate-500">Loading...</p>
      ) : alerts.length === 0 ? (
        <p className="text-sm text-slate-500">No alerts recorded yet.</p>
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                <Th>Received</Th>
                <Th>Hazard</Th>
                <Th>Ward / district</Th>
                <Th>Severity</Th>
                <Th>Channel</Th>
                <Th>Status</Th>
                <Th>Why it alerted</Th>
                <Th />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 bg-white">
              {alerts.map((a) => (
                <Fragment key={a.id}>
                  <tr>
                    <Td>{new Date(a.receivedAt).toLocaleString()}</Td>
                    <Td>{a.hazard.replace('_', ' ')}</Td>
                    <Td>
                      {a.ward}, {a.district}
                    </Td>
                    <Td>
                      <SeverityBadge severity={a.severity} />
                    </Td>
                    <Td>{a.channel}</Td>
                    <Td className={DELIVERY_STYLES[a.deliveryStatus]}>{a.deliveryStatus}</Td>
                    <Td className="max-w-xs truncate">{a.alertReason || a.deliveryDetail}</Td>
                    <Td>
                      <button
                        className="text-indigo-600 hover:underline"
                        onClick={() => setExpanded(expanded === a.id ? null : a.id)}
                      >
                        {expanded === a.id ? 'Hide' : 'Details'}
                      </button>
                    </Td>
                  </tr>
                  {expanded === a.id && (
                    <tr>
                      <td colSpan={8} className="bg-slate-50 px-4 py-3">
                        <p className="mb-2 text-sm text-slate-700">{a.message}</p>
                        {a.deliveries.length > 0 && (
                          <table className="w-full text-xs">
                            <thead>
                              <tr className="text-left text-slate-500">
                                <th className="pr-4">Channel</th>
                                <th className="pr-4">Recipient</th>
                                <th className="pr-4">Status</th>
                                <th className="pr-4">Detail</th>
                                <th className="pr-4">Attempted</th>
                              </tr>
                            </thead>
                            <tbody>
                              {a.deliveries.map((d, idx) => (
                                <tr key={idx}>
                                  <td className="pr-4">{d.channel}</td>
                                  <td className="pr-4">{d.recipient}</td>
                                  <td className={`pr-4 ${DELIVERY_STYLES[d.status]}`}>{d.status}</td>
                                  <td className="pr-4">{d.detail}</td>
                                  <td className="pr-4">{new Date(d.attemptedAt).toLocaleString()}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        )}
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function Th({ children }) {
  return <th className="px-3 py-2 text-left font-semibold text-slate-600">{children}</th>;
}

function Td({ children, className = '' }) {
  return <td className={`px-3 py-2 text-slate-700 ${className}`}>{children}</td>;
}
