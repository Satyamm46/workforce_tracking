import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/** Service module for dashboard REST calls. */
export const getStats = () => {
  return apiClient.get(API_PATHS.DASHBOARD_STATS);
};

/**
 * The people counted by one statistic tile.
 *
 * @param {string} group one of the backend DashboardGroup names, e.g. 'ON_BREAK'
 */
export const getMembers = (group) => {
  return apiClient.get(API_PATHS.DASHBOARD_MEMBERS, { params: { group } });
};

export const dashboardService = { getStats, getMembers };
