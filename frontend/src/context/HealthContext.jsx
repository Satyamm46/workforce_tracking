import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { healthService } from '../services/healthService';

/**
 * Possible backend health states surfaced to the UI.
 * UNKNOWN is the initial state before the first check resolves.
 */
const HEALTH_STATUS = Object.freeze({
  UP: 'UP',
  DOWN: 'DOWN',
  UNKNOWN: 'UNKNOWN',
});

const INITIAL_STATE = Object.freeze({
  status: HEALTH_STATUS.UNKNOWN,
  loading: true,
  error: null,
});

/**
 * The context object. Kept MODULE-PRIVATE (not exported) on purpose: consumers
 * must use the `useHealth` hook below, never `useContext(HealthContext)`
 * directly. This guarantees the provider-presence check always runs.
 */
const HealthContext = createContext(undefined);

/**
 * Provides backend-health state to the entire component tree beneath it.
 *
 * Holds the current status, performs the initial check on mount, and exposes a
 * `checkHealth` function so any descendant can re-trigger the check (e.g. a
 * "retry" button). This is the app-wide state pattern future contexts reuse.
 */
export const HealthProvider = ({ children }) => {
  const [state, setState] = useState(INITIAL_STATE);

  /**
   * Fetches the backend health status and reduces it into UI state.
   * Wrapped in useCallback so its identity is stable across renders.
   */
  const checkHealth = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const response = await healthService.getHealthStatus();
      setState({
        status: response.data?.status ?? HEALTH_STATUS.UNKNOWN,
        loading: false,
        error: null,
      });
    } catch (err) {
      // err is the normalized error shape produced by apiClient's interceptor.
      setState({
        status: HEALTH_STATUS.DOWN,
        loading: false,
        error: err?.message ?? 'Unable to determine backend status.',
      });
    }
  }, []);

  // Run one health check when the provider first mounts.
  useEffect(() => {
    checkHealth();
  }, [checkHealth]);

  /**
   * Memoize the context value so consumers only re-render when the state or
   * the checkHealth function actually changes — not on every provider render.
   */
  const value = useMemo(
    () => ({ ...state, checkHealth }),
    [state, checkHealth]
  );

  return <HealthContext.Provider value={value}>{children}</HealthContext.Provider>;
};

/**
 * The ONLY supported way to read health state.
 *
 * Throws a clear error if used outside <HealthProvider>, turning a subtle bug
 * (undefined context) into an obvious, immediate failure with a helpful message.
 *
 * @returns {{ status: string, loading: boolean, error: string|null, checkHealth: () => Promise<void> }}
 */
export const useHealth = () => {
  const context = useContext(HealthContext);
  if (context === undefined) {
    throw new Error('useHealth must be used within a <HealthProvider>.');
  }
  return context;
};

export { HEALTH_STATUS };