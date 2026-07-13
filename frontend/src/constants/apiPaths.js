/**
 * Registry of backend API endpoint paths.
 *
 * These are RELATIVE paths, appended to `API_BASE_URL` by the Axios client.
 * Example: `HEALTH` ('/health') + base ('http://localhost:8080/api')
 *          => 'http://localhost:8080/api/health'.
 *
 * Centralizing paths here is the frontend counterpart to the backend's
 * `ApiConstants`: components and services reference these names, never raw
 * URL strings, so an endpoint change is made in exactly one place.
 */
export const API_PATHS = Object.freeze({
  /** Public liveness check (GET). */
  HEALTH: '/health',
});