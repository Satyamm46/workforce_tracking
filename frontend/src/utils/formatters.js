/**
 * Shared display-formatting helpers.
 *
 * Pure functions with no state or dependencies — the frontend counterpart of
 * the backend's DateTimeUtil: formatting rules defined once, reused by every
 * screen, so "3h 42m" never renders three different ways.
 */

/**
 * Formats a minute count as a human-readable duration, e.g. 222 -> "3h 42m".
 *
 * @param {number} totalMinutes minutes worked
 * @returns {string} formatted duration
 */
export const formatMinutes = (totalMinutes) => {
  const minutes = Math.max(Number(totalMinutes) || 0, 0);
  const hours = Math.floor(minutes / 60);
  const remaining = minutes % 60;
  return hours > 0 ? `${hours}h ${remaining}m` : `${remaining}m`;
};

/**
 * Extracts the HH:MM time from a backend LocalDateTime string,
 * e.g. "2026-07-14T09:05:23" -> "09:05". Returns a dash for null/absent.
 *
 * @param {string|null|undefined} isoDateTime backend LocalDateTime string
 * @returns {string} "HH:MM" or "—"
 */
export const formatTime = (isoDateTime) => {
  if (!isoDateTime || isoDateTime.length < 16) {
    return '—';
  }
  return isoDateTime.substring(11, 16);
};