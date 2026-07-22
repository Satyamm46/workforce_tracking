/**
 * Registry of backend API endpoint paths, appended to API_BASE_URL by the
 * Axios client.
 */
export const API_PATHS = Object.freeze({
  /** Public liveness check (GET). */
  HEALTH: '/health',

  /** Auth endpoints. */
  AUTH_LOGIN: '/v1/auth/login',
  AUTH_ME: '/v1/auth/me',

  /** User management base path. Item paths append `/{id}`. */
  USERS: '/v1/users',

  /** Self-registration endpoints. POST is public; the rest are Super Admin. */
  REGISTRATIONS: '/v1/registrations',
  REGISTRATIONS_PENDING_COUNT: '/v1/registrations/pending-count',

  /** Attendance endpoints. */
  ATTENDANCE: '/v1/attendance',
  ATTENDANCE_CHECK_IN: '/v1/attendance/check-in',
  ATTENDANCE_CLOCK_OUT: '/v1/attendance/clock-out',
  ATTENDANCE_ME: '/v1/attendance/me',
  ATTENDANCE_ME_TODAY: '/v1/attendance/me/today',
  BREAK_START: '/v1/attendance/break/start',
  BREAK_END: '/v1/attendance/break/end',
  ATTENDANCE_OVERTIME_EXTEND: '/v1/attendance/overtime/extend',

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

  /** Push-subscription endpoints. */
  PUSH_PUBLIC_KEY: '/v1/push/public-key',
  PUSH_SUBSCRIBE: '/v1/push/subscribe',
  PUSH_UNSUBSCRIBE: '/v1/push/unsubscribe',

  /** Work-plan (next-day schedule) endpoints. */
  WORK_PLANS: '/v1/work-plans',
  WORK_PLANS_TOMORROW: '/v1/work-plans/tomorrow',
  WORK_PLANS_TODAY: '/v1/work-plans/today',
  WORK_PLANS_ME: '/v1/work-plans/me',
  WORK_PLANS_ME_DAY: '/v1/work-plans/me/day',
  WORK_PLANS_MISSING: '/v1/work-plans/missing',

  /** Work report endpoints (end-of-day reports). */
  WORK_REPORTS: '/v1/work-reports',
  WORK_REPORTS_ME: '/v1/work-reports/me',
  WORK_REPORTS_ME_DAY: '/v1/work-reports/me/day',

  /** Lecture summary endpoints (post-lecture summaries). */
  LECTURE_SUMMARIES: '/v1/lecture-summaries',
  LECTURE_SUMMARIES_ME: '/v1/lecture-summaries/me',

  /** Deadline extension endpoints (admin grace periods). */
  DEADLINE_EXTENSIONS: '/v1/deadline-extensions',

  /** Dashboard endpoints. */
  DASHBOARD_STATS: '/v1/dashboard/stats',

  /** Report endpoints. */
  REPORTS_ATTENDANCE: '/v1/reports/attendance',
  REPORTS_TEACHING: '/v1/reports/teaching',
});
