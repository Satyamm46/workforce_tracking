import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for the self-registration / approval API.
 *
 * `register` is public (no JWT); the rest are Super Admin-only. The apiClient
 * response interceptor unwraps the ApiResponse envelope, so each function
 * resolves to { data, message, success }.
 */

/** Submits a public registration request. */
export const register = (payload) => {
  return apiClient.post(API_PATHS.REGISTRATIONS, payload);
};

/** Fetches a page of registration requests in the given status. */
export const getRequests = (status = 'PENDING', page = 0, size = 10) => {
  return apiClient.get(API_PATHS.REGISTRATIONS, { params: { status, page, size } });
};

/** Fetches the number of pending requests. */
export const getPendingCount = () => {
  return apiClient.get(API_PATHS.REGISTRATIONS_PENDING_COUNT);
};

/** Approves a request; role/comment are optional overrides. */
export const approve = (id, payload = {}) => {
  return apiClient.patch(`${API_PATHS.REGISTRATIONS}/${id}/approve`, payload);
};

/** Rejects a request with an optional comment. */
export const reject = (id, payload = {}) => {
  return apiClient.patch(`${API_PATHS.REGISTRATIONS}/${id}/reject`, payload);
};

export const registrationService = {
  register,
  getRequests,
  getPendingCount,
  approve,
  reject,
};
