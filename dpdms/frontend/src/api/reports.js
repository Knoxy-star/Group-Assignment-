import { apiFetch, apiJson } from './client';

// GET /report-service/api/reports?format=&hazard=&ward=&district=&from=&to=&severity=&status=
// Returns a binary blob (Content-Disposition: attachment) plus X-Report-Rows /
// X-Report-Warnings headers - see ReportController in report-service.
export async function generateReport(filters) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, value);
    }
  });

  const res = await apiFetch(`/report-service/api/reports?${params.toString()}`);
  const blob = await res.blob();

  const disposition = res.headers.get('content-disposition') || '';
  const match = disposition.match(/filename="?([^";]+)"?/);
  const filename = match ? match[1] : `dpdms-report.${filters.format || 'pdf'}`;
  const warnings = res.headers.get('x-report-warnings');
  const rows = res.headers.get('x-report-rows');

  return { blob, filename, warnings, rows };
}

export function reportHistory() {
  return apiJson('/report-service/api/reports/history');
}

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
