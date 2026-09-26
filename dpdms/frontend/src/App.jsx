import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import HomeRedirect from './pages/HomeRedirect';
import SubmitPage from './pages/SubmitPage';
import MySubmissionsPage from './pages/MySubmissionsPage';
import QueuePage from './pages/QueuePage';
import DashboardPage from './pages/DashboardPage';
import ReportsPage from './pages/ReportsPage';
import AlertsPage from './pages/AlertsPage';
import ForbiddenPage from './pages/ForbiddenPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<HomeRedirect />} />
              <Route path="/forbidden" element={<ForbiddenPage />} />

              <Route element={<ProtectedRoute allowedRoles={['WARD_RECORDER']} requireOwnHazard />}>
                <Route path="/h/:hazard/submit" element={<SubmitPage />} />
                <Route path="/h/:hazard/my-submissions" element={<MySubmissionsPage />} />
              </Route>

              <Route
                element={
                  <ProtectedRoute
                    allowedRoles={['PROVINCIAL_SUPERVISOR', 'PROVINCIAL_ADMIN', 'NATIONAL_VIEWER']}
                    requireOwnHazard
                  />
                }
              >
                <Route path="/h/:hazard/queue" element={<QueuePage />} />
              </Route>

              <Route
                element={
                  <ProtectedRoute allowedRoles={['PROVINCIAL_SUPERVISOR', 'PROVINCIAL_ADMIN', 'NATIONAL_VIEWER']} />
                }
              >
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/alerts" element={<AlertsPage />} />
              </Route>

              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
