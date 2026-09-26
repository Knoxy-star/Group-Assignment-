import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { HAZARDS, slugForHazardEnum } from '../hazards/config';

const linkClass = ({ isActive }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${
    isActive ? 'bg-indigo-600 text-white' : 'text-slate-600 hover:bg-slate-100'
  }`;

export default function Layout() {
  const { session, logout } = useAuth();
  const { role, hazard, username } = session;
  const ownSlug = slugForHazardEnum(hazard);
  const isCrossHazard = role === 'PROVINCIAL_ADMIN' || role === 'NATIONAL_VIEWER';

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-2 px-4 py-3">
          <span className="mr-4 font-semibold text-slate-900">Rushinga DPDMS</span>

          {role === 'WARD_RECORDER' && ownSlug && (
            <>
              <NavLink to={`/h/${ownSlug}/submit`} className={linkClass}>
                Submit
              </NavLink>
              <NavLink to={`/h/${ownSlug}/my-submissions`} className={linkClass}>
                My submissions
              </NavLink>
            </>
          )}

          {role === 'PROVINCIAL_SUPERVISOR' && ownSlug && (
            <NavLink to={`/h/${ownSlug}/queue`} className={linkClass}>
              Approval queue
            </NavLink>
          )}

          {(role === 'PROVINCIAL_SUPERVISOR' || isCrossHazard) && (
            <>
              <NavLink to="/dashboard" className={linkClass}>
                Dashboard
              </NavLink>
              <NavLink to="/reports" className={linkClass}>
                Reports
              </NavLink>
              <NavLink to="/alerts" className={linkClass}>
                Alerts
              </NavLink>
            </>
          )}

          {isCrossHazard && (
            <div className="flex items-center gap-1 border-l border-slate-200 pl-2">
              {HAZARDS.map((h) => (
                <NavLink key={h.slug} to={`/h/${h.slug}/queue`} className={linkClass}>
                  {h.label}
                </NavLink>
              ))}
            </div>
          )}

          <div className="ml-auto flex items-center gap-3 text-sm text-slate-600">
            <span>
              {username} <span className="text-slate-400">({role.replace('_', ' ')})</span>
            </span>
            <button className="btn-secondary" onClick={logout}>
              Log out
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
