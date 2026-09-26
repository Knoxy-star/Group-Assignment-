import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { slugForHazardEnum } from '../hazards/config';

/** Sends a freshly logged-in user to the page their role actually uses. */
export default function HomeRedirect() {
  const { session } = useAuth();
  const slug = slugForHazardEnum(session.hazard);

  if (session.role === 'WARD_RECORDER' && slug) {
    return <Navigate to={`/h/${slug}/submit`} replace />;
  }
  if (session.role === 'PROVINCIAL_SUPERVISOR' && slug) {
    return <Navigate to={`/h/${slug}/queue`} replace />;
  }
  return <Navigate to="/dashboard" replace />;
}
