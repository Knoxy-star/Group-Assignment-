import { apiJson } from './client';

// POST /auth-service/api/auth/login -> { token, userId, username, role, hazard, ward }
export function login(username, password) {
  return apiJson('/auth-service/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}
