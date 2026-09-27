/**
 * Central application configuration, sourced from environment variables.
 *
 * Vite injects variables prefixed with `VITE_` at build time via
 * `import.meta.env`. Reading them in ONE place (here) rather than scattered
 * across the app means:
 *   - a single source of truth for environment-driven values, and
 *   - a safe fallback if a variable is missing during local development.
 */

/**
 * Base URL of the backend API, including the `/api` context path.
 * Falls back to the local backend if the env var is not set.
 */
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

/**
 * Default timeout (ms) applied to outgoing HTTP requests. Prevents a hung
 * request from leaving the UI waiting indefinitely.
 */
export const API_TIMEOUT_MS = 15000;