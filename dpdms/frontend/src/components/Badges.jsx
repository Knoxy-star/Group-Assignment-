const STATUS_STYLES = {
  PENDING: 'bg-amber-100 text-amber-800',
  APPROVED: 'bg-emerald-100 text-emerald-800',
  REJECTED: 'bg-rose-100 text-rose-800',
  CORRECTIONS_REQUESTED: 'bg-sky-100 text-sky-800',
};

const SEVERITY_STYLES = {
  LOW: 'bg-slate-100 text-slate-700',
  MODERATE: 'bg-amber-100 text-amber-800',
  HIGH: 'bg-orange-100 text-orange-800',
  CRITICAL: 'bg-rose-100 text-rose-800',
};

function Badge({ text, className }) {
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${className}`}>
      {text}
    </span>
  );
}

export function StatusBadge({ status }) {
  return <Badge text={status.replace('_', ' ')} className={STATUS_STYLES[status] || 'bg-slate-100 text-slate-700'} />;
}

export function SeverityBadge({ severity }) {
  return <Badge text={severity} className={SEVERITY_STYLES[severity] || 'bg-slate-100 text-slate-700'} />;
}
