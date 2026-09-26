import { useEffect, useMemo, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { listIncidents } from '../api/incidents';
import { HAZARDS, SEVERITIES } from '../hazards/config';
import { SeverityBadge } from '../components/Badges';
import IncidentMap from '../components/IncidentMap';
import { useAuth } from '../context/AuthContext';

const TREND_DAYS = 30;

export default function DashboardPage() {
  const { session } = useAuth();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    Promise.all(
      HAZARDS.map(async (h) => {
        try {
          const items = await listIncidents(h.servicePath, 'APPROVED');
          return { hazard: h, items: items.filter((i) => i.status === 'APPROVED'), error: null };
        } catch (err) {
          return { hazard: h, items: [], error: err.status === 403 ? 'No access for your role' : 'Unavailable' };
        }
      })
    ).then((r) => {
      if (!cancelled) {
        setResults(r);
        setLoading(false);
      }
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const allIncidents = useMemo(
    () =>
      results.flatMap((r) =>
        r.items.map((i) => ({ ...i, hazardLabel: r.hazard.label, hazardSlug: r.hazard.slug }))
      ),
    [results]
  );

  const countsByHazard = useMemo(
    () => results.map((r) => ({ name: r.hazard.label, count: r.items.length, error: r.error })),
    [results]
  );

  const countsBySeverity = useMemo(
    () =>
      SEVERITIES.map((sev) => ({
        name: sev,
        count: allIncidents.filter((i) => i.severity === sev).length,
      })),
    [allIncidents]
  );

  const trend = useMemo(() => {
    const days = [];
    const today = new Date();
    for (let i = TREND_DAYS - 1; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      days.push(d.toISOString().slice(0, 10));
    }
    const counts = Object.fromEntries(days.map((d) => [d, 0]));
    allIncidents.forEach((i) => {
      const day = i.occurredAt?.slice(0, 10);
      if (day in counts) counts[day] += 1;
    });
    return days.map((d) => ({ date: d.slice(5), count: counts[d] }));
  }, [allIncidents]);

  const recent = useMemo(
    () => [...allIncidents].sort((a, b) => new Date(b.occurredAt) - new Date(a.occurredAt)).slice(0, 10),
    [allIncidents]
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-lg font-semibold text-slate-900">Provincial dashboard</h1>
        <p className="text-sm text-slate-500">
          Logged in as {session.username} ({session.role.replace('_', ' ')}). Approved incidents across all hazards.
        </p>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
        {countsByHazard.map((c) => (
          <div key={c.name} className="card">
            <div className="text-sm text-slate-500">{c.name}</div>
            {c.error ? (
              <div className="text-xs text-rose-500">{c.error}</div>
            ) : (
              <>
                <div className="text-2xl font-bold text-slate-900">{c.count}</div>
                <div className="text-xs text-slate-400">approved</div>
              </>
            )}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="card">
          <h2 className="mb-3 text-sm font-semibold text-slate-700">Incidents by severity</h2>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={countsBySeverity}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Bar dataKey="count" fill="#4f46e5" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h2 className="mb-3 text-sm font-semibold text-slate-700">Trend - last {TREND_DAYS} days</h2>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={trend}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" fontSize={11} interval={4} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Line type="monotone" dataKey="count" stroke="#4f46e5" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card">
        <h2 className="mb-3 text-sm font-semibold text-slate-700">Incident map</h2>
        {loading ? <p className="text-sm text-slate-500">Loading...</p> : <IncidentMap incidents={allIncidents} />}
      </div>

      <div className="card">
        <h2 className="mb-3 text-sm font-semibold text-slate-700">Recent incidents</h2>
        {recent.length === 0 ? (
          <p className="text-sm text-slate-500">No approved incidents yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead>
                <tr className="text-left text-slate-500">
                  <th className="py-1 pr-4">Hazard</th>
                  <th className="py-1 pr-4">Ward</th>
                  <th className="py-1 pr-4">District</th>
                  <th className="py-1 pr-4">Severity</th>
                  <th className="py-1 pr-4">Occurred</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {recent.map((i) => (
                  <tr key={`${i.hazardSlug}-${i.id}`}>
                    <td className="py-1.5 pr-4">{i.hazardLabel}</td>
                    <td className="py-1.5 pr-4">{i.ward}</td>
                    <td className="py-1.5 pr-4">{i.district}</td>
                    <td className="py-1.5 pr-4">
                      <SeverityBadge severity={i.severity} />
                    </td>
                    <td className="py-1.5 pr-4">{new Date(i.occurredAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
