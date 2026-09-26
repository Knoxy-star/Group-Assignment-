import { useState } from 'react';
import MapPicker from './MapPicker';
import { SEVERITIES } from '../hazards/config';

function defaultsFor(hazard, initialValues) {
  const base = {
    district: '',
    province: '',
    occurredAt: '',
    severity: 'MODERATE',
    latitude: null,
    longitude: null,
  };
  hazard.fields.forEach((f) => {
    base[f.key] = f.type === 'boolean' ? false : f.type === 'select' ? f.options[0] : '';
  });
  return { ...base, ...initialValues };
}

/**
 * Generic create/edit form for any hazard, driven entirely by
 * hazards/config.js. Ward and reporter are never collected here - the
 * backend derives them from the caller's JWT (see each service's
 * create()), matching the brief's "authority scoped by (ward, hazard)".
 */
export default function IncidentForm({ hazard, initialValues, onSubmit, submitLabel = 'Submit' }) {
  const [values, setValues] = useState(() => defaultsFor(hazard, initialValues));
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  function set(key, value) {
    setValues((v) => ({ ...v, [key]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    if (values.latitude == null || values.longitude == null) {
      setError('Click on the map to set GPS coordinates before submitting.');
      return;
    }
    setSaving(true);
    try {
      const payload = {
        ...values,
        occurredAt: values.occurredAt ? `${values.occurredAt}:00` : null,
        latitude: Number(values.latitude),
        longitude: Number(values.longitude),
      };
      hazard.fields.forEach((f) => {
        if (f.type === 'number') {
          payload[f.key] = payload[f.key] === '' ? null : Number(payload[f.key]);
        }
      });
      await onSubmit(payload);
    } catch (err) {
      setError(err.message || 'Something went wrong');
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {error && (
        <div className="rounded-md bg-rose-50 px-4 py-2 text-sm text-rose-700">{error}</div>
      )}

      <fieldset className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <legend className="col-span-full mb-1 text-sm font-semibold text-slate-700">
          Shared incident details
        </legend>
        <Field label="District">
          <input
            required
            className="input"
            value={values.district}
            onChange={(e) => set('district', e.target.value)}
          />
        </Field>
        <Field label="Province">
          <input
            required
            className="input"
            value={values.province}
            onChange={(e) => set('province', e.target.value)}
          />
        </Field>
        <Field label="Date/time occurred">
          <input
            required
            type="datetime-local"
            className="input"
            value={values.occurredAt}
            onChange={(e) => set('occurredAt', e.target.value)}
          />
        </Field>
        <Field label="Severity">
          <select
            className="input"
            value={values.severity}
            onChange={(e) => set('severity', e.target.value)}
          >
            {SEVERITIES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </Field>
        <div className="col-span-full">
          <MapPicker
            latitude={values.latitude}
            longitude={values.longitude}
            onChange={(lat, lng) => setValues((v) => ({ ...v, latitude: lat, longitude: lng }))}
          />
          {values.latitude != null && (
            <p className="mt-1 text-xs text-slate-500">
              Selected: {values.latitude.toFixed(5)}, {values.longitude.toFixed(5)}
            </p>
          )}
        </div>
      </fieldset>

      <fieldset className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <legend className="col-span-full mb-1 text-sm font-semibold text-slate-700">
          {hazard.label} indicators
        </legend>
        {hazard.fields.map((f) => (
          <Field key={f.key} label={f.label}>
            {f.type === 'select' && (
              <select className="input" value={values[f.key]} onChange={(e) => set(f.key, e.target.value)}>
                {f.options.map((o) => (
                  <option key={o} value={o}>
                    {o.replace('_', ' ')}
                  </option>
                ))}
              </select>
            )}
            {f.type === 'boolean' && (
              <select
                className="input"
                value={values[f.key] ? 'true' : 'false'}
                onChange={(e) => set(f.key, e.target.value === 'true')}
              >
                <option value="true">{f.trueLabel}</option>
                <option value="false">{f.falseLabel}</option>
              </select>
            )}
            {(f.type === 'number' || f.type === 'text') && (
              <input
                required
                type={f.type === 'number' ? 'number' : 'text'}
                step={f.step}
                min={f.type === 'number' ? 0 : undefined}
                className="input"
                value={values[f.key]}
                onChange={(e) => set(f.key, e.target.value)}
              />
            )}
          </Field>
        ))}
      </fieldset>

      <button type="submit" disabled={saving} className="btn-primary">
        {saving ? 'Saving...' : submitLabel}
      </button>
    </form>
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
