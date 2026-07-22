import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for attendance API calls. The JWT rides automatically via
 * the apiClient interceptor; responses arrive unwrapped as
 * { data, message, success }.
 */

/** Starts or reopens the caller's working day. */
export const checkIn = () => {
  return apiClient.post(API_PATHS.ATTENDANCE_CHECK_IN);
};

/** Ends the caller's working day. */
export const clockOut = () => {
  return apiClient.post(API_PATHS.ATTENDANCE_CLOCK_OUT);
};

/** Fetches the caller's attendance record for today. */
export const getMyToday = () => {
  return apiClient.get(API_PATHS.ATTENDANCE_ME_TODAY);
};

/** Fetches a page of the caller's attendance history (newest first). */
export const getMyHistory = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.ATTENDANCE_ME, { params: { page, size } });
};

/**
 * Admin: fetches a page of all users' attendance for a day.
 * Omit `date` (null) to let the backend default to today.
 */
export const getByDate = (date = null, page = 0, size = 20) => {
  const params = { page, size };
  if (date) {
    params.date = date; // 'YYYY-MM-DD' — matches the backend's ISO.DATE
  }
  return apiClient.get(API_PATHS.ATTENDANCE, { params });
};

/** Starts a break for the caller. */
export const startBreak = () => {
  return apiClient.post(API_PATHS.BREAK_START);
};

/** Ends the current break and resumes work. */
export const endBreak = () => {
  return apiClient.post(API_PATHS.BREAK_END);
};

/** Extends the caller's current overtime window by another block. */
export const extendOvertime = () => {
  return apiClient.post(API_PATHS.ATTENDANCE_OVERTIME_EXTEND);
};

export const attendanceService = {
  checkIn,
  clockOut,
  startBreak,
  endBreak,
  extendOvertime,
  getMyToday,
  getMyHistory,
  getByDate,
};
