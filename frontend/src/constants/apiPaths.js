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

    /** Leave endpoints. */
  LEAVES: '/v1/leaves',
  LEAVES_ME: '/v1/leaves/me',
  LEAVES_ME_BALANCE: '/v1/leaves/me/balance',

    /** Lecture endpoints. */
  LECTURES: '/v1/lectures',
  LECTURES_ME: '/v1/lectures/me',

    /** Notification endpoints. */
  NOTIFICATIONS_ME: '/v1/notifications/me',
  NOTIFICATIONS_UNREAD_COUNT: '/v1/notifications/me/unread-count',
  NOTIFICATIONS_READ_ALL: '/v1/notifications/me/read-all',


  /** Attendance endpoints. */
  ATTENDANCE: '/v1/attendance',
  ATTENDANCE_CLOCK_OUT: '/v1/attendance/clock-out',
  ATTENDANCE_ME: '/v1/attendance/me',
  ATTENDANCE_ME_TODAY: '/v1/attendance/me/today',
  BREAK_START: '/v1/attendance/break/start',
  BREAK_END: '/v1/attendance/break/end',
  DASHBOARD_STATS: '/v1/dashboard/stats',
});
