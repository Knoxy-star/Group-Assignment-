import { Navigate, Outlet, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { hazardBySlug } from '../hazards/config';

/**
 * Frontend route gate. This is pure UX (redirect to /login or show a
 * clean "Forbidden" page instead of a raw 403 JSON blob) - the real
 * security decision is always re-checked server-side by each hazard
 * service's HazardScopeGuard, exactly as the brief requires ("never in
 * the front end alone").
 */
export default function ProtectedRoute({ allowedRoles, requireOwnHazard }) {
  const { session, isAuthenticated } = useAuth();
  const { hazard: hazardSlug } = useParams();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(session.role)) {
    return <Navigate to="/forbidden" replace />;
  }

  if (requireOwnHazard && hazardSlug && session.role !== 'PROVINCIAL_ADMIN' && session.role !== 'NATIONAL_VIEWER') {
    const hazard = hazardBySlug(hazardSlug);
    if (!hazard || hazard.hazardEnum !== session.hazard) {
      return <Navigate to="/forbidden" replace />;
    }
  }

  return <Outlet />;
}
