import { apiJson } from './client';

// Every hazard controller (FloodController, MiningAccidentController, ...)
// exposes the identical shape at /<servicePath>/api/incidents/**, so one
// generic client works for all five hazards - see hazards/config.js for
// what `servicePath` is per hazard.

export function listIncidents(servicePath, status) {
  const qs = status ? `?status=${status}` : '';
  return apiJson(`/${servicePath}/api/incidents${qs}`);
}

export function getIncident(servicePath, id) {
  return apiJson(`/${servicePath}/api/incidents/${id}`);
}

export function createIncident(servicePath, payload) {
  return apiJson(`/${servicePath}/api/incidents`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateIncident(servicePath, id, payload) {
  return apiJson(`/${servicePath}/api/incidents/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function deleteIncident(servicePath, id) {
  return apiJson(`/${servicePath}/api/incidents/${id}`, { method: 'DELETE' });
}

export function approveIncident(servicePath, id) {
  return apiJson(`/${servicePath}/api/incidents/${id}/approve`, { method: 'POST' });
}

export function rejectIncident(servicePath, id, notes) {
  return apiJson(`/${servicePath}/api/incidents/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ notes }),
  });
}

export function requestCorrections(servicePath, id, notes) {
  return apiJson(`/${servicePath}/api/incidents/${id}/request-corrections`, {
    method: 'POST',
    body: JSON.stringify({ notes }),
  });
}
