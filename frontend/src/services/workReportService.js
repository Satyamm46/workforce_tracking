import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for end-of-day work report API calls.
 */

/** Submits the caller's work report for their most recent checkout. */
export const submitReport = (payload) => {
  return apiClient.post(API_PATHS.WORK_REPORTS, payload);
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

export const workReportService = {
  submitReport,
  getMyReportForDay,
  getMyReports,
  getReportsByDate,
};
