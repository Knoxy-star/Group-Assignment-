// Every request goes through Vite's dev proxy (see vite.config.js) using a
// relative path such as "/flood-service/api/incidents", so this file never
// needs to know a hostname - exactly mirroring how the old Thymeleaf pages
// used relative gateway paths, and why no CORS setup is needed anywhere.

const STORAGE_KEY = 'dpdms_session';

export function getSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setSession(session) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEY);
}

class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

/**
 * Wrapper around fetch() that attaches the bearer token and turns
 * non-2xx responses into thrown ApiError. A 401 clears the session and
 * sends the user back to the login page - the actual RBAC/scoping check
 * still happens server-side (HazardScopeGuard), this is just how the
 * caller proves who they are.
 */
export async function apiFetch(path, options = {}) {
  const session = getSession();
  const headers = { ...(options.headers || {}) };
  if (session?.token) {
    headers['Authorization'] = `Bearer ${session.token}`;
  }
  if (options.body && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(path, { ...options, headers });

  if (res.status === 401) {
    clearSession();
    window.location.href = '/login';
    throw new ApiError(401, 'Session expired');
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      message = body.error || message;
    } catch {
      // response wasn't JSON (e.g. a blob endpoint failing) - keep default message
    }
    throw new ApiError(res.status, message);
  }

  return res;
}

export async function apiJson(path, options) {
  const res = await apiFetch(path, options);
  if (res.status === 204) return null;
  return res.json();
}

export { ApiError };
