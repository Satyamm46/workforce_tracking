import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for leave-management API calls. The apiClient attaches the
 * JWT and unwraps the ApiResponse envelope; each function resolves to
 * { data, message, success }.
 */

/** Submits a new leave request. */
export const applyForLeave = (payload) => {
  return apiClient.post(API_PATHS.LEAVES, payload);
};

/** Fetches a page of the caller's leave requests. */
export const getMyLeaves = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.LEAVES_ME, { params: { page, size } });
};

/** Fetches the caller's leave balance for the current year. */
export const getMyBalance = () => {
  return apiClient.get(API_PATHS.LEAVES_ME_BALANCE);
};

/** Cancels one of the caller's own pending requests. */
export const cancelLeave = (id) => {
  return apiClient.patch(`${API_PATHS.LEAVES}/${id}/cancel`);
};

/** Admin: fetches a page of requests with the given status. */
export const getLeavesByStatus = (status = 'PENDING', page = 0, size = 20) => {
  return apiClient.get(API_PATHS.LEAVES, { params: { status, page, size } });
};

/** Admin: approves a pending request with an optional comment. */
export const approveLeave = (id, comment = '') => {
  return apiClient.patch(`${API_PATHS.LEAVES}/${id}/approve`, { comment });
};

/** Admin: rejects a pending request with an optional comment. */
export const rejectLeave = (id, comment = '') => {
  return apiClient.patch(`${API_PATHS.LEAVES}/${id}/reject`, { comment });
};

export const leaveService = {
  applyForLeave,
  getMyLeaves,
  getMyBalance,
  cancelLeave,
  getLeavesByStatus,
  approveLeave,
  rejectLeave,
};
