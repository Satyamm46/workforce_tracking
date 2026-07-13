import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/authService';

/** localStorage key for the JWT. Must match the key apiClient reads. */
const TOKEN_STORAGE_KEY = 'accessToken';

/**
 * Module-private context. Consumers must use the useAuth hook, never
 * useContext(AuthContext) directly — same encapsulation as HealthContext.
 */
const AuthContext = createContext(undefined);

/**
 * Provides authentication state (current user) and actions (login, logout) to
 * the entire component tree.
 *
 * On mount, if a token is already stored, it attempts to restore the session
 * by fetching the current user — so a page refresh doesn't log the user out.
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Attempt to restore an existing session once, on first mount.
  useEffect(() => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);

    if (!token) {
      setLoading(false);
      return;
    }

    authService
      .getCurrentUser()
      .then((response) => setUser(response.data))
      .catch(() => {
        // Token missing/expired/invalid — clear it and stay logged out.
        localStorage.removeItem(TOKEN_STORAGE_KEY);
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  /**
   * Authenticates and stores the session. Rethrows on failure so the caller
   * (the login form) can display the error.
   */
  const login = useCallback(async (credentials) => {
    const response = await authService.login(credentials);
    const { accessToken, user: authenticatedUser } = response.data;

    localStorage.setItem(TOKEN_STORAGE_KEY, accessToken);
    setUser(authenticatedUser);

    return authenticatedUser;
  }, []);

  /** Clears the session — removes the token and the user. */
  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: user !== null,
      login,
      logout,
    }),
    [user, loading, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * The only supported way to read auth state. Throws if used outside the
 * provider, turning a wiring mistake into an immediate, clear error.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an <AuthProvider>.');
  }
  return context;
};