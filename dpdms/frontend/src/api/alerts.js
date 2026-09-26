import { apiJson } from './client';

// GET /alert-service/api/alerts - scoped server-side: recorder 403,
// supervisor sees their own hazard, admin/national viewer see all.
export function listAlerts() {
  return apiJson('/alert-service/api/alerts');
}
