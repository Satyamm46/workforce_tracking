import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for end-of-day work report API calls.
 */

/**
 * Submits the caller's work report. Pass `workDate` to file for an earlier
 * checked-out day (only accepted while that day's deadline is still open);
 * omit it for the most recent checkout.
 */
export const submitReport = (payload) => {
  return apiClient.post(API_PATHS.WORK_REPORTS, payload);
};

/** Checked-out days the caller still owes a report for and can still submit. */
export const getOpenReportDays = () => {
  return apiClient.get(API_PATHS.WORK_REPORTS_ME_OPEN_DAYS);
};

/** The caller's report for a specific day (null = today). */
export const getMyReportForDay = (date = null) => {
  const params = date ? { date } : {};
  return apiClient.get(API_PATHS.WORK_REPORTS_ME_DAY, { params });
};

/** A page of the caller's reports, newest first. */
export const getMyReports = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.WORK_REPORTS_ME, { params: { page, size } });
};

/** Admin: all reports for one day (null = today). */
export const getReportsByDate = (date = null, page = 0, size = 20) => {
  const params = { page, size };
  if (date) {
    params.date = date;
  }
  return apiClient.get(API_PATHS.WORK_REPORTS, { params });
};

/** Admin: all reports whose work date falls within [from, to] — backs monthly export. */
export const getReportsByRange = (from, to, page = 0, size = 100) => {
  return apiClient.get(API_PATHS.WORK_REPORTS, { params: { from, to, page, size } });
};

export const workReportService = {
  submitReport,
  getOpenReportDays,
  getMyReportForDay,
  getMyReports,
  getReportsByDate,
  getReportsByRange,
};
