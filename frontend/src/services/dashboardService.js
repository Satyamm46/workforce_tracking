import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/** Service module for dashboard REST calls. */
export const getStats = () => {
  return apiClient.get(API_PATHS.DASHBOARD_STATS);
};

export const dashboardService = { getStats };
