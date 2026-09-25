// Shared by all fire-service pages. When copying this
// pattern for another hazard's frontend, copy this file as-is and
// just change GATEWAY_BASE's service path where it's used per-page.

const GATEWAY_BASE = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('dpdms_token');
}

function getRole() {
    return localStorage.getItem('dpdms_role');
}

function getUsername() {
    return localStorage.getItem('dpdms_username');
}

// Call at the top of every protected page. Redirects to login if no
// token is present, and optionally restricts to a set of allowed roles.
function requireAuth(allowedRoles) {
    const token = getToken();
    if (!token) {
        window.location.href = GATEWAY_BASE + '/auth-service/login.html';
        return false;
    }
    if (allowedRoles && !allowedRoles.includes(getRole())) {
        document.body.innerHTML = '<p style="font-family:sans-serif;color:#b00020;padding:40px;">' +
            'Your role (' + getRole() + ') cannot access this page.</p>';
        return false;
    }
    return true;
}

// Wrapper around fetch() that attaches the Authorization header
// automatically. The actual RBAC/scoping check still happens
// server-side (see HazardScopeGuard) - this header is just how the
// caller proves who they are, it does not grant any access by itself.
async function authFetch(path, options) {
    options = options || {};
    options.headers = Object.assign({}, options.headers, {
        'Authorization': 'Bearer ' + getToken(),
        'Content-Type': 'application/json'
    });
    const res = await fetch(GATEWAY_BASE + path, options);
    if (res.status === 401) {
        localStorage.clear();
        window.location.href = GATEWAY_BASE + '/auth-service/login.html';
        throw new Error('Session expired');
    }
    return res;
}
