import { createContext, useContext, useMemo, useState } from 'react';
import { login as apiLogin } from '../api/auth';
import { getSession, setSession, clearSession } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSessionState] = useState(() => getSession());

  const value = useMemo(
    () => ({
      session,
      isAuthenticated: !!session,
      async login(username, password) {
        const result = await apiLogin(username, password);
        setSession(result);
        setSessionState(result);
        return result;
      },
      logout() {
        clearSession();
        setSessionState(null);
      },
    }),
    [session]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
