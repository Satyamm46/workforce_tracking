import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for the application's health/liveness endpoint.
 *
 * A "service" on the frontend groups the API calls for a single backend
 * domain. Components never call `apiClient` directly; they call these named
 * functions. This keeps HTTP details out of the UI and gives each backend
 * concern a single, testable home — the frontend counterpart to a backend
 * service class.
 */

/**
 * Calls GET /api/health to check whether the backend is up.
 *
 * The response interceptor in apiClient has already unwrapped the ApiResponse
 * envelope, so this resolves to `{ data, message, success }` where `data` is
 * the HealthResponse payload, e.g. `{ status: 'UP' }`.
 *
 * @returns {Promise<{ data: { status: string }, message: string, success: boolean }>}
 */
export const getHealthStatus = () => {
  return apiClient.get(API_PATHS.HEALTH);
};

/**
 * Grouped export so callers can import the whole service as a namespace:
 *   import { healthService } from '../services/healthService';
 *   healthService.getHealthStatus();
 */
export const healthService = {
  getHealthStatus,
};