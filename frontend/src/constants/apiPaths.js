/**
 * Registry of backend API endpoint paths, appended to API_BASE_URL by the
 * Axios client.
 */
export const API_PATHS = Object.freeze({
  /** Public liveness check (GET). */
  HEALTH: '/health',

  /** Login — exchanges credentials for a JWT (POST). */
  AUTH_LOGIN: '/v1/auth/login',

  /** Current authenticated user (GET). */
  AUTH_ME: '/v1/auth/me',

  /** User management base path. Item paths append `/{id}`. */
  USERS: '/v1/users',

  /** Attendance endpoints. */
  ATTENDANCE: '/v1/attendance',
  ATTENDANCE_CLOCK_OUT: '/v1/attendance/clock-out',
  ATTENDANCE_ME: '/v1/attendance/me',
  ATTENDANCE_ME_TODAY: '/v1/attendance/me/today',
  BREAK_START: '/v1/attendance/break/start',
  BREAK_END: '/v1/attendance/break/end',
});
