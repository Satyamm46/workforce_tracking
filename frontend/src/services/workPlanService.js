import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for next-day work plans (schedules). Responses arrive
 * unwrapped as { data, message, success } via the apiClient interceptor.
 */

/** Creates or updates the caller's plan for tomorrow. */
export const submitTomorrow = (payload) => {
  return apiClient.post(API_PATHS.WORK_PLANS_TOMORROW, payload);
};

/** Late escape hatch: creates the caller's missing plan for today. */
export const submitTodayLate = (payload) => {
  return apiClient.post(API_PATHS.WORK_PLANS_TODAY, payload);
};

/** The caller's plan for one day; omit date for today. */
export const getMyPlanForDay = (date = null) => {
  const params = date ? { date } : {};
  return apiClient.get(API_PATHS.WORK_PLANS_ME_DAY, { params });
};

/** A page of the caller's plans, newest day first. */
export const getMyPlans = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.WORK_PLANS_ME, { params: { page, size } });
};

/** Manager: all plans for one day; omit date for today. */
export const getPlansByDate = (date = null, page = 0, size = 20) => {
  const params = { page, size };
  if (date) {
    params.date = date;
  }
  return apiClient.get(API_PATHS.WORK_PLANS, { params });
};

/** Manager: names of Admins/Employees without a plan for one day. */
export const getMissingSubmitters = (date = null) => {
  const params = date ? { date } : {};
  return apiClient.get(API_PATHS.WORK_PLANS_MISSING, { params });
};

export const workPlanService = {
  submitTomorrow,
  submitTodayLate,
  getMyPlanForDay,
  getMyPlans,
  getPlansByDate,
  getMissingSubmitters,
};
