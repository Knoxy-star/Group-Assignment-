import { useState } from 'react';

/** Reason prompt for reject / request-corrections, both of which require notes. */
export default function DecisionModal({ title, confirmLabel, onConfirm, onClose }) {
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  async function handleConfirm() {
    if (!notes.trim()) {
      setError('Please provide a reason.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await onConfirm(notes.trim());
      onClose();
    } catch (err) {
      setError(err.message || 'Something went wrong');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-md rounded-lg bg-white p-5 shadow-xl">
        <h3 className="text-base font-semibold text-slate-900">{title}</h3>
        <textarea
          autoFocus
          rows={4}
          className="input mt-3"
          placeholder="Reason / notes for the recorder..."
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />
        {error && <p className="mt-2 text-sm text-rose-600">{error}</p>}
        <div className="mt-4 flex justify-end gap-2">
          <button className="btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button className="btn-primary" disabled={saving} onClick={handleConfirm}>
            {saving ? 'Saving...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
