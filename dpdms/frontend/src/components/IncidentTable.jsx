import { StatusBadge, SeverityBadge } from './Badges';

/** Generic incident list table, driven by a hazard's field config. */
export default function IncidentTable({ hazard, incidents, renderActions, emptyMessage }) {
  if (!incidents.length) {
    return <p className="text-sm text-slate-500">{emptyMessage || 'No incidents found.'}</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            <Th>Ward</Th>
            <Th>District</Th>
            <Th>Occurred</Th>
            <Th>Severity</Th>
            <Th>Status</Th>
            {hazard.fields.slice(0, 3).map((f) => (
              <Th key={f.key}>{f.label}</Th>
            ))}
            {renderActions && <Th>Actions</Th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {incidents.map((incident) => (
            <tr key={incident.id}>
              <Td>{incident.ward}</Td>
              <Td>{incident.district}</Td>
              <Td>{new Date(incident.occurredAt).toLocaleString()}</Td>
              <Td>
                <SeverityBadge severity={incident.severity} />
              </Td>
              <Td>
                <StatusBadge status={incident.status} />
              </Td>
              {hazard.fields.slice(0, 3).map((f) => (
                <Td key={f.key}>{formatValue(incident[f.key], f)}</Td>
              ))}
              {renderActions && <Td>{renderActions(incident)}</Td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatValue(value, field) {
  if (value == null) return '-';
  if (field.type === 'boolean') return value ? field.trueLabel : field.falseLabel;
  if (field.type === 'select') return String(value).replace('_', ' ');
  return String(value);
}

function Th({ children }) {
  return <th className="px-3 py-2 text-left font-semibold text-slate-600">{children}</th>;
}

function Td({ children }) {
  return <td className="whitespace-nowrap px-3 py-2 text-slate-700">{children}</td>;
}
