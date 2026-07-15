import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/** Service module for monthly report calls (admin). */

export const getAttendanceReport = (year, month) => {
  return apiClient.get(API_PATHS.REPORTS_ATTENDANCE, { params: { year, month } });
};

export const getTeachingReport = (year, month) => {
  return apiClient.get(API_PATHS.REPORTS_TEACHING, { params: { year, month } });
};

export const reportService = { getAttendanceReport, getTeachingReport };
