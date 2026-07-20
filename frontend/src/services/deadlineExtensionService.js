import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for admin deadline-extension API calls.
 */

/** Admin: grants (or updates) a deadline extension for a user + type + date. */
export const grantExtension = (payload) => {
  return apiClient.post(API_PATHS.DEADLINE_EXTENSIONS, payload);
};

/** Admin: page of all granted extensions, newest target date first. */
export const getExtensions = (page = 0, size = 20) => {
  return apiClient.get(API_PATHS.DEADLINE_EXTENSIONS, { params: { page, size } });
};

/** Admin: revokes an extension by id. */
export const revokeExtension = (id) => {
  return apiClient.delete(`${API_PATHS.DEADLINE_EXTENSIONS}/${id}`);
};

export const deadlineExtensionService = {
  grantExtension,
  getExtensions,
  revokeExtension,
};
